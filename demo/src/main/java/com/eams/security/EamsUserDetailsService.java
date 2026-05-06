package com.eams.security;

import com.eams.model.User;
import com.eams.repository.UserRepository;
import com.eams.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EamsUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        List<String> roleCodes = userRoleRepository.findRoleCodesByUserId(user.getId());
        return new EamsUserDetails(user, roleCodes);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
        List<String> roleCodes = userRoleRepository.findRoleCodesByUserId(user.getId());
        return new EamsUserDetails(user, roleCodes);
    }
}