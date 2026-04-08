<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from "vue";
import AccountWorkspace from "./components/AccountWorkspace.vue";
import AdminWorkspace from "./components/AdminWorkspace.vue";
import AuthScreen from "./components/AuthScreen.vue";
import ChatWorkspace from "./components/ChatWorkspace.vue";
import WorkspaceSidebar from "./components/WorkspaceSidebar.vue";
import { useAdmin } from "./composables/useAdmin";
import { useAuth } from "./composables/useAuth";
import { useChatWorkspace } from "./composables/useChatWorkspace";
import type { AppView } from "./types/app";

const activeView = ref<AppView>("chat");
const notice = ref("");
const sidebarCollapsed = ref(false);
let noticeTimer: number | null = null;

const auth = useAuth();

function clearNotice() {
  notice.value = "";
  if (noticeTimer !== null) {
    window.clearTimeout(noticeTimer);
    noticeTimer = null;
  }
}

function showNotice(message: string) {
  if (!message) {
    clearNotice();
    return;
  }
  if (noticeTimer !== null) {
    window.clearTimeout(noticeTimer);
  }
  notice.value = "";
  requestAnimationFrame(() => {
    notice.value = message;
    noticeTimer = window.setTimeout(() => {
      clearNotice();
    }, 4000);
  });
}

function handleLogout() {
  workspace.resetWorkspace();
  auth.logout();
  clearNotice();
  activeView.value = "chat";
}

const admin = useAdmin(auth.isAdmin);
const workspace = useChatWorkspace({
  token: auth.token,
  isAuthenticated: auth.isAuthenticated,
  activeView,
  showNotice,
  refreshUserData: auth.refreshUserData,
  onUnauthorized: handleLogout
});

async function initializeWorkspace() {
  await workspace.bootstrapWorkspace();
}

async function handleLogin() {
  await auth.login();
  activeView.value = auth.isAdmin.value ? "admin" : "chat";
  await initializeWorkspace();
  if (auth.isAdmin.value && activeView.value === "admin") {
    await admin.refreshAdminPanel();
  }
}

async function handleRegister() {
  await auth.register();
  activeView.value = "chat";
  await initializeWorkspace();
}

async function handleSavePreferredModel() {
  try {
    await auth.savePreferredModel();
    showNotice("默认模型已更新。");
  } catch (error: any) {
    showNotice(error?.response?.data?.message ?? "模型更新失败。");
  }
}

async function handleAdminTrendDaysChange(value: number) {
  admin.adminTrendDays.value = value;
  await admin.refreshAdminPanel();
}

watch(activeView, async (view) => {
  if (view === "admin" && auth.isAdmin.value) {
    await admin.refreshAdminPanel();
  }
});

onMounted(async () => {
  if (!auth.token.value) return;
  try {
    await auth.refreshUserData();
    await initializeWorkspace();
  } catch (error: any) {
    if (error?.response?.status === 401) {
      handleLogout();
    }
  }
});

onBeforeUnmount(() => {
  if (noticeTimer !== null) {
    window.clearTimeout(noticeTimer);
  }
});
</script>

