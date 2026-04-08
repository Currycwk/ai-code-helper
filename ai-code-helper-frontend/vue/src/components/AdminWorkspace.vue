<script setup lang="ts">
import type {
  AdminDashboardResponse,
  AuditLogResponse,
  ModelSummaryResponse,
  SubscriptionTemplateResponse,
  UsageTrendPointResponse,
  UserProfileResponse
} from "../types/app";

defineProps<{
  adminDashboard: AdminDashboardResponse | null;
  adminUsers: UserProfileResponse[];
  adminTrend: UsageTrendPointResponse[];
  adminTrendDays: number;
  availableModels: ModelSummaryResponse[];
  trendMax: number;
  userFilters: {
    userId: string;
  };
  auditLogs: AuditLogResponse[];
  auditFilters: {
    userId: string;
    modelName: string;
    success: string;
    start: string;
    end: string;
  };
  subscriptionTemplates: SubscriptionTemplateResponse[];
  quotaSavingUserId: number | null;
  roleSavingUserId: number | null;
  statusSavingUserId: number | null;
  tierSavingUserId: number | null;
  modelQuotaSavingUserId: number | null;
}>();

const emit = defineEmits<{
  (e: "update:adminTrendDays", value: number): void;
  (e: "refresh"): void;
  (e: "update-quota", user: UserProfileResponse): void;
  (e: "update-role", user: UserProfileResponse): void;
  (e: "update-status", user: UserProfileResponse): void;
  (e: "update-tier", user: UserProfileResponse): void;
  (e: "update-model-quota", payload: { user: UserProfileResponse; modelName: string; dailyQuota: number }): void;
}>();
</script>

