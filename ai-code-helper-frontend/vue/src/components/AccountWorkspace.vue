<script setup lang="ts">
import type { UserProfileResponse, UserUsageSummaryResponse } from "../types/app";

defineProps<{
  currentUser: UserProfileResponse | null;
  usageSummary: UserUsageSummaryResponse | null;
}>();
</script>

<template>
  <section class="account-layout">
    <header class="panel-header">
      <div>
        <h1>账户详情</h1>
        <p>查看当前登录用户的基础资料、权限信息和配额状态。</p>
      </div>
    </header>

    <div class="account-grid">
      <section class="surface panel account-highlight">
        <h3>{{ currentUser?.displayName }}</h3>
        <p class="account-subtitle">{{ currentUser?.username }}</p>
        <div class="detail-list">
          <div class="detail-row"><span>用户 ID</span><strong>{{ currentUser?.id ?? "-" }}</strong></div>
          <div class="detail-row"><span>角色</span><strong>{{ currentUser?.role ?? "-" }}</strong></div>
          <div class="detail-row"><span>账号状态</span><strong>{{ currentUser?.enabled ? "启用" : "禁用" }}</strong></div>
          <div class="detail-row"><span>套餐等级</span><strong>{{ currentUser?.subscriptionTier ?? "-" }}</strong></div>
          <div class="detail-row"><span>默认模型</span><strong>{{ currentUser?.preferredModel ?? "-" }}</strong></div>
        </div>
      </section>

      <section class="surface panel">
        <h3>配额信息</h3>
        <div class="stats-grid">
          <div><span>每日总配额</span><strong>{{ currentUser?.dailyRequestQuota ?? 0 }}</strong></div>
          <div><span>今日已用</span><strong>{{ currentUser?.todayRequestCount ?? 0 }}</strong></div>
          <div><span>剩余配额</span><strong>{{ currentUser?.remainingQuota ?? 0 }}</strong></div>
          <div><span>累计请求</span><strong>{{ usageSummary?.totalRequests ?? 0 }}</strong></div>
        </div>
      </section>

      <section class="surface panel">
        <h3>模型配额</h3>
        <div v-if="currentUser?.modelQuotas?.length" class="usage-card">
          <div v-for="quota in currentUser.modelQuotas" :key="quota.modelName" class="usage-item">
            <span>{{ quota.modelName }}</span>
            <strong>{{ quota.dailyQuota }}</strong>
          </div>
        </div>
        <div v-else class="empty-inline">暂无单独模型配额，当前按总配额限制。</div>
      </section>

      <section class="surface panel">
        <h3>字符统计</h3>
        <div class="stats-grid">
          <div><span>输入字符</span><strong>{{ usageSummary?.totalPromptChars ?? 0 }}</strong></div>
          <div><span>输出字符</span><strong>{{ usageSummary?.totalCompletionChars ?? 0 }}</strong></div>
        </div>
      </section>
    </div>
  </section>
</template>