<template>
  <div class="app-shell">
    <AuthScreen
      v-if="!auth.isAuthenticated.value"
      :auth-mode="auth.authMode.value"
      :auth-loading="auth.authLoading.value"
      :auth-error="auth.authError.value"
      :login-form="auth.loginForm.value"
      :register-form="auth.registerForm.value"
      @switch-mode="auth.authMode.value = $event"
      @login="handleLogin"
      @register="handleRegister"
    />

    <section v-else :class="['workspace-shell', `workspace-shell--${activeView}`, { 'sidebar-collapsed': sidebarCollapsed }]">
      <WorkspaceSidebar
        :collapsed="sidebarCollapsed"
        :active-view="activeView"
        :is-admin="auth.isAdmin.value"
        :current-user="auth.currentUser.value"
        :available-models="auth.availableModels.value"
        :conversations="workspace.conversations.value"
        :current-conversation-id="workspace.currentConversationId.value"
        :creating-conversation="workspace.creatingConversation.value"
        :conversations-loading="workspace.conversationsLoading.value"
        :renaming-conversation-id="workspace.renamingConversationId.value"
        :deleting-conversation-id="workspace.deletingConversationId.value"
        @change-view="activeView = $event"
        @create-conversation="workspace.createConversation(true)"
        @refresh-conversations="workspace.loadConversations()"
        @open-conversation="workspace.openConversation($event)"
        @rename-conversation="workspace.renameConversation"
        @delete-conversation="workspace.deleteConversation"
        @save-model="handleSavePreferredModel"
        @logout="handleLogout"
        @toggle-collapse="sidebarCollapsed = !sidebarCollapsed"
      />

      <main :class="['workspace-main', `workspace-main--${activeView}`]">
        <div v-if="notice" class="notice-banner">
          <span>{{ notice }}</span>
          <button class="notice-close-button" aria-label="关闭提示" @click="clearNotice">×</button>
        </div>

        <ChatWorkspace
          v-if="activeView === 'chat'"
          :current-conversation-id="workspace.currentConversationId.value"
          :current-conversation-title="workspace.currentConversationTitle.value"
          :creating-conversation="workspace.creatingConversation.value"
          :renaming-conversation="workspace.renamingConversationId.value === workspace.currentConversationId.value"
          :conversation-loading="workspace.conversationLoading.value"
          :messages="workspace.messages.value"
          :input="workspace.input.value"
          :is-streaming="workspace.isStreaming.value"
          :copied-message-id="workspace.copiedMessageId.value"
          :deleting-message-id="workspace.deletingMessageId.value"
          :editing-message-id="workspace.editingMessageId.value"
          :usage-summary="auth.usageSummary.value"
          :current-user="auth.currentUser.value"
          :chat-body-ref="workspace.chatBodyRef"
          @update:input="workspace.input.value = $event"
          @create-conversation="workspace.createConversation(true)"
          @rename-current="workspace.renameConversation(workspace.currentConversationId.value!, workspace.currentConversationTitle.value)"
          @send-message="workspace.sendMessage()"
          @copy-message="workspace.copyMessage"
          @delete-message="workspace.deleteMessage"
          @regenerate-message="workspace.regenerateMessage"
        />

        <AccountWorkspace
          v-else-if="activeView === 'account'"
          :current-user="auth.currentUser.value"
          :usage-summary="auth.usageSummary.value"
        />

        <AdminWorkspace
          v-else
          :admin-dashboard="admin.adminDashboard.value"
          :admin-users="admin.adminUsers.value"
          :admin-trend="admin.adminTrend.value"
          :admin-trend-days="admin.adminTrendDays.value"
          :available-models="auth.availableModels.value"
          :trend-max="admin.trendMax.value"
          :user-filters="admin.userFilters.value"
          :audit-logs="admin.auditLogs.value"
          :audit-filters="admin.auditFilters.value"
          :subscription-templates="admin.subscriptionTemplates.value"
          :quota-saving-user-id="admin.quotaSavingUserId.value"
          :role-saving-user-id="admin.roleSavingUserId.value"
          :status-saving-user-id="admin.statusSavingUserId.value"
          :tier-saving-user-id="admin.tierSavingUserId.value"
          :model-quota-saving-user-id="admin.modelQuotaSavingUserId.value"
          @update:admin-trend-days="handleAdminTrendDaysChange"
          @refresh="admin.refreshAdminPanel()"
          @update-quota="admin.updateUserQuota"
          @update-role="admin.updateUserRole"
          @update-status="admin.updateUserStatus"
          @update-tier="admin.updateUserSubscriptionTier"
          @update-model-quota="admin.updateUserModelQuota($event.user, $event.modelName, $event.dailyQuota)"
        />
      </main>
    </section>
  </div>
</template>
