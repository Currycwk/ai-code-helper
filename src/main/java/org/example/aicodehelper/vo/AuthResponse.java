package org.example.aicodehelper.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(name = "AuthResponse", description = "登录或注册成功后的响应")
public class AuthResponse {
    @Schema(description = "JWT 访问令牌", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.demo-signature")
    private String token;
    @Schema(description = "当前登录用户")
    private UserProfileResponse user;
}
