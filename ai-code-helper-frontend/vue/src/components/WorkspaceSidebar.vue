<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import type { AppView, ConversationSummaryResponse, ModelSummaryResponse, UserProfileResponse } from "../types/app";

const props = defineProps<{
  collapsed: boolean;
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
  (e: "toggle-collapse"): void;
}>();

const accountMenuOpen = ref(false);
const accountMenuRef = ref<HTMLElement | null>(null);

const displayName = computed(() => props.currentUser?.displayName || props.currentUser?.username || "用户");
const avatarLetter = computed(() => displayName.value.trim().charAt(0).toUpperCase() || "U");

function toggleAccountMenu() {
  accountMenuOpen.value = !accountMenuOpen.value;
}

function closeAccountMenu() {
  accountMenuOpen.value = false;
}

function handleDocumentClick(event: MouseEvent) {
  if (!accountMenuRef.value) return;
  if (accountMenuRef.value.contains(event.target as Node)) return;
  closeAccountMenu();
}

function openAccountView() {
  emit("change-view", "account");
  closeAccountMenu();
}

function logoutFromMenu() {
  emit("logout");
  closeAccountMenu();
}

onMounted(() => {
  document.addEventListener("click", handleDocumentClick);
});

onBeforeUnmount(() => {
  document.removeEventListener("click", handleDocumentClick);
});
</script>

<template>
  <aside :class="['sidebar', { collapsed }]">
    <div class="sidebar-brand">
      <div class="sidebar-brand-mark" aria-hidden="true">
        <span></span><span></span><span></span><span></span><span></span>
      </div>
      <div v-if="!collapsed">
        <p class="eyebrow">Workspace</p>
        <h2>AI Code Helper</h2>
        <p class="sidebar-subtitle">AI 学习、会话管理与后台运营工作台</p>
      </div>
    </div>

    <div class="sidebar-scroll-area">
      <nav class="nav-list">
        <button :class="{ active: activeView === 'chat' }" :title="collapsed ? '对话工作台' : undefined" @click="emit('change-view', 'chat')">
          <span class="nav-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M4 6.5A2.5 2.5 0 0 1 6.5 4h11A2.5 2.5 0 0 1 20 6.5v7A2.5 2.5 0 0 1 17.5 16h-6.3L7 19v-3H6.5A2.5 2.5 0 0 1 4 13.5v-7Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>
            </svg>
          </span>
          <span v-if="!collapsed">对话工作台</span>
        </button>
        <button v-if="isAdmin" :class="{ active: activeView === 'admin' }" :title="collapsed ? '管理员后台' : undefined" @click="emit('change-view', 'admin')">
          <span class="nav-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M4 5.5h7v6H4v-6Zm9 0h7v4h-7v-4ZM4 13.5h7v5H4v-5Zm9-2h7v7h-7v-7Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>
            </svg>
          </span>
          <span v-if="!collapsed">管理员后台</span>
        </button>
      </nav>

      <div v-if="!collapsed" class="sidebar-card">
        <div class="sidebar-stat"><span>今日已用</span><strong>{{ currentUser?.todayRequestCount ?? 0 }}</strong></div>
        <div class="sidebar-stat"><span>剩余配额</span><strong>{{ currentUser?.remainingQuota ?? 0 }}</strong></div>
      </div>

      <div v-if="!collapsed" class="sidebar-card conversation-card">
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

      <div v-if="currentUser && !collapsed" class="sidebar-card">
        <label class="stacked-field">
          <span>默认模型</span>
          <select v-model="currentUser.preferredModel">
            <option v-for="model in availableModels" :key="model.name" :value="model.name">{{ model.name }}</option>
          </select>
        </label>
        <button class="secondary-button" @click="emit('save-model')">保存模型配置</button>
      </div>
    </div>

    <button class="sidebar-collapse-toggle" :class="{ collapsed }" :title="collapsed ? '展开侧边栏' : '收起侧边栏'" @click="emit('toggle-collapse')">
      <svg viewBox="0 0 24 24" fill="none">
        <path d="m14.5 6-5 6 5 6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
    </button>

    <div ref="accountMenuRef" class="sidebar-account">
      <button class="sidebar-account-trigger" @click.stop="toggleAccountMenu">
        <span class="sidebar-account-avatar">{{ avatarLetter }}</span>
        <span v-if="!collapsed" class="sidebar-account-text">{{ displayName }}</span>
        <span v-if="!collapsed" class="sidebar-account-chevron" :class="{ open: accountMenuOpen }">⌃</span>
      </button>

      <div v-if="accountMenuOpen" class="sidebar-account-menu">
        <div class="sidebar-account-menu-header">
          <span class="sidebar-account-avatar large">{{ avatarLetter }}</span>
          <div>
            <strong>{{ displayName }}</strong>
            <p>{{ currentUser?.username }}</p>
          </div>
        </div>
        <button class="sidebar-account-menu-item" @click="openAccountView">账户详情</button>
        <button class="sidebar-account-menu-item logout" @click="logoutFromMenu">退出登录</button>
      </div>
    </div>
  </aside>
</template>
