# AI Code Helper

[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3-42b883)](https://vuejs.org/)
[![Vite](https://img.shields.io/badge/Vite-5-646cff)](https://vitejs.dev/)
[![MyBatis](https://img.shields.io/badge/MyBatis-3-red)](https://mybatis.org/mybatis-3/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

[中文](#中文说明) | [English](#english)

---

## 中文说明

一个面向编程学习、面试准备和 AI 问答场景的全栈项目。

后端基于 Spring Boot 3 构建，集成了 JWT 鉴权、MyBatis 数据访问、LangChain4j、RAG 检索增强、会话中心、套餐与配额体系、调用审计日志和管理员后台能力；前端基于 Vue 3 + Vite 构建，提供登录注册、对话工作台、账户详情和管理员界面。

后端部分的 AI 对话主链路主要参考项目原型进行实现，在此基础上继续扩展了登录鉴权、用户管理、会话管理、配额控制、审计日志和管理员后台等功能。前端部分主要通过 AI 工具辅助生成并持续迭代。

如果你希望在这个基础上继续扩展自己的 AI Agent / AI 助手项目，这个仓库可以作为一个可运行、可继续演进的起点。

### 中文目录

- [项目截图](#项目截图)
- [核心功能](#核心功能)
- [软件架构](#软件架构)
  - [技术栈](#技术栈)
  - [核心特点](#核心特点)
  - [项目结构](#项目结构)
- [快速开始](#快速开始)
  - [前置要求](#前置要求)
  - [配置说明](#配置说明)
  - [数据库初始化](#数据库初始化)
  - [构建项目](#构建项目)
  - [默认管理员账号](#默认管理员账号)
- [接口文档](#接口文档)
- [SQL 调试](#sql-调试)
- [后端设计亮点](#后端设计亮点)
- [后续可扩展方向](#后续可扩展方向)
- [参考原型](#参考原型)
- [License](#license)

## 项目截图

登录页  
![登录页](image_show/img_login.png)

对话工作台（普通用户）  
![对话工作台](image_show/img_workspace.png)

管理员后台  
![管理员后台](image_show/img_admin.png)

## 核心功能

- 用户体系：注册、登录、JWT 鉴权、角色区分、禁用用户、账户详情页
- 会话中心：历史对话持久化、会话重命名、删除对话、删除消息、继续对话、多端同步
- AI 对话：SSE 流式输出、模型切换、消息编辑后重新生成
- 知识增强：基于 LangChain4j 标准 RAG 的本地知识库检索增强
- 配额能力：每日总配额、按模型分配配额、套餐等级和默认配额模板
- 管理后台：用户列表、用户 ID 筛选、套餐与角色管理、配额管理、模型配额管理
- 调用审计：按用户、模型、成功失败、时间范围筛选调用日志
- 接口文档：集成 Knife4j / OpenAPI 3，便于联调和测试

## 软件架构

### 技术栈

**后端**

- Java 21
- Spring Boot 3.5
- Spring Web
- Spring Security
- MyBatis
- MySQL 8
- LangChain4j
- DashScope / 通义千问
- JWT
- Knife4j / OpenAPI 3
- Lombok

**前端**

- Vue 3
- TypeScript
- Vite
- Axios

### 核心特点

#### 1. 面向业务的 AI 应用后端

项目不只是简单封装大模型接口，而是围绕 AI 问答场景补齐了用户体系、会话管理、配额控制、审计日志和后台管理接口，更接近可落地的业务系统。

#### 2. 流式对话与会话持久化结合

后端通过 SSE 增量返回模型输出，前端实时渲染；同时把会话、消息和上下文记忆写入数据库，保证刷新页面、重启服务后仍能继续对话。

#### 3. 基于 MyBatis 的显式数据访问

数据访问层统一采用 Mapper 模式，服务层通过 Mapper 执行 SQL，便于控制查询逻辑、审计统计 SQL 和复杂筛选场景。

#### 4. 可运营的用户与配额体系

项目支持禁用用户、套餐等级、默认配额模板、按模型配额限制和管理员后台配置，适合继续向多用户 AI 平台演进。

#### 5. 支持知识库增强与降级运行

已接入 RAG 检索增强。即使知识库初始化失败或嵌入模型额度不足，系统也可以降级启动，保证核心功能可用。

### 项目结构

```text
ai-code-helper/
├─ src/main/java/org/example/aicodehelper
│  ├─ config/          # Spring、OpenAPI、跨域、模型等配置
│  ├─ controller/      # Controller 层
│  ├─ domain/          # 实体对象
│  ├─ dto/request/     # 请求参数对象
│  ├─ event/           # SSE / 流式事件对象
│  ├─ exception/       # 全局异常处理
│  ├─ guardrail/       # 输入安全控制
│  ├─ listener/        # 模型调用监听
│  ├─ mapper/          # MyBatis Mapper 层
│  ├─ model/           # 大模型相关配置对象
│  ├─ rag/             # RAG 检索增强相关配置
│  ├─ security/        # JWT、用户认证与权限控制
│  ├─ service/         # Service 层
│  └─ vo/              # 返回前端的展示对象
├─ src/main/resources
│  ├─ application.yml  # 应用配置
│  ├─ docs/            # 本地知识库文档
│  └─ system-prompt.txt
├─ ai-code-helper-frontend/vue
│  ├─ src/api/         # 前端请求封装
│  ├─ src/components/  # 页面组件
│  ├─ src/composables/ # 组合式逻辑
│  └─ src/types/       # 前端类型定义
├─ image_show/         # README 截图资源
├─ sql/
│  └─ schema.sql       # 数据库初始化脚本
├─ pom.xml
└─ README.md
```

## 快速开始

### 前置要求

在启动项目之前，请先准备以下环境：

- JDK 21
- Maven 3.9+
- Node.js 18+
- MySQL 8.x
- DashScope API Key

### 配置说明

后端默认读取 `src/main/resources/application.yml` 中的配置。推荐通过环境变量覆盖关键项，也可以在本地开发时直接写入 `application.yml`，但不要提交真实密钥到远程仓库。

建议准备以下配置项：

1. 数据库配置
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

2. 模型配置
- `DASHSCOPE_API_KEY`

3. 安全配置
- `APP_JWT_SECRET`

4. 管理员账号配置
- `APP_ADMIN_USERNAME`
- `APP_ADMIN_PASSWORD`

默认后端端口为 `8081`，上下文路径为 `/api`。

### 数据库初始化

项目当前基于 MyBatis，不依赖 JPA 自动建表，因此首次启动前需要手动初始化数据库结构。

初始化脚本位于：[`sql/schema.sql`](sql/schema.sql)

你可以任选一种方式执行：

**方式 A：在 MySQL 客户端中执行**

```sql
SOURCE /path/to/ai-code-helper/sql/schema.sql;
```

**方式 B：使用命令行导入**

```bash
mysql -u root -p < sql/schema.sql
```

这个脚本会完成：

- 创建 `db1`
- 创建用户表、模型配额表、会话表、消息表、记忆表、调用审计表
- 建立必要的主键、唯一约束和外键关系

数据库和数据表使用 `utf8mb4`，可以正常存储中文标题、消息内容和审计日志预览。

### 构建项目

#### 1. 构建后端

在项目根目录执行：

```bash
mvn clean package
```

如果只想跳过测试快速打包：

```bash
mvn -Dmaven.test.skip=true package
```

#### 2. 启动后端

```bash
mvn spring-boot:run
```

启动成功后可访问：

- Knife4j: `http://localhost:8081/api/doc.html`
- OpenAPI JSON: `http://localhost:8081/api/v3/api-docs`

#### 3. 安装并启动前端

进入前端目录：

```bash
cd ai-code-helper-frontend/vue
npm install
npm run dev
```

前端默认运行在：

- `http://localhost:5173`

### 默认管理员账号

若数据库中不存在管理员账号，系统启动时会按配置自动初始化管理员账户。默认值为：

- 用户名：`admin`
- 密码：`admin123456`

建议在实际使用前通过环境变量覆盖。

## 接口文档

项目已集成 Knife4j，启动成功后可直接访问：

- [http://localhost:8081/api/doc.html](http://localhost:8081/api/doc.html)

## SQL 调试

项目已开启 MyBatis SQL 控制台输出，运行和调试时可以直接看到：

- 执行的 SQL
- 绑定参数
- 返回列和结果行数

如果你希望进一步区分开发环境和生产环境，建议把 SQL 日志单独放到 `application-dev.yml` 中。

## 后端设计亮点

### 1. 以业务能力为中心，而不是简单调用模型接口

后端不仅实现了大模型调用，还补齐了用户、会话、配额、审计和后台管理等完整业务闭环。

### 2. 会话中心具备可恢复上下文能力

系统把会话消息和上下文记忆一起持久化，保证“继续对话”是真正延续上下文，而不是只展示历史消息。

### 3. 支持模型和套餐扩展

模型目录、套餐等级、默认配额模板和模型配额都支持继续扩展，便于后续演进到多模型、多套餐的 AI 平台。

## 后续可扩展方向

- 增加知识库上传、管理和重建索引能力
- 增加会话搜索、分页、归档和导出能力
- 增加成本统计、限流和告警能力
- 支持更多模型供应商和多环境配置

## 参考原型

- [CSDN 参考文章](https://blog.csdn.net/weixin_41701290/article/details/149270103)
- [项目原型仓库](https://github.com/liyupi/ai-code-helper)

## License

本项目采用 MIT License。

---

## English

A full-stack AI assistant project for programming learning, interview preparation, and AI Q&A scenarios.

The backend is built with Spring Boot 3 and integrates JWT authentication, MyBatis data access, LangChain4j, RAG-based retrieval enhancement, conversation management, subscription and quota control, audit logs, and an admin panel. The frontend is built with Vue 3 + Vite and includes login/register, chat workspace, account details, and admin views.

The main AI chat flow on the backend was initially inspired by the original prototype repository, and then extended with authentication, user management, conversation management, quota control, audit logs, and admin capabilities. The frontend was mainly generated and iterated with AI-assisted tooling.

If you want to build your own AI Agent / AI assistant project on top of a runnable baseline, this repository is a practical starting point.

### English TOC

- [Screenshots](#screenshots)
- [Key Features](#key-features)
- [Architecture](#architecture)
  - [Tech Stack](#tech-stack)
  - [Core Highlights](#core-highlights)
  - [Project Structure](#project-structure)
- [Quick Start](#quick-start)
  - [Prerequisites](#prerequisites)
  - [Configuration](#configuration)
  - [Database Initialization](#database-initialization)
  - [Build the Project](#build-the-project)
  - [Default Admin Account](#default-admin-account)
- [API Documentation](#api-documentation)
- [SQL Debugging](#sql-debugging)
- [Backend Design Highlights](#backend-design-highlights)
- [Future Extensions](#future-extensions)
- [Reference](#reference)
- [License](#license)

## Screenshots

Login Page  
![Login Page](image_show/img_login.png)

Chat Workspace  
![Chat Workspace](image_show/img_workspace.png)

Admin Panel  
![Admin Panel](image_show/img_admin.png)

## Key Features

- User system: register, login, JWT auth, role separation, disabled users, account details page
- Conversation center: persistent chat history, rename conversation, delete conversation, delete message, resume conversation, multi-device sync
- AI chat: SSE streaming output, model switching, edit-and-regenerate message
- Knowledge enhancement: local RAG retrieval based on LangChain4j
- Quota system: daily total quota, per-model quota, subscription tiers, default quota templates
- Admin console: user list, filter by user ID, subscription and role management, quota management, model quota management
- Audit logs: filter by user, model, success/failure, and time range
- API docs: integrated Knife4j / OpenAPI 3 for testing and integration

## Architecture

### Tech Stack

**Backend**

- Java 21
- Spring Boot 3.5
- Spring Web
- Spring Security
- MyBatis
- MySQL 8
- LangChain4j
- DashScope / Qwen
- JWT
- Knife4j / OpenAPI 3
- Lombok

**Frontend**

- Vue 3
- TypeScript
- Vite
- Axios

### Core Highlights

#### 1. Business-oriented AI backend

This project is more than a thin wrapper around an LLM API. It includes user management, conversation lifecycle management, quota control, audit logs, and admin operations around the AI workflow.

#### 2. Streaming chat with persistent memory

The backend streams responses via SSE while also persisting conversations, messages, and memory context into the database, so users can continue conversations after refresh or restart.

#### 3. Explicit SQL data access with MyBatis

The data access layer uses MyBatis mappers throughout, giving clear control over SQL, filtering logic, and audit/statistics queries.

#### 4. Operational user and quota system

The project supports disabled users, subscription tiers, default quota templates, per-model quota limits, and admin-side configuration, making it suitable for evolving into a multi-user AI platform.

#### 5. RAG support with graceful degradation

RAG retrieval enhancement is integrated. If knowledge-base initialization fails or embedding quota is exhausted, the system can still start in degraded mode so the core chat experience remains available.

### Project Structure

```text
ai-code-helper/
├─ src/main/java/org/example/aicodehelper
│  ├─ config/          # Spring, OpenAPI, CORS, model configuration
│  ├─ controller/      # Controller layer
│  ├─ domain/          # Domain entities
│  ├─ dto/request/     # Request DTOs
│  ├─ event/           # SSE / streaming event objects
│  ├─ exception/       # Global exception handling
│  ├─ guardrail/       # Input safety rules
│  ├─ listener/        # Model invocation listeners
│  ├─ mapper/          # MyBatis mapper layer
│  ├─ model/           # Model-related config objects
│  ├─ rag/             # RAG configuration
│  ├─ security/        # JWT, auth, permission control
│  ├─ service/         # Service layer
│  └─ vo/              # View objects returned to frontend
├─ src/main/resources
│  ├─ application.yml
│  ├─ docs/
│  └─ system-prompt.txt
├─ ai-code-helper-frontend/vue
│  ├─ src/api/
│  ├─ src/components/
│  ├─ src/composables/
│  └─ src/types/
├─ image_show/
├─ sql/
│  └─ schema.sql
├─ pom.xml
└─ README.md
```

## Quick Start

### Prerequisites

Make sure you have:

- JDK 21
- Maven 3.9+
- Node.js 18+
- MySQL 8.x
- DashScope API Key

### Configuration

The backend reads configuration from `src/main/resources/application.yml` by default. It is recommended to override sensitive values with environment variables instead of committing real secrets.

Suggested configuration items:

1. Database
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

2. Model
- `DASHSCOPE_API_KEY`

3. Security
- `APP_JWT_SECRET`

4. Admin account
- `APP_ADMIN_USERNAME`
- `APP_ADMIN_PASSWORD`

The backend runs on port `8081` with context path `/api` by default.

### Database Initialization

This project now uses MyBatis and does not rely on JPA auto-DDL, so you need to initialize the database schema manually before the first startup.

Initialization script:

- [`sql/schema.sql`](sql/schema.sql)

You can execute it in one of the following ways:

**Option A: Run inside MySQL client**

```sql
SOURCE /path/to/ai-code-helper/sql/schema.sql;
```

**Option B: Import from command line**

```bash
mysql -u root -p < sql/schema.sql
```

The script will:

- create `db1`
- create user, model quota, conversation, message, memory, and audit log tables
- create required primary keys, unique constraints, and foreign key relationships

The database and tables use `utf8mb4` to support Chinese text and full Unicode content correctly.

### Build the Project

#### 1. Build backend

```bash
mvn clean package
```

To skip tests:

```bash
mvn -Dmaven.test.skip=true package
```

#### 2. Start backend

```bash
mvn spring-boot:run
```

Available after startup:

- Knife4j: `http://localhost:8081/api/doc.html`
- OpenAPI JSON: `http://localhost:8081/api/v3/api-docs`

#### 3. Install and start frontend

```bash
cd ai-code-helper-frontend/vue
npm install
npm run dev
```

Frontend default URL:

- `http://localhost:5173`

### Default Admin Account

If no admin account exists in the database, the system initializes one automatically on startup. Default values:

- Username: `admin`
- Password: `admin123456`

Override them before real use.

## API Documentation

- [http://localhost:8081/api/doc.html](http://localhost:8081/api/doc.html)

## SQL Debugging

MyBatis SQL logging is enabled in the project, so during development you can directly see:

- executed SQL
- bound parameters
- returned columns and row counts

If needed, you can later move SQL logging into a dedicated `application-dev.yml`.

## Backend Design Highlights

### 1. Business workflow first

The backend is not only about calling an LLM. It builds a complete workflow around users, conversations, quotas, audit, and admin management.

### 2. Recoverable conversation context

Conversation messages and memory are persisted together, so “continue conversation” actually restores context instead of only showing history.

### 3. Extensible models and subscription strategy

The project is already structured to support more models, more subscription tiers, and more quota strategies later.

## Future Extensions

- Knowledge base upload, management, and re-indexing
- Conversation search, pagination, archive, and export
- Cost statistics, rate limiting, and alerting
- More model providers and multi-environment configuration

## Reference

- [CSDN Reference Article](https://blog.csdn.net/weixin_41701290/article/details/149270103)
- [Original Prototype Repository](https://github.com/liyupi/ai-code-helper)

## License

This project is licensed under the MIT License.
