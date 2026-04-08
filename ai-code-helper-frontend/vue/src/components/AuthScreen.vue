<script setup lang="ts">
import type { AuthMode } from "../types/app";

defineProps<{
  authMode: AuthMode;
  authLoading: boolean;
  authError: string;
  loginForm: { username: string; password: string };
  registerForm: { username: string; displayName: string; password: string };
}>();

const emit = defineEmits<{
  (e: "switch-mode", mode: AuthMode): void;
  (e: "login"): void;
  (e: "register"): void;
}>();
</script>

<template>
  <section class="auth-shell">
    <div class="auth-hero">
      <div class="auth-hero-copy">
        <div>
          <p class="eyebrow">AI Code Helper</p>
          <h1>把会话、配额、模型和后台管理放进一个轻量工作台。</h1>
          <p class="hero-copy">
            登录后可以继续历史对话、查看账户详情、切换默认模型，也可以在管理员后台管理用户、套餐、配额和审计日志。
          </p>
        </div>

        <div class="hero-card">
          <div class="hero-stat">
            <strong>接口文档</strong>
            <span>http://localhost:8081/api/doc.html</span>
          </div>
          <div class="hero-stat">
            <strong>默认管理员</strong>
            <span>admin / admin123456</span>
          </div>
        </div>
      </div>

      <div class="hero-visual" aria-hidden="true">
        <div class="hero-visual-board">
          <div class="hero-board-head">
            <span></span>
            <span></span>
            <span></span>
          </div>
          <div class="hero-board-chart">
            <i></i>
            <i></i>
            <i></i>
            <i></i>
            <i></i>
            <i></i>
            <i></i>
          </div>
        </div>

        <div class="hero-float hero-float-main">
          <small>Conversation Memory</small>
          <strong>10 条上下文</strong>
          <span>持续会话 / 历史恢复 / 多端同步</span>
        </div>

        <div class="hero-float hero-float-side">
          <small>Admin View</small>
          <strong>用户 / 配额 / 审计</strong>
        </div>

        <div class="hero-dots">
          <span></span>
          <span></span>
          <span></span>
          <span></span>
          <span></span>
          <span></span>
          <span></span>
          <span></span>
          <span></span>
        </div>
      </div>
    </div>

    <div class="auth-panel">
      <div class="panel-header auth-panel-header">
        <div>
          <h1>{{ authMode === "login" ? "登录账户" : "创建账户" }}</h1>
          <p>{{ authMode === "login" ? "输入用户名和密码后进入工作台。" : "注册后会直接登录到系统。" }}</p>
        </div>
      </div>

      <div class="auth-tabs">
        <button :class="{ active: authMode === 'login' }" type="button" @click="emit('switch-mode', 'login')">登录</button>
        <button :class="{ active: authMode === 'register' }" type="button" @click="emit('switch-mode', 'register')">注册</button>
      </div>

      <form v-if="authMode === 'login'" class="auth-form" @submit.prevent="emit('login')">
        <label>
          <span>用户名</span>
          <input v-model="loginForm.username" type="text" placeholder="请输入用户名" />
        </label>
        <label>
          <span>密码</span>
          <input v-model="loginForm.password" type="password" placeholder="请输入密码" />
        </label>
        <button class="primary-button" :disabled="authLoading">{{ authLoading ? "登录中..." : "登录" }}</button>
      </form>

      <form v-else class="auth-form" @submit.prevent="emit('register')">
        <label>
          <span>用户名</span>
          <input v-model="registerForm.username" type="text" placeholder="请输入用户名" />
        </label>
        <label>
          <span>显示名称</span>
          <input v-model="registerForm.displayName" type="text" placeholder="请输入显示名称" />
        </label>
        <label>
          <span>密码</span>
          <input v-model="registerForm.password" type="password" placeholder="请输入密码" />
        </label>
        <button class="primary-button" :disabled="authLoading">{{ authLoading ? "注册中..." : "注册并登录" }}</button>
      </form>

      <p v-if="authError" class="error-banner">{{ authError }}</p>
    </div>
  </section>
</template>
