import { computed, ref } from "vue";
import apiClient, { clearStoredToken, getStoredToken, setStoredToken } from "../api/client";
import type {
  AuthMode,
  AuthResponse,
  ModelSummaryResponse,
  UserProfileResponse,
  UserUsageSummaryResponse
} from "../types/app";

export function useAuth() {
  const authMode = ref<AuthMode>("login");
  const token = ref(getStoredToken());
  const currentUser = ref<UserProfileResponse | null>(null);
  const availableModels = ref<ModelSummaryResponse[]>([]);
  const usageSummary = ref<UserUsageSummaryResponse | null>(null);
  const authError = ref("");
  const authLoading = ref(false);

  const loginForm = ref({ username: "admin", password: "admin123456" });
  const registerForm = ref({ username: "", displayName: "", password: "" });

  const isAuthenticated = computed(() => Boolean(token.value && currentUser.value));
  const isAdmin = computed(() => currentUser.value?.role === "ADMIN");

  async function refreshProfile() {
    if (!token.value) return;
    const { data } = await apiClient.get<UserProfileResponse>("/auth/me");
    currentUser.value = data;
  }

  async function refreshModels() {
    if (!token.value) return;
    const { data } = await apiClient.get<ModelSummaryResponse[]>("/users/me/models");
    availableModels.value = data;
  }

  async function refreshUsageSummary() {
    if (!token.value) return;
    const { data } = await apiClient.get<UserUsageSummaryResponse>("/users/me/usage");
    usageSummary.value = data;
  }

  async function refreshUserData() {
    if (!token.value) return;
    await Promise.all([refreshProfile(), refreshModels(), refreshUsageSummary()]);
  }

  async function login() {
    authLoading.value = true;
    authError.value = "";
    try {
      const { data } = await apiClient.post<AuthResponse>("/auth/login", loginForm.value);
      token.value = data.token;
      setStoredToken(data.token);
      currentUser.value = data.user;
      await refreshUserData();
      return data.user;
    } catch (error: any) {
      const message = error?.response?.data?.message;
      if (message === "用户已被禁用") {
        authError.value = "用户被禁用，请联系管理员";
      } else if (error?.response?.status === 401) {
        authError.value = "账号或密码错误，请重试";
      } else {
        authError.value = message ?? "登录失败。";
      }
      throw error;
    } finally {
      loginForm.value.password = "";
      authLoading.value = false;
    }
  }

  async function register() {
    authLoading.value = true;
    authError.value = "";
    try {
      const { data } = await apiClient.post<AuthResponse>("/auth/register", registerForm.value);
      token.value = data.token;
      setStoredToken(data.token);
      currentUser.value = data.user;
      await refreshUserData();
      return data.user;
    } catch (error: any) {
      authError.value = error?.response?.data?.message ?? "注册失败。";
      throw error;
    } finally {
      authLoading.value = false;
    }
  }

  function logout() {
    clearStoredToken();
    token.value = "";
    currentUser.value = null;
    availableModels.value = [];
    usageSummary.value = null;
    authError.value = "";
  }

  async function savePreferredModel() {
    if (!currentUser.value) return;
    const { data } = await apiClient.patch<UserProfileResponse>("/users/me/model", {
      modelName: currentUser.value.preferredModel
    });
    currentUser.value = data;
  }

  return {
    authMode,
    authError,
    authLoading,
    availableModels,
    currentUser,
    isAdmin,
    isAuthenticated,
    login,
    loginForm,
    logout,
    refreshModels,
    refreshProfile,
    refreshUserData,
    refreshUsageSummary,
    register,
    registerForm,
    savePreferredModel,
    token,
    usageSummary
  };
}
