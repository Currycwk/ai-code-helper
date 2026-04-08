package org.example.aicodehelper.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "UpdateUserStatusRequest", description = "更新用户启用状态请求")
public class UpdateUserStatusRequest {

    @NotNull
    @Schema(description = "是否启用用户", example = "false")
    private Boolean enabled;
}
