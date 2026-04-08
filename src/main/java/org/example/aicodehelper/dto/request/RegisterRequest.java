package org.example.aicodehelper.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "RegisterRequest", description = "注册请求体")
public class RegisterRequest {
    @Schema(description = "注册用户名", example = "alice")
    @NotBlank
    @Size(min = 3, max = 64)
    private String username;

    @Schema(description = "显示名称", example = "Alice Zhang")
    @NotBlank
    @Size(min = 2, max = 64)
    private String displayName;

    @Schema(description = "登录密码", example = "alice123456")
    @NotBlank
    @Size(min = 6, max = 64)
    private String password;
}
