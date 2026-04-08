import axios from "axios";

const TOKEN_KEY = "ai-code-helper-token";

export function getStoredToken() {
  return window.localStorage.getItem(TOKEN_KEY) ?? "";
}

export function setStoredToken(token: string) {
  window.localStorage.setItem(TOKEN_KEY, token);
}

export function clearStoredToken() {
  window.localStorage.removeItem(TOKEN_KEY);
}

const apiClient = axios.create({
  baseURL: "/api",
  timeout: 15000
});

apiClient.interceptors.request.use((config) => {
  const token = getStoredToken();
  if (token) {
    config.headers = config.headers ?? {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default apiClient;
