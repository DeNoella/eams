package com.eams.security;

import com.eams.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class EamsUserDetails implements UserDetails {

    private final UUID userId;
    private final UUID organisationId;
    private final String email;
    private final List<GrantedAuthority> authorities;
    private final boolean active;

    public EamsUserDetails(User user, List<String> roleCodes) {
        this.userId = user.getId();
        this.organisationId = user.getOrganisation().getId();
        this.email = user.getEmail();
        this.active = Boolean.TRUE.equals(user.getIsActive());
        this.authorities = roleCodes.stream()
                .map(code -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + code))
                .toList();
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return null; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return active; }
}