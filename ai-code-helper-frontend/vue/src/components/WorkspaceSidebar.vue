<script setup lang="ts">
import type { AppView, ConversationSummaryResponse, ModelSummaryResponse, UserProfileResponse } from "../types/app";

defineProps<{
  activeView: AppView;
  isAdmin: boolean;
  currentUser: UserProfileResponse | null;
  availableModels: ModelSummaryResponse[];
  conversations: ConversationSummaryResponse[];
  currentConversationId: number | null;
  creatingConversation: boolean;
  conversationsLoading: boolean;
  renamingConversationId: number | null;
  deletingConversationId: number | null;
}>();

const emit = defineEmits<{
  (e: "change-view", view: AppView): void;
  (e: "create-conversation"): void;
  (e: "refresh-conversations"): void;
  (e: "open-conversation", id: number): void;
  (e: "rename-conversation", id: number, title: string): void;
  (e: "delete-conversation", id: number): void;
  (e: "save-model"): void;
  (e: "logout"): void;
}>();
</script>

<template>
  <aside class="sidebar">
    <div>
      <p class="eyebrow">Workspace</p>
      <h2>{{ currentUser?.displayName }}</h2>
      <p class="sidebar-subtitle">{{ currentUser?.username }} | {{ currentUser?.role }}</p>
    </div>

    <nav class="nav-list">
      <button :class="{ active: activeView === 'chat' }" @click="emit('change-view', 'chat')">对话工作台</button>
      <button :class="{ active: activeView === 'account' }" @click="emit('change-view', 'account')">账户详情</button>
      <button v-if="isAdmin" :class="{ active: activeView === 'admin' }" @click="emit('change-view', 'admin')">管理员后台</button>
    </nav>

    <div class="sidebar-card">
      <div class="sidebar-stat"><span>今日已用</span><strong>{{ currentUser?.todayRequestCount ?? 0 }}</strong></div>
      <div class="sidebar-stat"><span>剩余配额</span><strong>{{ currentUser?.remainingQuota ?? 0 }}</strong></div>
    </div>

    <div class="sidebar-card conversation-card">
      <div class="table-header">
        <strong>会话中心</strong>
        <div class="action-group">
          <button class="secondary-button small" :disabled="creatingConversation" @click="emit('create-conversation')">
            {{ creatingConversation ? "创建中..." : "新会话" }}
          </button>
          <button class="ghost-button small" :disabled="conversationsLoading" @click="emit('refresh-conversations')">刷新</button>
        </div>
      </div>

      <div v-if="!conversations.length" class="empty-inline">暂无历史会话</div>
      <div v-else class="conversation-list">
        <div v-for="conversation in conversations" :key="conversation.id" class="conversation-item" :class="{ active: currentConversationId === conversation.id }">
          <div class="conversation-item-head">
            <button class="conversation-open-button" @click="emit('open-conversation', conversation.id)">
              <strong>{{ conversation.title }}</strong>
            </button>
            <div class="action-group">
              <button class="message-action-button" :disabled="renamingConversationId === conversation.id" @click="emit('rename-conversation', conversation.id, conversation.title)">
                {{ renamingConversationId === conversation.id ? "重命名中" : "重命名" }}
              </button>
              <button class="message-action-button destructive" :disabled="deletingConversationId === conversation.id" @click="emit('delete-conversation', conversation.id)">
                {{ deletingConversationId === conversation.id ? "删除中" : "删除" }}
              </button>
            </div>
          </div>
          <span>{{ conversation.lastMessagePreview || "暂无消息" }}</span>
          <small>{{ conversation.updatedAt }}</small>
        </div>
      </div>
    </div>

    <div v-if="currentUser" class="sidebar-card">
      <label class="stacked-field">
        <span>默认模型</span>
        <select v-model="currentUser.preferredModel">
          <option v-for="model in availableModels" :key="model.name" :value="model.name">{{ model.name }}</option>
        </select>
      </label>
      <button class="secondary-button" @click="emit('save-model')">保存模型配置</button>
    </div>

    <button class="ghost-button" @click="emit('logout')">退出登录</button>
  </aside>
</template>
