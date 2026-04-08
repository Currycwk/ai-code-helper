import { computed, ref, type ComputedRef } from "vue";
import apiClient from "../api/client";
import type {
  AdminDashboardResponse,
  AuditLogResponse,
  SubscriptionTemplateResponse,
  UsageTrendPointResponse,
  UserModelQuotaResponse,
  UserProfileResponse
} from "../types/app";

export function useAdmin(isAdmin: ComputedRef<boolean>) {
  const adminDashboard = ref<AdminDashboardResponse | null>(null);
  const adminUsers = ref<UserProfileResponse[]>([]);
  const adminTrend = ref<UsageTrendPointResponse[]>([]);
  const adminTrendDays = ref(7);
  const userFilters = ref({
    userId: ""
  });
  const auditLogs = ref<AuditLogResponse[]>([]);
  const subscriptionTemplates = ref<SubscriptionTemplateResponse[]>([]);
  const auditFilters = ref({
    userId: "",
    modelName: "",
    success: "",
    start: "",
    end: ""
  });
  const quotaSavingUserId = ref<number | null>(null);
  const roleSavingUserId = ref<number | null>(null);
  const statusSavingUserId = ref<number | null>(null);
  const tierSavingUserId = ref<number | null>(null);
  const modelQuotaSavingUserId = ref<number | null>(null);
  const trendMax = computed(() => Math.max(1, ...adminTrend.value.map((item) => item.requestCount)));

  function buildUserParams() {
    const userParams: Record<string, number> = {};
    if (userFilters.value.userId) userParams.userId = Number(userFilters.value.userId);
    return userParams;
  }

  function buildAuditParams() {
    const params: Record<string, string | number | boolean> = {};
    if (auditFilters.value.userId) params.userId = Number(auditFilters.value.userId);
    if (auditFilters.value.modelName) params.modelName = auditFilters.value.modelName;
    if (auditFilters.value.success) params.success = auditFilters.value.success === "true";
    if (auditFilters.value.start) params.start = auditFilters.value.start;
    if (auditFilters.value.end) params.end = auditFilters.value.end;
    return params;
  }

  async function refreshDashboard() {
    if (!isAdmin.value) return;
    const { data } = await apiClient.get<AdminDashboardResponse>("/admin/dashboard");
    adminDashboard.value = data;
  }

  async function refreshUsers() {
    if (!isAdmin.value) return;
    const { data } = await apiClient.get<UserProfileResponse[]>("/admin/users", { params: buildUserParams() });
    adminUsers.value = data;
  }

  async function refreshTrend() {
    if (!isAdmin.value) return;
    const { data } = await apiClient.get<UsageTrendPointResponse[]>("/admin/usage/trend", { params: { days: adminTrendDays.value } });
    adminTrend.value = data;
  }

  async function refreshAuditLogs() {
    if (!isAdmin.value) return;
    const { data } = await apiClient.get<AuditLogResponse[]>("/admin/audit-logs", { params: buildAuditParams() });
    auditLogs.value = data;
  }

  async function refreshTemplates() {
    if (!isAdmin.value) return;
    const { data } = await apiClient.get<SubscriptionTemplateResponse[]>("/admin/subscription-templates");
    subscriptionTemplates.value = data;
  }

  async function refreshAdminPanel() {
    if (!isAdmin.value) return;

    await Promise.allSettled([
      refreshDashboard(),
      refreshUsers(),
      refreshTrend(),
      refreshAuditLogs(),
      refreshTemplates()
    ]);
  }

  async function updateUserQuota(user: UserProfileResponse) {
    quotaSavingUserId.value = user.id;
    try {
      await apiClient.patch(`/admin/users/${user.id}/quota`, { dailyRequestQuota: user.dailyRequestQuota });
      await refreshUsers();
    } finally {
      quotaSavingUserId.value = null;
    }
  }

  async function updateUserRole(user: UserProfileResponse) {
    roleSavingUserId.value = user.id;
    try {
      await apiClient.patch(`/admin/users/${user.id}/role`, { role: user.role });
      await refreshUsers();
    } finally {
      roleSavingUserId.value = null;
    }
  }

  async function updateUserStatus(user: UserProfileResponse) {
    statusSavingUserId.value = user.id;
    try {
      await apiClient.patch(`/admin/users/${user.id}/status`, { enabled: user.enabled });
      await refreshUsers();
    } finally {
      statusSavingUserId.value = null;
    }
  }

  async function updateUserSubscriptionTier(user: UserProfileResponse) {
    tierSavingUserId.value = user.id;
    try {
      await apiClient.patch(`/admin/users/${user.id}/subscription-tier`, { subscriptionTier: user.subscriptionTier });
      await refreshUsers();
    } finally {
      tierSavingUserId.value = null;
    }
  }

  async function updateUserModelQuota(user: UserProfileResponse, modelName: string, dailyQuota: number) {
    modelQuotaSavingUserId.value = user.id;
    try {
      const { data } = await apiClient.patch<UserModelQuotaResponse[]>(`/admin/users/${user.id}/model-quotas`, {
        modelName,
        dailyQuota
      });
      user.modelQuotas = data;
      await refreshUsers();
    } finally {
      modelQuotaSavingUserId.value = null;
    }
  }

  return {
    adminDashboard,
    adminTrend,
    adminTrendDays,
    adminUsers,
    auditLogs,
    auditFilters,
    modelQuotaSavingUserId,
    quotaSavingUserId,
    refreshAuditLogs,
    refreshDashboard,
    refreshAdminPanel,
    refreshTemplates,
    refreshTrend,
    refreshUsers,
    roleSavingUserId,
    statusSavingUserId,
    subscriptionTemplates,
    tierSavingUserId,
    trendMax,
    userFilters,
    updateUserModelQuota,
    updateUserQuota,
    updateUserRole,
    updateUserStatus,
    updateUserSubscriptionTier
  };
}
