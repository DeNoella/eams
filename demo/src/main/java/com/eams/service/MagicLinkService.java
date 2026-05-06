package com.eams.service;

import com.eams.exception.MagicLinkExpiredException;
import com.eams.exception.ResourceNotFoundException;
import com.eams.model.MagicLinkToken;
import com.eams.model.Organisation;
import com.eams.model.User;
import com.eams.model.UserRole;
import com.eams.repository.MagicLinkTokenRepository;
import com.eams.repository.OrganisationRepository;
import com.eams.repository.RoleRepository;
import com.eams.repository.UserRoleRepository;
import com.eams.repository.UserRepository;
import com.eams.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MagicLinkService {

    private final UserRepository userRepository;
    private final MagicLinkTokenRepository magicLinkTokenRepository;
    private final OrganisationRepository organisationRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${eams.magic-link.expiry-minutes:15}")
    private int expiryMinutes;

    @Value("${eams.magic-link.base-url:http://localhost:5173}")
    private String baseUrl;

    @Value("${eams.magic-link.auto-provision-user:true}")
    private boolean autoProvisionUser;

    private static final UUID DEFAULT_ORGANISATION_ID =
        UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Transactional
    public void requestMagicLink(String email, String ipAddress) {
        // Sign-in path now strictly requires a registered account. The Sign Up
        // tab on the frontend calls /auth/register first, so unknown emails
        // here mean the user hasn't created an account yet.
        User user = userRepository.findByEmailIgnoreCaseAndIsDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "No account found with that email. Please create an account first."));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new RuntimeException("Account is inactive. Contact your administrator.");
        }

        magicLinkTokenRepository.invalidateAllForUser(user.getId());

        String rawToken = generateSecureToken();
        String tokenHash = hashToken(rawToken);

        MagicLinkToken magicToken = MagicLinkToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(OffsetDateTime.now().plusMinutes(expiryMinutes))
                .ipAddress(parseIpAddress(ipAddress))
                .isUsed(false)
                .build();
        magicLinkTokenRepository.save(magicToken);

        String magicLinkUrl = baseUrl + "/auth/verify?token=" + rawToken + "&email=" + email;
        emailService.sendMagicLinkEmail(user.getFullName(), email, magicLinkUrl);

        log.info("Magic link sent to user: {} from IP: {}", email, ipAddress);
    }

    private User autoProvisionUser(String email) {
        if (!autoProvisionUser) {
            throw new ResourceNotFoundException("No account found with email: " + email);
        }

        Organisation organisation = organisationRepository.findById(DEFAULT_ORGANISATION_ID)
                .orElseThrow(() -> new ResourceNotFoundException("Default organisation is not configured"));

        String derivedName = email.split("@")[0].replace(".", " ").replace("_", " ").trim();
        String fullName = derivedName.isBlank() ? "New User" : toTitleCase(derivedName);
        String employeeId = "AUTO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        User user = User.builder()
                .organisation(organisation)
                .employeeId(employeeId)
                .fullName(fullName)
                .email(email.toLowerCase())
                .jobTitle("Staff")
                .isActive(true)
                .mfaEnabled(true)
                .mfaMethod("MAGIC_LINK")
                .build();

        User saved = userRepository.save(user);
        roleRepository.findByCodeAndOrganisationIdAndIsDeletedFalse("READ_ONLY_VIEWER", organisation.getId())
                .ifPresent(role -> userRoleRepository.save(UserRole.builder()
                        .user(saved)
                        .role(role)
                        .assignedBy(saved)
                        .build()));

        log.info("Auto-provisioned magic-link user: {}", email);
        return saved;
    }

    private String toTitleCase(String value) {
        String[] parts = value.split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1).toLowerCase());
            }
        }
        return builder.toString();
    }

    @Transactional
    public TokenPair verifyMagicLink(String rawToken, String email) {
        String tokenHash = hashToken(rawToken);

        MagicLinkToken magicToken = magicLinkTokenRepository
                .findByTokenHashAndIsUsedFalse(tokenHash)
                .orElseThrow(() -> new MagicLinkExpiredException("Invalid or already used magic link"));

        if (OffsetDateTime.now().isAfter(magicToken.getExpiresAt())) {
            throw new MagicLinkExpiredException("Magic link has expired. Please request a new one.");
        }

        if (!magicToken.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new MagicLinkExpiredException("Invalid magic link");
        }

        magicToken.setIsUsed(true);
        magicToken.setUsedAt(OffsetDateTime.now());
        magicLinkTokenRepository.save(magicToken);

        User user = magicToken.getUser();
        user.setLastLoginAt(OffsetDateTime.now());
        user.setFailedLoginCount((short) 0);

        List<String> roleCodes = userRoleRepository.findRoleCodesByUserId(user.getId());

        String accessToken = jwtTokenProvider.generateAccessToken(
            user.getId(), user.getOrganisation().getId(), roleCodes);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        log.info("User authenticated via magic link: {}", email);

        return new TokenPair(accessToken, refreshToken);
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }

    private InetAddress parseIpAddress(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return null;
        }
        try {
            return InetAddress.getByName(ipAddress);
        } catch (Exception e) {
            log.warn("Unable to parse client IP address: {}", ipAddress);
            return null;
        }
    }

    public record TokenPair(String accessToken, String refreshToken) {}

    public String generateAccessTokenFromRefresh(String refreshToken) {
        UUID userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<String> roleCodes = userRoleRepository.findRoleCodesByUserId(userId);
        return jwtTokenProvider.generateAccessToken(userId, user.getOrganisation().getId(), roleCodes);
    }

    public TokenPair devAutoLogin(String email) {
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElse(null);
        
        if (user == null) {
            throw new ResourceNotFoundException("No user found with email: " + email);
        }
        
        user.setLastLoginAt(OffsetDateTime.now());
        List<String> roleCodes = userRoleRepository.findRoleCodesByUserId(user.getId());
        
        String accessToken = jwtTokenProvider.generateAccessToken(
            user.getId(), user.getOrganisation().getId(), roleCodes);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        
        log.info("Dev auto-login for user: {}", email);
        return new TokenPair(accessToken, refreshToken);
    }
}