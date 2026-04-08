package org.example.aicodehelper.service;

import lombok.RequiredArgsConstructor;
import org.example.aicodehelper.domain.UserAccount;
import org.example.aicodehelper.domain.UserRole;
import org.example.aicodehelper.dto.request.AuthRequest;
import org.example.aicodehelper.dto.request.RegisterRequest;
import org.example.aicodehelper.vo.AuthResponse;
import org.example.aicodehelper.exception.BadRequestException;
import org.example.aicodehelper.exception.UnauthorizedException;
import org.example.aicodehelper.security.AppUserPrincipal;
import org.example.aicodehelper.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserAccountService userAccountService;
    private final ModelCatalogService modelCatalogService;
    private final SubscriptionPolicyService subscriptionPolicyService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userAccountService.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }
        UserAccount user = new UserAccount();
        user.setUsername(request.getUsername());
        user.setDisplayName(request.getDisplayName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.USER);
        user.setPreferredModel(modelCatalogService.getDefaultModel());
        userAccountService.createUser(user);
        subscriptionPolicyService.applyTemplate(user, user.getSubscriptionTier());
        userAccountService.saveUser(user);

        AppUserPrincipal principal = new AppUserPrincipal(user);
        return AuthResponse.builder()
                .token(jwtService.generateToken(principal))
                .user(userAccountService.toProfile(user))
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (DisabledException ex) {
            throw new UnauthorizedException("用户已被禁用");
        } catch (BadCredentialsException ex) {
            throw new UnauthorizedException("用户名或密码错误");
        }
        UserAccount user = userAccountService.getRequiredByUsername(request.getUsername());
        AppUserPrincipal principal = new AppUserPrincipal(user);
        return AuthResponse.builder()
                .token(jwtService.generateToken(principal))
                .user(userAccountService.toProfile(user))
                .build();
    }
}
