package com.eams.controller;

import com.eams.dto.request.MagicLinkRequestDto;
import com.eams.dto.request.RegisterRequestDto;
import com.eams.dto.response.ApiResponse;
import com.eams.dto.response.AuthResponseDto;
import com.eams.service.MagicLinkService;
import com.eams.service.UserService;
import com.eams.security.JwtTokenProvider;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MagicLinkService magicLinkService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Self-service Sign Up endpoint backing the Sign Up tab on the auth page.
     * Returns 200 OK on success, 409 Conflict when the email is already in use
     * (handled by GlobalExceptionHandler), 400 on validation failures.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequestDto request) {
        userService.registerWithPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Account created successfully"));
    }

    @PostMapping("/magic-link/request")
    public ResponseEntity<ApiResponse<String>> requestMagicLink(
            @Valid @RequestBody MagicLinkRequestDto request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIpAddress(httpRequest);
        magicLinkService.requestMagicLink(request.getEmail(), ipAddress);

        return ResponseEntity.ok(ApiResponse.success(
            "Magic link sent to " + request.getEmail() +
            ". Check your inbox — link expires in 15 minutes."));
    }

    @PostMapping("/dev-login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> devLogin(
            @RequestBody MagicLinkRequestDto request,
            HttpServletResponse response) {
        try {
            MagicLinkService.TokenPair tokenPair = magicLinkService.devAutoLogin(request.getEmail());
            
            ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", tokenPair.refreshToken())
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
            
            return ResponseEntity.ok(ApiResponse.success(
                new AuthResponseDto(tokenPair.accessToken(), "Bearer")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Dev login failed: " + e.getMessage()));
        }
    }

    @GetMapping("/magic-link/verify")
    public ResponseEntity<ApiResponse<AuthResponseDto>> verifyMagicLink(
            @RequestParam String token,
            @RequestParam String email,
            HttpServletRequest request,
            HttpServletResponse response) {

        MagicLinkService.TokenPair tokenPair =
            magicLinkService.verifyMagicLink(token, email);

        // Set refresh token as HttpOnly cookie
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token",
                tokenPair.refreshToken())
                .httpOnly(true)
                .secure(request.isSecure())
                .sameSite(request.isSecure() ? "Strict" : "Lax")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok(ApiResponse.success(
            new AuthResponseDto(tokenPair.accessToken(), "Bearer")));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponseDto>> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("No refresh token found"));
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid refresh token"));
        }
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Refresh token type required"));
        }
        String newToken = magicLinkService.generateAccessTokenFromRefresh(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(
            new AuthResponseDto(newToken, "Bearer")));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletResponse response) {
        // Clear the refresh token cookie
        ResponseCookie clearCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());

        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}