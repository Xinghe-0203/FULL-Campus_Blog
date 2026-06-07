# Campus Blog Forum System

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Version](https://img.shields.io/badge/version-v2.0.21-blue)](https://github.com/Xinghe-0203/FULL-Campus_Blog)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.13-green)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4.21-brightgreen)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue)](https://www.mysql.com/)

校园博客论坛系统 —— 基于 Spring Boot 3 + Vue 3 的全栈博客论坛平台，支持文章发布、校友圈、点赞收藏、关注互动、私信通知等功能。

---

## 目录

- [技术栈](#技术栈)
- [项目功能](#项目功能)
- [项目结构](#项目结构)
- [部署指南](#部署指南)
- [默认账号](#默认账号)
- [系统架构](#系统架构)
- [性能优化](#性能优化)
- [安全特性](#安全特性)
- [API 文档](#api-文档)
- [文档索引](#文档索引)
- [开源协议](#开源协议)
- [作者](#作者)

---

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| **Java** | 21 LTS | 编程语言 |
| **Spring Boot** | 3.3.13 | Web 框架 |
| **Spring Security** | 6.x | 认证与授权 |
| **MyBatis Plus** | 3.5.8 | ORM 框架 |
| **JJWT** | 0.12.6 | JWT 令牌生成与验证 |
| **Caffeine** | 3.2.0 | 本地缓存（5 个缓存实例） |
| **Knife4j** | 4.5.0 | API 文档（Swagger 增强） |
| **Jsoup** | 1.18.3 | XSS 防护（HTML 净化） |
| **Hutool** | 5.8.40 | Java 工具库 |
| **Lombok** | 1.18.42 | 代码简化 |
| **Maven** | 3.8+ | 构建工具 |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| **TypeScript** | 6.x | 编程语言 |
| **Vue** | 3.4.21 | UI 框架（Composition API） |
| **Vite** | 5.2.0 | 构建工具 |
| **Vue Router** | 4.3.0 | 前端路由 |
| **Pinia** | 2.1.7 | 状态管理 |
| **Axios** | 1.7.4 | HTTP 客户端 |
| **Marked** | 12.0.1 | Markdown 渲染 |
| **DOMPurify** | 3.0.9 | XSS 防护 |
| **Highlight.js** | 11.9 | 代码高亮 |
| **npm** | 9+ | 包管理工具 |

### 数据库

| 技术 | 版本 | 说明 |
|------|------|------|
| **MySQL** | 8.0+ | 主数据库（22 张表） |

### 编程语言

| 语言 | 用途 |
|------|------|
| **Java 21** | 后端业务逻辑、API 接口、安全认证 |
| **TypeScript** | 前端组件、状态管理、类型安全 |
| **SQL** | 数据库建表、索引、查询 |
| **CSS** | 前端样式（Rainy Glassmorphism 设计系统） |
| **XML** | MyBatis Mapper 映射、Logback 日志配置 |

---

## 项目功能

### 用户系统
- 用户注册、登录（JWT 双 Token：Access 24h + Refresh 7d）
- 个人资料编辑、头像上传
- 密码修改、密码重置

### 文章系统
- 文章 CRUD（Markdown 编辑器）
- 标签管理、草稿箱
- 全文搜索、分类筛选
- 阅读量统计

### 互动系统
- 点赞 / 取消点赞
- 收藏 / 取消收藏
- 嵌套评论（支持多级回复）
- 关注 / 取消关注

### 校友圈
- 动态发布（支持 @提及、位置标签、话题标签）
- 转发功能
- 点赞、评论

### 消息系统
- 私信（一对一聊天）
- 通知（点赞、评论、关注提醒）
- 举报功能

### 管理后台
- 用户管理（禁用、角色分配）
- 内容审核（文章、评论）
- 举报处理
- 数据统计面板

---

## 项目结构

```
Campus-Blog/
├── edu_project/                    # 后端（Spring Boot 3 + Java 21）
│   ├── pom.xml                     # Maven 依赖配置
│   ├── Dockerfile                  # Docker 构建文件
│   ├── docker-compose.yml          # Docker Compose 编排
│   ├── schema.sql                  # 数据库建表脚本
│   ├── .env.example                # 环境变量模板
│   └── src/main/
│       ├── java/com/example/edu_project/
│       │   ├── controller/         # REST 控制器（按领域分组）
│       │   │   ├── auth/           # 认证（登录、注册、密码重置）
│       │   │   ├── user/           # 用户管理
│       │   │   ├── post/           # 文章、评论、点赞、收藏、标签
│       │   │   ├── circle/         # 校友圈
│       │   │   ├── social/         # 关注、私信、通知、举报
│       │   │   ├── content/        # 话题、热门、媒体、分享、统计
│       │   │   └── admin/          # 管理后台（8 个 Controller）
│       │   ├── service/            # 业务逻辑层
│       │   ├── mapper/             # MyBatis Plus Mapper（22 个）
│       │   ├── entity/             # 数据库实体（22 个）
│       │   ├── dto/                # 请求数据传输对象
│       │   ├── vo/                 # 响应视图对象
│       │   └── config/             # 配置类（安全、缓存、数据库、Web）
│       └── resources/
│           ├── application.yml     # 主配置文件
│           ├── logback-spring.xml  # 日志配置
│           └── mapper/             # MyBatis XML 映射
│
├── edu_project_vue/                # 前端（Vue 3 + TypeScript）
│   ├── package.json                # npm 依赖配置
│   ├── vite.config.ts              # Vite 构建配置
│   ├── tsconfig.json               # TypeScript 配置
│   └── src/
│       ├── api/                    # API 模块（17 个）
│       ├── views/                  # 页面组件（30 个）
│       ├── components/             # 通用组件（11 个）
│       ├── stores/                 # Pinia 状态管理（3 个 Store）
│       ├── router/                 # 路由配置（模块化）
│       ├── composables/            # 组合式函数
│       ├── styles/                 # 全局样式（Rainy Glassmorphism）
│       ├── types/                  # TypeScript 类型定义
│       └── utils/                  # 工具函数
│
├── README.md                       # 项目说明（本文件）
├── CLAUDE.md                       # Claude Code 开发指南
├── AGENTS.md                       # AI Agent 项目指南
└── CHANGELOG.md                    # 版本更新日志
```

---

## 部署指南

### 前置要求

| 软件 | 最低版本 | 用途 |
|------|----------|------|
| JDK | 21 LTS | 后端编译与运行 |
| Maven | 3.8+ | 后端构建 |
| Node.js | 18+ | 前端构建与开发 |
| npm | 9+ | 前端包管理 |
| MySQL | 8.0+ | 数据库 |

### 方式一：本地开发部署

#### 1. 数据库初始化

```bash
mysql -u root -p
```

```sql
CREATE DATABASE campus_blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_blog;
SOURCE edu_project/schema.sql;
```

#### 2. 后端启动

```bash
cd edu_project
cp .env.example .env        # 编辑 .env，设置 DB_PASSWORD 和 JWT_SECRET
mvn spring-boot:run
```

后端启动后：
- API 地址：http://localhost:8825/api
- API 文档：http://localhost:8825/api/doc.html

#### 3. 前端启动

```bash
cd edu_project_vue
npm install
npm run dev
```

前端启动后访问：http://localhost:3000

### 方式二：Docker 部署

```bash
cd edu_project
cp .env.example .env        # 编辑 .env，设置 DB_PASSWORD 和 JWT_SECRET
docker-compose up -d        # 启动 MySQL + 后端
docker-compose logs -f      # 查看日志
```

前端需要单独构建：

```bash
cd edu_project_vue
npm install
npm run build               # 产出 dist/ 目录
```

将 `dist/` 目录部署到 Nginx 或其他静态文件服务器。

### 方式三：生产环境部署

#### 后端打包

```bash
cd edu_project
mvn clean package -DskipTests
java -jar target/edu_project-0.0.1-SNAPSHOT.jar
```

#### 前端构建

```bash
cd edu_project_vue
npm install
npm run build
```

#### Nginx 参考配置

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        root /path/to/edu_project_vue/dist;
        try_files $uri $uri/ /index.html;
    }

    # 后端 API 代理
    location /api/ {
        proxy_pass http://localhost:8825/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 上传文件代理
    location /uploads/ {
        proxy_pass http://localhost:8825/api/uploads/;
    }
}
```

---

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | Admin123 | ROLE_ADMIN |

---

## 系统架构

### 整体架构

```
浏览器 → Nginx → Vue 3 (SPA) → Axios → Spring Boot 3 → MyBatis Plus → MySQL
                                          ↓
                                    Caffeine 缓存
```

### 后端分层

```
Controller（控制器）→ Service（服务）→ Mapper（数据访问）→ Entity（实体）
         ↓
    Result<T>（统一响应格式）
```

### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": 1716000000000
}
```

### 数据库表（22 张）

`sys_user`, `blog_post`, `blog_comment`, `blog_tag`, `blog_post_tag`, `blog_like`, `blog_collect`, `blog_follow`, `blog_notification`, `blog_trending`, `blog_draft`, `blog_draft_tag`, `blog_report`, `blog_circle_post`, `blog_circle_like`, `blog_circle_comment`, `blog_circle_repost`, `blog_media`, `blog_post_media`, `blog_topic`, `blog_message`, `blog_share`

---

## 性能优化

| 优化项 | 说明 |
|--------|------|
| 数据库索引 | 16 个索引覆盖高频查询 |
| Caffeine 缓存 | 5 个缓存实例（用户、帖子、分类、标签、统计） |
| 分页查询 | MyBatis Plus 分页插件 |
| 异步日志 | Logback AsyncAppender 减少 I/O 阻塞 |
| 前端优化 | 路由懒加载、代码分割、Tree Shaking |

---

## 安全特性

| 特性 | 实现方式 |
|------|----------|
| 密码加密 | BCrypt（强度 12），API 不返回密码字段 |
| JWT 认证 | Access Token（24h）+ Refresh Token（7d），支持令牌轮换 |
| 令牌黑名单 | 登出时加入黑名单，防止重放攻击 |
| 登录锁定 | 5 次失败锁定 15 分钟（原子操作） |
| XSS 防护 | 服务端 Jsoup HTML 净化 + 前端 DOMPurify |
| 限流 | 23 个端点限流（RateLimitInterceptor） |
| SQL 注入 | MyBatis Plus 参数化查询 |
| CORS | 可配置白名单，生产环境禁止 `*` |
| 数据校验 | Spring Validation（@Valid、@NotBlank） |

---

## API 文档

启动后端后访问 Knife4j API 文档：

**http://localhost:8825/api/doc.html**

### API 模块概览

| 模块 | 端点数 | 说明 |
|------|--------|------|
| Auth | 5 | 登录、注册、刷新令牌、登出、密码重置 |
| User | 8 | 用户信息、头像、个人资料 |
| Post | 12 | 文章 CRUD、草稿、搜索 |
| Comment | 6 | 评论 CRUD、嵌套回复 |
| Like | 4 | 点赞 / 取消点赞、状态查询 |
| Collect | 4 | 收藏 / 取消收藏、状态查询 |
| Follow | 5 | 关注 / 取消关注、粉丝列表 |
| Notification | 5 | 通知列表、已读、删除 |
| Message | 5 | 私信会话、发送、已读 |
| Circle | 8 | 校友圈动态 CRUD |
| Tag | 5 | 标签管理 |
| Topic | 5 | 话题管理 |
| Trending | 3 | 热门内容 |
| Media | 4 | 文件上传 |
| Report | 4 | 举报 |
| Share | 3 | 分享 |
| Statistics | 4 | 统计数据 |
| Admin | 15+ | 管理后台 |

**总计：120+ 个 API 端点**

---

## 文档索引

| 文档 | 路径 | 说明 |
|------|------|------|
| 项目说明 | [README.md](README.md) | 本文档 |
| 开发指南 | [CLAUDE.md](CLAUDE.md) | Claude Code 开发规范 |
| Agent 指南 | [AGENTS.md](AGENTS.md) | AI Agent 项目指南 |
| 更新日志 | [CHANGELOG.md](CHANGELOG.md) | 版本变更记录 |
| 后端文档 | [edu_project/README.md](edu_project/README.md) | 后端详细文档 |
| 前端文档 | [edu_project_vue/README.md](edu_project_vue/README.md) | 前端详细文档 |
| 部署文档 | [edu_project/DEPLOY.md](edu_project/DEPLOY.md) | 生产环境部署指南 |
| API 文档 | http://localhost:8825/api/doc.html | 启动后端后访问 |

---

## 开源协议

本项目基于 **MIT License** 开源。

MIT License 是一种宽松的软件许可证，允许任何人自由使用、复制、修改、合并、发布、分发、再许可和/或出售本软件的副本，只需在所有副本或重要部分中包含版权声明和许可声明。

详见 [LICENSE](LICENSE) 文件。

---

## 作者

**刘畅 (Liu Chang)**

- GitHub：[Xinghe-0203](https://github.com/Xinghe-0203/FULL-Campus_Blog)
- 博客：[blog.starsx.top](https://blog.starsx.top/)

---

## 致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot) - Java Web 框架
- [Vue.js](https://vuejs.org/) - 前端 UI 框架
- [MyBatis Plus](https://baomidou.com/) - ORM 框架
- [Vite](https://vitejs.dev/) - 前端构建工具
- [Caffeine](https://github.com/ben-manes/caffeine) - Java 缓存库