<template>
  <section class="admin-layout">
    <header class="panel-header">
      <div>
        <h1>管理员后台</h1>
        <p>管理用户、套餐模板、配额和调用审计日志。</p>
      </div>
      <div class="toolbar">
        <label class="stacked-field compact">
          <span>趋势天数</span>
          <select :value="adminTrendDays" @change="emit('update:adminTrendDays', Number(($event.target as HTMLSelectElement).value))">
            <option :value="7">7 天</option>
            <option :value="14">14 天</option>
            <option :value="30">30 天</option>
          </select>
        </label>
        <button class="secondary-button" @click="emit('refresh')">刷新后台数据</button>
      </div>
    </header>

    <div class="admin-kpi-strip">
      <div class="kpi-tile">
        <span>总用户数</span>
        <strong>{{ adminDashboard?.totalUsers ?? 0 }}</strong>
      </div>
      <div class="kpi-tile">
        <span>今日请求</span>
        <strong>{{ adminDashboard?.todayRequests ?? 0 }}</strong>
      </div>
      <div class="kpi-tile">
        <span>今日成功</span>
        <strong>{{ adminDashboard?.todaySuccessRequests ?? 0 }}</strong>
      </div>
      <div class="kpi-tile">
        <span>可选模型</span>
        <strong>{{ availableModels.length }}</strong>
      </div>
    </div>

    <div class="admin-grid">
      <section class="surface overview-card">
        <div class="table-header">
          <h3>套餐默认模板</h3>
          <span>按套餐自动套用</span>
        </div>
        <div class="template-grid">
          <div v-for="template in subscriptionTemplates" :key="template.subscriptionTier" class="template-card">
            <div class="recent-row">
              <strong>{{ template.subscriptionTier }}</strong>
              <span>总配额 {{ template.dailyRequestQuota }}</span>
            </div>
            <div class="template-quotas">
              <span v-for="quota in template.modelQuotas" :key="`${template.subscriptionTier}-${quota.modelName}`" class="model-pill">
                {{ quota.modelName }}: {{ quota.dailyQuota }}
              </span>
            </div>
          </div>
        </div>
      </section>

      <section class="surface trend-card">
        <div class="table-header">
          <h3>调用趋势</h3>
          <span>最近 {{ adminTrendDays }} 天</span>
        </div>
        <div v-for="point in adminTrend" :key="point.date" class="trend-row">
          <span>{{ point.date }}</span>
          <div class="trend-bar-track">
            <div class="trend-bar-fill" :style="{ width: `${(point.requestCount / trendMax) * 100}%` }" />
          </div>
          <strong>{{ point.requestCount }}</strong>
        </div>
      </section>

      <section class="surface usage-card">
        <div class="table-header">
          <h3>模型使用排行</h3>
          <span>今日模型热度</span>
        </div>
        <div v-for="item in adminDashboard?.modelUsage ?? []" :key="item.modelName" class="usage-item">
          <span>{{ item.modelName }}</span>
          <strong>{{ item.requestCount }}</strong>
        </div>
      </section>

      <section class="surface table-card">
        <div class="table-header">
          <h3>用户列表</h3>
          <span>{{ adminUsers.length }} 位用户</span>
        </div>
        <div class="audit-filter-bar user-filter-bar">
          <input v-model="userFilters.userId" type="number" min="1" placeholder="按用户 ID 筛选" />
          <button class="secondary-button small" @click="emit('refresh')">筛选用户</button>
        </div>
        <div class="user-table">
          <div class="user-table-head admin-user-head">
            <span>用户 ID</span>
            <span>用户</span>
            <span>角色</span>
            <span>状态 / 套餐</span>
            <span>今日 / 总配额</span>
            <span>默认模型</span>
            <span>模型配额</span>
            <span>操作</span>
          </div>
          <div v-for="user in adminUsers" :key="user.id" class="user-table-row">
            <div><strong>#{{ user.id }}</strong></div>
            <div>
              <strong>{{ user.displayName }}</strong>
              <p>{{ user.username }}</p>
            </div>
            <div>
              <select v-model="user.role">
                <option value="USER">USER</option>
                <option value="ADMIN">ADMIN</option>
              </select>
            </div>
            <div class="quota-editor">
              <label><input v-model="user.enabled" type="checkbox" /> 启用</label>
              <select v-model="user.subscriptionTier">
                <option value="FREE">FREE</option>
                <option value="PRO">PRO</option>
                <option value="ENTERPRISE">ENTERPRISE</option>
              </select>
            </div>
            <div class="quota-editor">
              <input v-model.number="user.dailyRequestQuota" type="number" min="1" max="10000" />
              <small>{{ user.todayRequestCount }} / {{ user.remainingQuota }} 剩余</small>
            </div>
            <div><span class="model-pill">{{ user.preferredModel }}</span></div>
            <div class="quota-editor model-quota-list">
              <label v-for="model in availableModels" :key="`${user.id}-${model.name}`" class="model-quota-item">
                <span>{{ model.name }}</span>
                <input
                  :value="user.modelQuotas.find((item) => item.modelName === model.name)?.dailyQuota ?? ''"
                  type="number"
                  min="0"
                  placeholder="继承总配额"
                  @change="emit('update-model-quota', { user, modelName: model.name, dailyQuota: Number(($event.target as HTMLInputElement).value || 0) })"
                />
              </label>
            </div>
            <div class="action-group">
              <button class="secondary-button small" :disabled="quotaSavingUserId === user.id" @click="emit('update-quota', user)">保存配额</button>
              <button class="ghost-button small" :disabled="roleSavingUserId === user.id" @click="emit('update-role', user)">保存角色</button>
              <button class="ghost-button small" :disabled="statusSavingUserId === user.id" @click="emit('update-status', user)">保存状态</button>
              <button class="ghost-button small" :disabled="tierSavingUserId === user.id" @click="emit('update-tier', user)">应用套餐模板</button>
            </div>
          </div>
        </div>
      </section>

      <section class="surface table-card">
        <div class="table-header">
          <h3>调用审计日志</h3>
          <span>{{ auditLogs.length }} 条</span>
        </div>
        <div class="audit-filter-bar">
          <input v-model="auditFilters.userId" type="number" min="1" placeholder="用户 ID" />
          <select v-model="auditFilters.modelName">
            <option value="">全部模型</option>
            <option v-for="model in availableModels" :key="`filter-${model.name}`" :value="model.name">{{ model.name }}</option>
          </select>
          <select v-model="auditFilters.success">
            <option value="">全部结果</option>
            <option value="true">成功</option>
            <option value="false">失败</option>
          </select>
          <input v-model="auditFilters.start" type="datetime-local" />
          <input v-model="auditFilters.end" type="datetime-local" />
          <button class="secondary-button small" @click="emit('refresh')">筛选</button>
        </div>
        <div class="audit-log-list">
          <div v-for="log in auditLogs" :key="log.id" class="audit-log-item">
            <div class="recent-row">
              <strong>{{ log.username }}</strong>
              <span>{{ log.modelName }} | {{ log.latencyMs }} ms | {{ log.success ? "成功" : "失败" }}</span>
            </div>
            <div class="recent-row subtle">
              <span>用户 {{ log.userId }} | {{ log.conversationTitle || `会话 ${log.memoryId ?? 0}` }} | {{ log.createdAt }}</span>
              <span>{{ log.requestLength }} / {{ log.responseLength }} 字符</span>
            </div>
            <p class="audit-preview">请求：{{ log.requestPreview || "-" }}</p>
            <p class="audit-preview">响应：{{ log.responsePreview || "-" }}</p>
            <p v-if="log.errorMessage" class="audit-error">错误：{{ log.errorMessage }}</p>
          </div>
        </div>
      </section>
    </div>
  </section>
</template>
