package org.example.aicodehelper.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "AuthRequest", description = "登录请求体")
public class AuthRequest {
    @Schema(description = "登录用户名", example = "admin")
    @NotBlank
    @Size(min = 3, max = 64)
    private String username;

    @Schema(description = "登录密码", example = "admin123456")
    @NotBlank
    @Size(min = 6, max = 64)
    private String password;
}
