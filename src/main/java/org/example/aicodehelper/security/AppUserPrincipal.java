package org.example.aicodehelper.security;

import lombok.Getter;
import org.example.aicodehelper.domain.UserAccount;
import org.example.aicodehelper.domain.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class AppUserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final String displayName;
    private final UserRole role;
    private final boolean enabled;

    public AppUserPrincipal(UserAccount user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPasswordHash();
        this.displayName = user.getDisplayName();
        this.role = user.getRole();
        this.enabled = Boolean.TRUE.equals(user.getEnabled());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
