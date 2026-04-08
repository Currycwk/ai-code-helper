package org.example.aicodehelper.controller;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.aicodehelper.domain.UserAccount;
import org.example.aicodehelper.dto.request.UpdatePreferredModelRequest;
import org.example.aicodehelper.vo.ModelSummaryResponse;
import org.example.aicodehelper.vo.UserProfileResponse;
import org.example.aicodehelper.vo.UserUsageSummaryResponse;
import org.example.aicodehelper.security.AppUserPrincipal;
import org.example.aicodehelper.service.ModelCatalogService;
import org.example.aicodehelper.service.UsageService;
import org.example.aicodehelper.service.UserAccountService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
@Tag(name = "User", description = "用户中心接口，包括个人资料、模型偏好与调用统计")
public class UserController {

    private final UserAccountService userAccountService;
    private final ModelCatalogService modelCatalogService;
    private final UsageService usageService;

    @GetMapping
    @Operation(summary = "获取个人资料", description = "返回当前用户的基础资料、角色、今日调用数、剩余配额和默认模型。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未登录", content = @Content)
    })
    public UserProfileResponse profile(@Parameter(hidden = true) @AuthenticationPrincipal AppUserPrincipal principal) {
        UserAccount user = userAccountService.getRequiredById(principal.getId());
        return userAccountService.toProfile(user);
    }

    @PatchMapping("/model")
    @Operation(summary = "切换默认模型", description = "修改当前用户默认使用的大模型，后续聊天请求会自动按该模型调用。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "模型名称不支持", content = @Content),
            @ApiResponse(responseCode = "401", description = "未登录", content = @Content)
    })
    public UserProfileResponse updatePreferredModel(@Parameter(hidden = true) @AuthenticationPrincipal AppUserPrincipal principal,
                                                    @Valid @RequestBody UpdatePreferredModelRequest request) {
        UserAccount user = userAccountService.getRequiredById(principal.getId());
        return userAccountService.updatePreferredModel(user, request.getModelName());
    }

    @GetMapping("/models")
    @Operation(summary = "查询可选模型列表", description = "返回系统支持的模型目录，以及每个模型是否为默认模型、是否支持联网搜索。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未登录", content = @Content)
    })
    public List<ModelSummaryResponse> models() {
        return modelCatalogService.listAvailableModels();
    }

    @GetMapping("/usage")
    @Operation(summary = "查询个人调用统计", description = "返回当前用户累计调用次数、累计字符量和最近 20 次调用记录。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未登录", content = @Content)
    })
    public UserUsageSummaryResponse usage(@Parameter(hidden = true) @AuthenticationPrincipal AppUserPrincipal principal) {
        UserAccount user = userAccountService.getRequiredById(principal.getId());
        return usageService.buildUserSummary(user);
    }
}
