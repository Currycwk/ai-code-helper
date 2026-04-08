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

  async function refreshAdminPanel() {
    if (!isAdmin.value) return;
    const params: Record<string, string | number | boolean> = {};
    const userParams: Record<string, number> = {};
    if (userFilters.value.userId) userParams.userId = Number(userFilters.value.userId);
    if (auditFilters.value.userId) params.userId = Number(auditFilters.value.userId);
    if (auditFilters.value.modelName) params.modelName = auditFilters.value.modelName;
    if (auditFilters.value.success) params.success = auditFilters.value.success === "true";
    if (auditFilters.value.start) params.start = auditFilters.value.start;
    if (auditFilters.value.end) params.end = auditFilters.value.end;

    const [dashboard, users, trend, logs, templates] = await Promise.allSettled([
      apiClient.get<AdminDashboardResponse>("/admin/dashboard"),
      apiClient.get<UserProfileResponse[]>("/admin/users", { params: userParams }),
      apiClient.get<UsageTrendPointResponse[]>("/admin/usage/trend", { params: { days: adminTrendDays.value } }),
      apiClient.get<AuditLogResponse[]>("/admin/audit-logs", { params }),
      apiClient.get<SubscriptionTemplateResponse[]>("/admin/subscription-templates")
    ]);

    if (dashboard.status === "fulfilled") {
      adminDashboard.value = dashboard.value.data;
    }
    if (users.status === "fulfilled") {
      adminUsers.value = users.value.data;
    }
    if (trend.status === "fulfilled") {
      adminTrend.value = trend.value.data;
    }
    if (logs.status === "fulfilled") {
      auditLogs.value = logs.value.data;
    }
    if (templates.status === "fulfilled") {
      subscriptionTemplates.value = templates.value.data;
    }
  }

  async function updateUserQuota(user: UserProfileResponse) {
    quotaSavingUserId.value = user.id;
    try {
      await apiClient.patch(`/admin/users/${user.id}/quota`, { dailyRequestQuota: user.dailyRequestQuota });
      await refreshAdminPanel();
    } finally {
      quotaSavingUserId.value = null;
    }
  }

  async function updateUserRole(user: UserProfileResponse) {
    roleSavingUserId.value = user.id;
    try {
      await apiClient.patch(`/admin/users/${user.id}/role`, { role: user.role });
      await refreshAdminPanel();
    } finally {
      roleSavingUserId.value = null;
    }
  }

  async function updateUserStatus(user: UserProfileResponse) {
    statusSavingUserId.value = user.id;
    try {
      await apiClient.patch(`/admin/users/${user.id}/status`, { enabled: user.enabled });
      await refreshAdminPanel();
    } finally {
      statusSavingUserId.value = null;
    }
  }

  async function updateUserSubscriptionTier(user: UserProfileResponse) {
    tierSavingUserId.value = user.id;
    try {
      await apiClient.patch(`/admin/users/${user.id}/subscription-tier`, { subscriptionTier: user.subscriptionTier });
      await refreshAdminPanel();
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
      await refreshAdminPanel();
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
    refreshAdminPanel,
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
