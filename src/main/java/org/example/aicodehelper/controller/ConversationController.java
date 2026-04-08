package org.example.aicodehelper.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.aicodehelper.domain.UserAccount;
import org.example.aicodehelper.dto.request.CreateConversationRequest;
import org.example.aicodehelper.dto.request.RenameConversationRequest;
import org.example.aicodehelper.vo.ConversationDetailResponse;
import org.example.aicodehelper.vo.ConversationSummaryResponse;
import org.example.aicodehelper.security.AppUserPrincipal;
import org.example.aicodehelper.service.ConversationService;
import org.example.aicodehelper.service.UserAccountService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 会话中心控制器。
 * 负责会话的创建、查询、重命名、删除，以及消息级别的删除操作，
 * 对外提供“历史对话”和“继续对话”所需的核心接口。
 */
@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
@Tag(name = "Conversation", description = "会话中心接口，包括创建、查询、删除会话以及删除单条消息")
public class ConversationController {

    private final ConversationService conversationService;
    private final UserAccountService userAccountService;

    @PostMapping
    @Operation(summary = "创建新会话", description = "创建一个空白会话，后续聊天基于该会话继续。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "401", description = "未登录", content = @Content)
    })
    public ConversationSummaryResponse create(@Parameter(hidden = true) @AuthenticationPrincipal AppUserPrincipal principal,
                                              @RequestBody(required = false) CreateConversationRequest request) {
        UserAccount user = userAccountService.getRequiredById(principal.getId());
        return conversationService.createConversation(user, request);
    }

    @GetMapping
    @Operation(summary = "查询会话列表", description = "返回当前用户的历史会话列表，支持跨端同步。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未登录", content = @Content)
    })
    public List<ConversationSummaryResponse> list(@Parameter(hidden = true) @AuthenticationPrincipal AppUserPrincipal principal) {
        UserAccount user = userAccountService.getRequiredById(principal.getId());
        return conversationService.listConversations(user);
    }

    @GetMapping("/{conversationId}")
    @Operation(summary = "查询会话详情", description = "加载某个会话的完整消息列表，可用于继续对话或多端同步。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "会话不存在", content = @Content),
            @ApiResponse(responseCode = "401", description = "未登录", content = @Content)
    })
    public ConversationDetailResponse detail(@Parameter(hidden = true) @AuthenticationPrincipal AppUserPrincipal principal,
                                             @Parameter(description = "会话 ID", example = "12") @PathVariable Long conversationId) {
        UserAccount user = userAccountService.getRequiredById(principal.getId());
        return conversationService.getConversationDetail(user, conversationId);
    }

    @PatchMapping("/{conversationId}/title")
    @Operation(summary = "重命名会话", description = "修改会话标题，并标记为手动标题。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "修改成功"),
            @ApiResponse(responseCode = "400", description = "标题不能为空", content = @Content),
            @ApiResponse(responseCode = "404", description = "会话不存在", content = @Content),
            @ApiResponse(responseCode = "401", description = "未登录", content = @Content)
    })
    public ConversationSummaryResponse renameConversation(@Parameter(hidden = true) @AuthenticationPrincipal AppUserPrincipal principal,
                                                          @Parameter(description = "会话 ID", example = "12") @PathVariable Long conversationId,
                                                          @Valid @RequestBody RenameConversationRequest request) {
        UserAccount user = userAccountService.getRequiredById(principal.getId());
        return conversationService.renameConversation(user, conversationId, request.getTitle());
    }

    @DeleteMapping("/{conversationId}")
    @Operation(summary = "删除会话", description = "删除整个会话以及其下的所有消息。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "会话不存在", content = @Content),
            @ApiResponse(responseCode = "401", description = "未登录", content = @Content)
    })
    public void deleteConversation(@Parameter(hidden = true) @AuthenticationPrincipal AppUserPrincipal principal,
                                   @Parameter(description = "会话 ID", example = "12") @PathVariable Long conversationId) {
        UserAccount user = userAccountService.getRequiredById(principal.getId());
        conversationService.deleteConversation(user, conversationId);
    }

    @DeleteMapping("/{conversationId}/messages/{messageId}")
    @Operation(summary = "删除单条消息", description = "删除会话中的某条消息，并同步修正会话摘要和记忆。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "会话或消息不存在", content = @Content),
            @ApiResponse(responseCode = "401", description = "未登录", content = @Content)
    })
    public void deleteMessage(@Parameter(hidden = true) @AuthenticationPrincipal AppUserPrincipal principal,
                              @Parameter(description = "会话 ID", example = "12") @PathVariable Long conversationId,
                              @Parameter(description = "消息 ID", example = "88") @PathVariable Long messageId) {
        UserAccount user = userAccountService.getRequiredById(principal.getId());
        conversationService.deleteMessage(user, conversationId, messageId);
    }
}
