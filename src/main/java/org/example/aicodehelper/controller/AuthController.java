package org.example.aicodehelper.controller;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.aicodehelper.dto.request.AuthRequest;
import org.example.aicodehelper.dto.request.RegisterRequest;
import org.example.aicodehelper.vo.AuthResponse;
import org.example.aicodehelper.vo.UserProfileResponse;
import org.example.aicodehelper.security.AppUserPrincipal;
import org.example.aicodehelper.service.AuthService;
import org.example.aicodehelper.service.UserAccountService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器。
 * 提供注册、登录和查询当前登录用户信息的接口，
 * 是前端建立用户会话和获取当前身份状态的入口。
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "登录注册与当前用户认证信息接口")
public class AuthController {

    private final AuthService authService;
    private final UserAccountService userAccountService;

    @PostMapping("/register")
    @SecurityRequirements
    @Operation(
            summary = "用户注册",
            description = "创建普通用户账号并直接返回 JWT，便于注册后立即登录测试。"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "注册成功"),
            @ApiResponse(responseCode = "400", description = "参数错误或用户名已存在", content = @Content)
    })
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(
            summary = "用户登录",
            description = "使用用户名和密码登录，返回 JWT 和当前用户概要信息。"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "登录成功"),
            @ApiResponse(responseCode = "401", description = "用户名或密码错误", content = @Content)
    })
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @Operation(
            summary = "获取当前登录用户",
            description = "根据当前 JWT 返回登录用户的资料、角色、配额和默认模型。"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未登录或 Token 无效", content = @Content)
    })
    public UserProfileResponse me(@Parameter(hidden = true) @AuthenticationPrincipal AppUserPrincipal principal) {
        return userAccountService.toProfile(userAccountService.getRequiredById(principal.getId()));
    }
}
