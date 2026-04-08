import { nextTick, ref, type ComputedRef, type Ref } from "vue";
import apiClient from "../api/client";
import type {
  AppView,
  ChatMessage,
  ConversationDetailResponse,
  ConversationMessageResponse,
  ConversationSummaryResponse,
  SseChatEvent
} from "../types/app";

interface ChatWorkspaceOptions {
  token: Ref<string>;
  isAuthenticated: ComputedRef<boolean>;
  activeView: Ref<AppView>;
  notice: Ref<string>;
  refreshUserData: () => Promise<void>;
  onUnauthorized: () => void;
}

export function useChatWorkspace(options: ChatWorkspaceOptions) {
  const conversations = ref<ConversationSummaryResponse[]>([]);
  const currentConversationId = ref<number | null>(null);
  const currentConversationTitle = ref("未选择会话");
  const conversationsLoading = ref(false);
  const conversationLoading = ref(false);
  const creatingConversation = ref(false);
  const renamingConversationId = ref<number | null>(null);
  const deletingConversationId = ref<number | null>(null);
  const deletingMessageId = ref<number | string | null>(null);
  const editingMessageId = ref<number | string | null>(null);
  const copiedMessageId = ref<number | string | null>(null);
  const messages = ref<ChatMessage[]>([]);
  const input = ref("");
  const isStreaming = ref(false);
  const chatBodyRef = ref<HTMLDivElement | null>(null);
  let streamController: AbortController | null = null;

  function buildSseUrl(conversationId: number, message: string) {
    const params = new URLSearchParams({ conversationId: String(conversationId), message });
    return `/api/ai/chat?${params.toString()}`;
  }

  function buildRegenerateUrl(conversationId: number, messageId: number) {
    return `/api/ai/conversations/${conversationId}/messages/${messageId}/regenerate`;
  }

  function toChatMessages(items: ConversationMessageResponse[]) {
    return items
      .filter((item) => item.role !== "SYSTEM")
      .map<ChatMessage>((item) => ({
        id: item.id,
        role: item.role === "USER" ? "user" : "assistant",
        content: item.content,
        createdAt: item.createdAt,
        meta: item.modelName ? { model: item.modelName } : undefined
      }));
  }

  async function scrollToBottom() {
    await nextTick();
    if (chatBodyRef.value) {
      chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight;
    }
  }

  function stopStream() {
    if (streamController) {
      streamController.abort();
      streamController = null;
    }
    isStreaming.value = false;
  }

  async function handleAuthorizedRequest<T>(action: () => Promise<T>) {
    try {
      return await action();
    } catch (error: any) {
      if (error?.response?.status === 401) {
        options.onUnauthorized();
      }
      throw error;
    }
  }

  async function loadConversations() {
    if (!options.token.value) return;
    conversationsLoading.value = true;
    try {
      const response = await handleAuthorizedRequest(() =>
        apiClient.get<ConversationSummaryResponse[]>("/conversations")
      );
      conversations.value = response.data;
    } finally {
      conversationsLoading.value = false;
    }
  }

  async function openConversation(id: number, switchToChat = true) {
    conversationLoading.value = true;
    try {
      const response = await handleAuthorizedRequest(() =>
        apiClient.get<ConversationDetailResponse>(`/conversations/${id}`)
      );
      currentConversationId.value = response.data.id;
      currentConversationTitle.value = response.data.title;
      messages.value = toChatMessages(response.data.messages);
      if (switchToChat) options.activeView.value = "chat";
      await scrollToBottom();
    } finally {
      conversationLoading.value = false;
    }
  }

  async function createConversation(makeActive = true) {
    creatingConversation.value = true;
    options.notice.value = "";
    try {
      const response = await handleAuthorizedRequest(() =>
        apiClient.post<ConversationSummaryResponse>("/conversations", {})
      );
      const created = response.data;
      conversations.value = [created, ...conversations.value.filter((item) => item.id !== created.id)];
      if (makeActive) {
        currentConversationId.value = created.id;
        currentConversationTitle.value = created.title;
        messages.value = [];
        options.activeView.value = "chat";
      }
      return created;
    } catch (error: any) {
      options.notice.value = error?.response?.data?.message ?? "创建会话失败。";
      return null;
    } finally {
      creatingConversation.value = false;
    }
  }

  async function renameConversation(id: number, currentTitle: string) {
    const nextTitle = window.prompt("请输入新的会话标题", currentTitle)?.trim();
    if (!nextTitle) return;
    renamingConversationId.value = id;
    options.notice.value = "";
    try {
      const response = await handleAuthorizedRequest(() =>
        apiClient.patch<ConversationSummaryResponse>(`/conversations/${id}/title`, { title: nextTitle })
      );
      const updated = response.data;
      conversations.value = conversations.value.map((item) => (item.id === id ? updated : item));
      if (currentConversationId.value === id) currentConversationTitle.value = updated.title;
      options.notice.value = "会话标题已更新。";
    } catch (error: any) {
      options.notice.value = error?.response?.data?.message ?? "会话重命名失败。";
    } finally {
      renamingConversationId.value = null;
    }
  }

  async function deleteConversation(id: number) {
    if (!window.confirm("确认删除这个对话吗？删除后不可恢复。")) return;
    deletingConversationId.value = id;
    options.notice.value = "";
    try {
      await handleAuthorizedRequest(() => apiClient.delete(`/conversations/${id}`));
      conversations.value = conversations.value.filter((item) => item.id !== id);
      if (currentConversationId.value === id) {
        const next = conversations.value[0];
        if (next) {
          await openConversation(next.id, false);
        } else {
          currentConversationId.value = null;
          currentConversationTitle.value = "未选择会话";
          messages.value = [];
        }
      }
      options.notice.value = "对话已删除。";
    } catch (error: any) {
      options.notice.value = error?.response?.data?.message ?? "删除对话失败。";
    } finally {
      deletingConversationId.value = null;
    }
  }

  async function deleteMessage(id: number | string) {
    if (!currentConversationId.value || typeof id !== "number") return;
    if (!window.confirm("确认删除这条消息吗？")) return;
    deletingMessageId.value = id;
    options.notice.value = "";
    try {
      await handleAuthorizedRequest(() =>
        apiClient.delete(`/conversations/${currentConversationId.value}/messages/${id}`)
      );
      await Promise.all([openConversation(currentConversationId.value, false), loadConversations()]);
      options.notice.value = "消息已删除。";
    } catch (error: any) {
      options.notice.value = error?.response?.data?.message ?? "删除消息失败。";
    } finally {
      deletingMessageId.value = null;
    }
  }

  function buildHeaders(extraHeaders?: HeadersInit): HeadersInit {
    const headers: HeadersInit = {
      Accept: "text/event-stream",
      "Cache-Control": "no-cache",
      "Accept-Encoding": "identity",
      ...extraHeaders
    };
    if (options.token.value) {
      headers.Authorization = `Bearer ${options.token.value}`;
    }
    return headers;
  }

  async function consumeSseResponse(response: Response, aiMessage: ChatMessage) {
    if (!response.ok || !response.body) {
      throw new Error(response.status === 403 ? "今日调用配额已用完。" : "流式请求失败。");
    }
    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let buffer = "";
    let doneReceived = false;

    while (true) {
      const { value, done } = await reader.read();
      if (done) break;
      buffer += decoder.decode(value, { stream: true }).replace(/\r/g, "");
      while (true) {
        const dividerIndex = buffer.indexOf("\n\n");
        if (dividerIndex === -1) break;
        const rawEvent = buffer.slice(0, dividerIndex);
        buffer = buffer.slice(dividerIndex + 2);
        const data = rawEvent
          .split("\n")
          .filter((line) => line.startsWith("data:"))
          .map((line) => line.slice(5).trimStart())
          .join("\n");
        if (!data) continue;
        const parsed = JSON.parse(data) as SseChatEvent;
        if (parsed.type === "delta" && parsed.delta) {
          aiMessage.content += parsed.delta;
          messages.value = [...messages.value];
          scrollToBottom();
        } else if (parsed.type === "done") {
          aiMessage.meta = parsed.meta;
          messages.value = [...messages.value];
          doneReceived = true;
        } else if (parsed.type === "error") {
          aiMessage.content += `\n\n[流式错误] ${parsed.error ?? "unknown"}`;
          messages.value = [...messages.value];
          doneReceived = true;
        }
      }
      if (doneReceived) {
        await reader.cancel();
        break;
      }
    }
  }

  async function startFetchStream(conversationId: number, aiMessage: ChatMessage, userMessage: string) {
    streamController = new AbortController();
    const response = await fetch(buildSseUrl(conversationId, userMessage), {
      signal: streamController.signal,
      cache: "no-store",
      headers: buildHeaders()
    });
    await consumeSseResponse(response, aiMessage);
  }

  async function sendMessage() {
    const content = input.value.trim();
    if (!content || !options.isAuthenticated.value) return;
    let conversationId = currentConversationId.value;
    if (!conversationId) {
      const created = await createConversation(true);
      conversationId = created?.id ?? null;
    }
    if (!conversationId) return;

    input.value = "";
    const userMessage: ChatMessage = { id: `u-${Date.now()}`, role: "user", content };
    const aiMessage: ChatMessage = { id: `a-${Date.now()}`, role: "assistant", content: "", streaming: true };
    messages.value.push(userMessage, aiMessage);
    isStreaming.value = true;

    try {
      await startFetchStream(conversationId, aiMessage, content);
      await Promise.all([options.refreshUserData(), loadConversations(), openConversation(conversationId, false)]);
    } catch (error: any) {
      aiMessage.content += `\n\n${error?.message ?? "请求失败"}`;
      messages.value = [...messages.value];
    } finally {
      aiMessage.streaming = false;
      isStreaming.value = false;
      scrollToBottom();
    }
  }

  async function regenerateMessage(message: ChatMessage) {
    if (!currentConversationId.value || typeof message.id !== "number" || message.role !== "user") return;
    const nextContent = window.prompt("编辑这条消息，并重新生成后续回答", message.content)?.trim();
    if (!nextContent || nextContent === message.content) return;

    const index = messages.value.findIndex((item) => item.id === message.id);
    const editedMessage: ChatMessage = { ...message, content: nextContent };
    const aiMessage: ChatMessage = { id: `regen-${Date.now()}`, role: "assistant", content: "", streaming: true };
    messages.value = index >= 0
      ? [...messages.value.slice(0, index), editedMessage, aiMessage]
      : [...messages.value, editedMessage, aiMessage];

    editingMessageId.value = message.id;
    isStreaming.value = true;
    options.notice.value = "";

    try {
      streamController = new AbortController();
      const response = await fetch(buildRegenerateUrl(currentConversationId.value, message.id), {
        method: "POST",
        signal: streamController.signal,
        cache: "no-store",
        headers: buildHeaders({ "Content-Type": "application/json" }),
        body: JSON.stringify({ message: nextContent })
      });
      await consumeSseResponse(response, aiMessage);
      await Promise.all([options.refreshUserData(), loadConversations(), openConversation(currentConversationId.value, false)]);
      options.notice.value = "消息已更新并重新生成。";
    } catch (error: any) {
      aiMessage.content += `\n\n${error?.message ?? "重新生成失败"}`;
      messages.value = [...messages.value];
    } finally {
      aiMessage.streaming = false;
      editingMessageId.value = null;
      isStreaming.value = false;
      scrollToBottom();
    }
  }

  async function copyMessage(message: ChatMessage) {
    await navigator.clipboard.writeText(message.content);
    copiedMessageId.value = message.id;
    setTimeout(() => {
      if (copiedMessageId.value === message.id) copiedMessageId.value = null;
    }, 1200);
  }

  async function bootstrapWorkspace() {
    if (!options.token.value) return;
    await loadConversations();
    if (conversations.value.length > 0) {
      await openConversation(conversations.value[0].id, false);
    }
  }

  function resetWorkspace() {
    stopStream();
    conversations.value = [];
    currentConversationId.value = null;
    currentConversationTitle.value = "未选择会话";
    messages.value = [];
    input.value = "";
  }

  return {
    bootstrapWorkspace,
    chatBodyRef,
    conversations,
    conversationsLoading,
    conversationLoading,
    copiedMessageId,
    copyMessage,
    createConversation,
    creatingConversation,
    currentConversationId,
    currentConversationTitle,
    deleteConversation,
    deletingConversationId,
    deleteMessage,
    deletingMessageId,
    editingMessageId,
    input,
    isStreaming,
    loadConversations,
    messages,
    openConversation,
    regenerateMessage,
    renamingConversationId,
    renameConversation,
    resetWorkspace,
    sendMessage,
    stopStream
  };
}
