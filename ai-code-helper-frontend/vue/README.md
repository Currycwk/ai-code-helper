# AI 编程小助手 - 前端

这是一个基于 Vue 3 的单页聊天室前端，使用 SSE 与后端流式对话，并用 Axios 请求本地欢迎语配置。

## 本地开发（Windows PowerShell）

```powershell
cd C:\Users\youyu\IdeaProjects\ai-code-helper\ai-code-helper-frontend\vue
npm install
npm run dev
```

## 打包构建

```powershell
cd C:\Users\youyu\IdeaProjects\ai-code-helper\ai-code-helper-frontend\vue
npm run build
```

## 说明

- SSE 接口通过 `/api/ai/chat` 访问，已在 `vite.config.ts` 中代理到 `http://localhost:8081`。
- 页面加载时会通过 Axios 读取 `public/welcome.json`，以满足 Axios 使用要求并展示欢迎语。
