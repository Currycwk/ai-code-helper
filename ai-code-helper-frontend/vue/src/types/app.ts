export type UserRole = "USER" | "ADMIN";
export type SubscriptionTier = "FREE" | "PRO" | "ENTERPRISE";
export type AppView = "chat" | "account" | "admin";
export type AuthMode = "login" | "register";
export type ChatRole = "user" | "assistant";
export type ConversationMessageRole = "USER" | "ASSISTANT" | "SYSTEM";

export interface AuthResponse {
  token: string;
  user: UserProfileResponse;
}

export interface UserProfileResponse {
  id: number;
  username: string;
  displayName: string;
  role: UserRole;
  subscriptionTier: SubscriptionTier;
  enabled: boolean;
  dailyRequestQuota: number;
  todayRequestCount: number;
  remainingQuota: number;
  preferredModel: string;
  modelQuotas: UserModelQuotaResponse[];
}

export interface ModelSummaryResponse {
  name: string;
  supportsSearch: boolean;
  isDefault: boolean;
}

export interface RecentCallItem {
  modelName: string;
  memoryId: number;
  conversationTitle?: string;
  requestLength: number;
  responseLength: number;
  latencyMs: number;
  success: boolean;
  errorMessage?: string;
  createdAt: string;
}

export interface UserUsageSummaryResponse {
  totalRequests: number;
  totalPromptChars: number;
  totalCompletionChars: number;
  recentCalls: RecentCallItem[];
}

export interface AdminDashboardModelUsageItem {
  modelName: string;
  requestCount: number;
}

export interface AdminDashboardResponse {
  totalUsers: number;
  todayRequests: number;
  todaySuccessRequests: number;
  modelUsage: AdminDashboardModelUsageItem[];
}

export interface UsageTrendPointResponse {
  date: string;
  requestCount: number;
}

export interface UserModelQuotaResponse {
  modelName: string;
  dailyQuota: number;
}

export interface AuditLogResponse {
  id: number;
  userId: number;
  username: string;
  modelName: string;
  memoryId: number;
  conversationTitle?: string;
  requestPreview: string;
  responsePreview: string;
  requestLength: number;
  responseLength: number;
  latencyMs: number;
  success: boolean;
  errorMessage?: string;
  createdAt: string;
}

export interface SubscriptionTemplateResponse {
  subscriptionTier: SubscriptionTier;
  dailyRequestQuota: number;
  modelQuotas: UserModelQuotaResponse[];
}

export interface ConversationSummaryResponse {
  id: number;
  title: string;
  lastMessagePreview: string;
  messageCount: number;
  updatedAt: string;
}

export interface ConversationMessageResponse {
  id: number;
  role: ConversationMessageRole;
  content: string;
  modelName?: string;
  createdAt: string;
}

export interface ConversationDetailResponse {
  id: number;
  title: string;
  updatedAt: string;
  messages: ConversationMessageResponse[];
}

export interface SseChatMeta {
  model?: string;
  elapsedMs?: number;
}

export interface SseChatEvent {
  type: "delta" | "done" | "error";
  delta?: string;
  error?: string;
  meta?: SseChatMeta;
}

export interface ChatMessage {
  id: number | string;
  role: ChatRole;
  content: string;
  createdAt?: string;
  meta?: SseChatMeta;
  streaming?: boolean;
}
