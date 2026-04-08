<script setup lang="ts">
import { ref } from "vue";
import type { ChatMessage, UserProfileResponse, UserUsageSummaryResponse } from "../types/app";

defineProps<{
  currentConversationId: number | null;
  currentConversationTitle: string;
  creatingConversation: boolean;
  renamingConversation: boolean;
  conversationLoading: boolean;
  messages: ChatMessage[];
  input: string;
  isStreaming: boolean;
  copiedMessageId: number | string | null;
  deletingMessageId: number | string | null;
  editingMessageId: number | string | null;
  usageSummary: UserUsageSummaryResponse | null;
  currentUser: UserProfileResponse | null;
  chatBodyRef?: any;
}>();

const emit = defineEmits<{
  (e: "update:input", value: string): void;
  (e: "create-conversation"): void;
  (e: "rename-current"): void;
  (e: "send-message"): void;
  (e: "copy-message", message: ChatMessage): void;
  (e: "delete-message", id: number | string): void;
  (e: "regenerate-message", message: ChatMessage): void;
}>();

const statsCollapsed = ref(false);
const recentCollapsed = ref(false);
</script>

<template>
  <section class="chat-layout">
    <header class="panel-header">
      <div>
        <h1>对话工作台</h1>
        <p>当前会话：{{ currentConversationTitle }}<span v-if="currentConversationId"> | #{{ currentConversationId }}</span></p>
      </div>
      <div class="action-group">
        <button v-if="currentConversationId" class="ghost-button" :disabled="renamingConversation" @click="emit('rename-current')">
          {{ renamingConversation ? "重命名中" : "重命名" }}
        </button>
        <button class="secondary-button" :disabled="creatingConversation" @click="emit('create-conversation')">
          {{ creatingConversation ? "创建中..." : "新会话" }}
        </button>
      </div>
    </header>

    <div class="panel-grid">
      <section class="panel surface chat-surface">
        <div :ref="chatBodyRef" class="chat-body">
          <div v-if="conversationLoading" class="empty-state">正在加载会话详情...</div>
          <div v-else-if="!currentConversationId" class="empty-state">先创建一个会话，再开始对话。</div>
          <div v-else-if="messages.length === 0" class="empty-state">这是一个新的会话，发送第一条消息开始对话。</div>
          <div v-for="message in messages" :key="message.id" class="message" :class="message.role">
            <div v-if="message.role === 'assistant' && message.meta" class="message-meta">
              <span v-if="message.meta.model">模型：{{ message.meta.model }}</span>
              <span v-if="message.meta.elapsedMs != null">耗时：{{ message.meta.elapsedMs }} ms</span>
              <span v-if="message.createdAt">时间：{{ message.createdAt }}</span>
            </div>
            <div class="message-actions">
              <button v-if="message.role === 'user'" class="message-action-button" :disabled="editingMessageId === message.id || isStreaming" @click="emit('regenerate-message', message)">
                {{ editingMessageId === message.id ? "处理中" : "编辑后重生成" }}
              </button>
              <button v-if="message.role === 'assistant'" class="message-action-button" @click="emit('copy-message', message)">
                {{ copiedMessageId === message.id ? "已复制" : "复制" }}
              </button>
              <button v-if="typeof message.id === 'number'" class="message-action-button destructive" :disabled="deletingMessageId === message.id" @click="emit('delete-message', message.id)">
                {{ deletingMessageId === message.id ? "删除中" : "删除" }}
              </button>
            </div>
            {{ message.content }}
            <span v-if="message.streaming">▋</span>
          </div>
        </div>
        <div class="chat-footer">
          <div class="input-row">
            <textarea
              :value="input"
              rows="3"
              placeholder="输入你的编程学习、求职或代码问题..."
              @input="emit('update:input', ($event.target as HTMLTextAreaElement).value)"
              @keydown.enter.prevent="emit('send-message')"
            />
            <button class="primary-button" :disabled="isStreaming" @click="emit('send-message')">
              {{ isStreaming ? "生成中..." : "发送" }}
            </button>
          </div>
        </div>
      </section>

      <section class="panel side-column">
        <div :class="['surface stats-card', { collapsed: statsCollapsed }]">
          <div class="card-header">
            <h3>个人调用统计</h3>
            <button class="card-collapse-button" :class="{ collapsed: statsCollapsed }" :title="statsCollapsed ? '展开个人调用统计' : '收起个人调用统计'" @click="statsCollapsed = !statsCollapsed">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="m8 10 4 4 4-4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </div>
          <div v-if="!statsCollapsed" class="stats-grid">
            <div><span>累计请求</span><strong>{{ usageSummary?.totalRequests ?? 0 }}</strong></div>
            <div><span>输入字符</span><strong>{{ usageSummary?.totalPromptChars ?? 0 }}</strong></div>
            <div><span>输出字符</span><strong>{{ usageSummary?.totalCompletionChars ?? 0 }}</strong></div>
            <div><span>当前模型</span><strong>{{ currentUser?.preferredModel ?? "-" }}</strong></div>
          </div>
        </div>
        <div :class="['surface recent-card', { collapsed: recentCollapsed }]">
          <div class="card-header">
            <h3>最近调用</h3>
            <button class="card-collapse-button" :class="{ collapsed: recentCollapsed }" :title="recentCollapsed ? '展开最近调用' : '收起最近调用'" @click="recentCollapsed = !recentCollapsed">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="m8 10 4 4 4-4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </div>
          <div v-if="!recentCollapsed && !usageSummary?.recentCalls?.length" class="empty-inline">暂无调用记录</div>
          <div v-else-if="!recentCollapsed" class="recent-list">
            <div v-for="item in usageSummary?.recentCalls ?? []" :key="`${item.createdAt}-${item.memoryId}`" class="recent-item">
              <div class="recent-row">
                <strong>{{ item.modelName }}</strong>
                <span>{{ item.latencyMs }} ms</span>
              </div>
              <div class="recent-row subtle">
                <span>{{ item.conversationTitle || `会话 ${item.memoryId}` }}</span>
                <span>{{ item.success ? "成功" : "失败" }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  </section>
</template>
