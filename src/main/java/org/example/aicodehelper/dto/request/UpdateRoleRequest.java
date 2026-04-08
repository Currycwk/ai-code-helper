package org.example.aicodehelper.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.aicodehelper.domain.UserRole;

@Data
@Schema(name = "UpdateRoleRequest", description = "修改用户角色请求")
public class UpdateRoleRequest {
    @Schema(description = "用户角色，可选 USER 或 ADMIN", example = "ADMIN")
    @NotNull
    private UserRole role;
}
