package org.example.aicodehelper.security;

import lombok.Getter;
import org.example.aicodehelper.domain.UserAccount;
import org.example.aicodehelper.domain.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 当前登录用户主体对象。
 * 对 UserAccount 做了一层安全上下文适配，封装成 Spring Security 可识别的认证主体，
 * 方便控制器和权限框架统一获取用户 ID、角色和用户名。
 */
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
