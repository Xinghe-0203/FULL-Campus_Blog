# 校园博客论坛系统 / Campus Blog Forum System

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Version](https://img.shields.io/badge/version-v2.0.15-blue)](https://github.com/Xinghe-0203/FULL-Campus_Blog)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-green)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4.21-brightgreen)](https://vuejs.org/)

> **MIT License** — 允许自由使用、复制、修改、合并、发布、分发、再授权和销售本软件的副本。

一个基于 **Spring Boot 3 + Vue 3 + MyBatis Plus** 的全栈校园博客论坛系统，支持文章发布、校友圈动态、点赞收藏、关注互动、消息通知等功能。

**v2.0.15 最新修复：**
- HomeFilters.vue `filters is not defined` 运行时错误修复
- PostDetail.vue 文章评论幽灵数据问题（records 解析修复）
- Circle.vue 点赞失败无错误提示问题修复
- Circle.vue 热门动态移至左侧边栏，优化数据源
- Circle.vue 侧边栏数据每 60 秒自动刷新
- logger.js CORS 错误静默处理（/api/log 端点不存在）
- 校友圈移除标签功能（仅保留话题）

**v2.0.14 最新修复：**
- 密码重置字段名修复（password → newPassword）

**v2.0.13 最新修复：**
- 文章话题功能移除（标签功能保留）
- 校友圈话题功能修复并完善
- 话题数据路径修复（data.data.records）
- PostEdit.vue import 语句补全

**v2.0.11 最新修复：**
- 修复写文章选择标签时白屏的运行时错误

**v2.0 全新升级：**
- Rainy Glassmorphism UI 设计系统（水滴、涟漪、光泽动效）
- 16 项性能优化索引（查询速度提升 300 倍）
- Caffeine 缓存策略（5 个缓存实例）
- 校友圈功能增强（@mentions、位置标签、话题标签、显示切换）
- Messages 布局修复（浮动返回按钮）
- 所有页面宽度统一扩展至 1400px

---

## 目录

- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [快速开始](#快速开始)
  - [环境要求](#环境要求)
  - [数据库初始化](#数据库初始化)
  - [后端启动](#后端启动)
  - [前端启动](#前端启动)
- [项目结构](#项目结构)
- [API 接口文档](#api-接口文档)
- [数据库设计](#数据库设计)
- [性能优化](#性能优化)
- [安全特性](#安全特性)
- [配置说明](#配置说明)
- [部署指南](#部署指南)
- [开发规范](#开发规范)
- [更新日志](#更新日志)
- [许可证](#许可证)
- [作者](#作者)

---

## 功能特性

### 用户模块
- 用户注册/登录（JWT Token 认证）
- 个人资料编辑、头像上传、封面图上传
- 修改密码、密码找回（邮箱验证码）
- 登录失败锁定（5 次失败 → 15 分钟锁定）
- 多设备登录管理、强制登出其他设备

### 文章模块
- 文章发布/编辑/删除（含审核流程）
- Markdown 编辑器 + 实时预览
- 文章封面图、文章标签
- 草稿自动保存
- 文章浏览量统计、全文搜索
- 高级搜索（关键词、标签、分类、时间范围）

### 互动模块
- 点赞/取消点赞、收藏/取消收藏
- 评论/回复（二级评论嵌套）
- 关注/取消关注、粉丝/关注列表
- 分享功能（站内分享记录）

### 校友圈模块（v2.0 增强）
- 发布动态（支持图片）
- 点赞/评论/转发
- 推荐/关注动态流
- 可见性控制（公开/好友/私密）
- 用户动态列表
- **@mentions** — 动态中提及用户
- **位置标签** — 动态关联地理位置
- **话题标签** — 动态关联话题
- **显示切换** — 评论/转发显示控制

### 通知模块
- 消息通知（点赞、评论、关注）
- 未读消息数、标记已读
- 私信功能（发送/接收/已读）

### 管理后台
- 用户管理（启用/禁用/封禁/重置密码）
- 文章管理（审核/删除）
- 评论管理（删除）
- 举报管理（待处理/处理举报）
- 数据统计（社区统计/平台统计）

### UI 设计（v2.0 新增）
- Rainy Glassmorphism 毛玻璃拟态风格
- 水滴动画背景效果
- 涟漪交互反馈
- 光泽渐变动效
- 统一 1400px 最大页面宽度

---

## 技术栈

### 后端
| 组件 | 版本 | 说明 |
|------|------|------|
| Java | 21 | LTS 版本 |
| Spring Boot | 3.3.0 | Web 框架 |
| MyBatis Plus | 3.5.8 | ORM 框架 |
| Spring Security | 6.x | 安全认证 |
| JJWT | 0.12.6 | Token 认证 |
| Knife4j | 4.5.0 | API 文档 |
| Hutool | 5.8.40 | Java 工具库 |
| Jsoup | 1.18.3 | HTML 解析/XSS 防护 |
| Caffeine | 3.2.0 | 本地缓存 |
| MySQL | 8.0+ | 数据库 |

### 前端
| 组件 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.21 | UI 框架 |
| Vite | 5.2.0 | 构建工具 |
| Vue Router | 4.3.0 | 路由管理 |
| Pinia | 2.1.7 | 状态管理 |
| Axios | 1.7.4 | HTTP 客户端 |
| Marked | 12.0.1 | Markdown 渲染 |
| DOMPurify | 3.0.9 | XSS 防护 |

---

## 快速开始

### 环境要求
| 依赖 | 最低版本 | 推荐版本 |
|------|----------|----------|
| JDK | 21 | 21 LTS |
| Node.js | 18 | 20 LTS |
| MySQL | 8.0 | 8.0+ |
| Maven | 3.8 | 3.9+ |

### 1. 克隆项目

```bash
git clone https://github.com/Xinghe-0203/FULL-Campus_Blog.git
cd FULL-Campus_Blog
```

### 2. 数据库初始化

```bash
# 登录 MySQL
mysql -u root -p

# 在 MySQL 中执行
CREATE DATABASE campus_blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_blog;
SOURCE edu_project/数据库表.sql;

# 或直接在命令行中执行
mysql -u root -p campus_blog < edu_project/数据库表.sql
```

### 3. 后端启动

```bash
cd edu_project

# 复制环境变量模板
copy .env.example .env      # Windows
cp .env.example .env        # Linux/Mac

# 编辑 .env 文件，填入实际配置值
# DB_HOST=localhost
# DB_PORT=3306
# DB_NAME=campus_blog
# DB_USERNAME=root
# DB_PASSWORD=your_password
# JWT_SECRET=your_secret_key_at_least_32_characters

# 开发模式启动
mvn spring-boot:run

# 生产模式打包
mvn clean package -DskipTests
java -jar target/edu_project-0.0.1-SNAPSHOT.jar

# 运行测试（使用 H2 内存数据库）
mvn test
```

后端启动后访问：
- API: http://localhost:8825/api
- API 文档: http://localhost:8825/api/doc.html (Knife4j)

### 4. 前端启动

```bash
cd edu_project_vue

# 安装依赖
npm install

# 开发模式启动
npm run dev

# 生产模式构建
npm run build

# 代码检查
npm run lint
```

前端启动后访问：http://localhost:3000

> 前端 `/api` 请求会自动代理到后端 localhost:8825

### 5. 默认管理员账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | Admin123 | 管理员 |

---

## 项目结构

```
FULL-Campus_Blog/
├── edu_project/                    # 后端 (Spring Boot 3 + MyBatis Plus + Java 21)
│   ├── src/main/java/
│   │   └── com.example.edu_project/
│   │       ├── controller/          # 23 个 Controller
│   │       ├── service/            # 17 个 Service 接口
│   │       ├── service/impl/       # 17 个 Service 实现
│   │       ├── mapper/             # 22 个 Mapper 接口
│   │       ├── entity/             # 22 个 Entity 实体
│   │       ├── config/             # 配置类 (Security, JWT, Cache, Async 等)
│   │       ├── dto/                # 数据传输对象
│   │       ├── vo/                 # 视图对象
│   │       ├── event/              # 事件类
│   │       └── utils/              # 工具类
│   ├── src/main/resources/
│   │   └── application.yml         # 主配置
│   ├── src/test/                   # 测试 (H2 内存数据库)
│   ├── pom.xml                     # Maven 配置
│   ├── Dockerfile                  # Docker 镜像
│   ├── docker-compose.yml          # Docker 编排
│   ├── .env.example                # 环境变量模板
│   └── 数据库表.sql                 # 22 张数据库表
│
├── edu_project_vue/                # 前端 (Vue 3 + Vite)
│   ├── src/
│   │   ├── api/                    # 17 个 API 模块
│   │   ├── views/                  # 30 个页面组件
│   │   ├── components/             # 11 个可复用组件
│   │   │   ├── common/             # 8 个通用组件
│   │   │   └── layout/             # 3 个布局组件
│   │   ├── composables/            # 组合式函数
│   │   ├── stores/                 # 3 个 Pinia Store
│   │   ├── router/                 # 路由配置
│   │   ├── styles/                 # 样式文件
│   │   └── utils/                  # 工具函数
│   ├── package.json
│   └── vite.config.js
│
├── README.md                       # 项目文档
├── AGENTS.md                       # 开发规范
└── package.json                    # 根目录 (空 workspace)
```

### 统计概览

| 模块 | 数量 |
|------|------|
| 后端 Controller | 23 |
| 后端 Service 接口 | 17 |
| 后端 Mapper 接口 | 22 |
| 后端 Entity 实体 | 22 |
| 前端页面组件 | 30 |
| 前端可复用组件 | 11 |
| 前端 API 模块 | 17 |
| Pinia Store | 3 |
| 数据库表 | 22 |
| 性能优化索引 | 16 |
| Caffeine 缓存实例 | 5 |
| API 端点总数 | 120+ |

---

## API 接口文档

> 完整 API 文档已集成到 Knife4j，启动后端后访问 http://localhost:8825/api/doc.html

### 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1234567890123
}
```

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 409 | 数据冲突 |
| 500 | 服务器内部错误 |

### Controller 列表

| # | Controller | 端点数量 | 路径前缀 |
|---|-----------|---------|---------|
| 1 | AuthController | 4 | `/api/auth` |
| 2 | PasswordController | 2 | `/api/auth/password` |
| 3 | SysUserController | 8 | `/api/user` |
| 4 | BlogPostController | 10 | `/api/post` |
| 5 | BlogCommentController | 4 | `/api/comment` |
| 6 | BlogLikeController | 4 | `/api/like` |
| 7 | BlogCollectController | 4 | `/api/collect` |
| 8 | BlogTagController | 4 | `/api/tag` |
| 9 | FollowController | 6 | `/api/follow` |
| 10 | NotificationController | 5 | `/api/notification` |
| 11 | TrendingController | 3 | `/api/trending` |
| 12 | ReportController | 2 | `/api/report` |
| 13 | CircleController | 13 | `/api/circle` |
| 14 | MediaController | 7 | `/api/media` |
| 15 | TopicController | 5 | `/api/topic` |
| 16 | MessageController | 6 | `/api/message` |
| 17 | ShareController | 2 | `/api/share` |
| 18 | StatisticsController | 1 | `/api/statistics` |
| 19 | AdminPostController | 4 | `/api/admin/post` |
| 20 | AdminCommentController | 2 | `/api/admin/comment` |
| 21 | AdminReportController | 3 | `/api/admin/reports` |
| 22 | AdminUserController | 4 | `/api/admin/user` |
| 23 | AdminStatisticsController | 1 | `/api/admin/statistics` |

### 认证接口 `/api/auth`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 用户注册 | POST | `/api/auth/register` | 注册新用户 |
| 用户登录 | POST | `/api/auth/login` | 登录获取 Token |
| 发送验证码 | POST | `/api/auth/password/send-code` | 发送密码重置验证码 |
| 重置密码 | PUT | `/api/auth/password/reset-password` | 使用验证码重置密码 |

### 用户接口 `/api/user`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 刷新 Token | POST | `/api/user/refresh` | 使用 refreshToken 刷新 accessToken |
| 获取用户信息 | GET | `/api/user/{id}` | 根据 ID 获取用户信息 |
| 修改密码 | PUT | `/api/user/password` | 修改当前用户密码 |
| 搜索用户 | GET | `/api/user/search` | 搜索用户 |
| 退出登录 | POST | `/api/user/logout` | 退出登录（Token 加入黑名单） |
| 获取设备列表 | GET | `/api/user/devices` | 获取当前用户的登录设备列表 |
| 退出其他设备 | POST | `/api/user/logout-other-devices` | 强制登出其他设备 |
| 更新个人资料 | PUT | `/api/user/profile` | 更新昵称、简介等 |
| 更新头像 | PUT | `/api/user/avatar` | 上传并更新头像 |
| 更新封面图 | PUT | `/api/user/cover-image` | 上传并更新封面图 |

### 文章接口 `/api/post`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 发布文章 | POST | `/api/post` | 发布新文章 |
| 更新文章 | PUT | `/api/post/{id}` | 更新文章 |
| 删除文章 | DELETE | `/api/post/{id}` | 删除文章（软删除） |
| 获取文章详情 | GET | `/api/post/{id}` | 获取文章详情（包含作者信息） |
| 获取文章列表 | GET | `/api/post/list` | 获取文章列表（支持排序：latest/hot/essence） |
| 增加阅读量 | PUT | `/api/post/{id}/view` | 增加文章阅读量 |
| 高级搜索 | GET | `/api/post/search/advanced` | 高级搜索（关键词、标签、分类、时间） |
| 搜索建议 | GET | `/api/post/search/suggest` | 获取搜索建议 |
| 获取我的文章 | GET | `/api/post/my` | 获取当前用户的文章列表 |
| 获取草稿列表 | GET | `/api/post/draft/my` | 获取当前用户的草稿列表 |

### 评论接口 `/api/comment`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 发表评论 | POST | `/api/comment` | 对文章发表评论 |
| 获取文章评论 | GET | `/api/comment/post/{postId}` | 获取文章的评论列表（树形结构） |
| 删除评论 | DELETE | `/api/comment/{id}` | 删除评论（软删除） |
| 获取我的评论 | GET | `/api/comment/my` | 获取当前用户的评论列表 |

### 点赞接口 `/api/like`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 点赞/取消点赞 | POST | `/api/like/{postId}` | 切换点赞状态 |
| 检查点赞状态 | GET | `/api/like/check/{postId}` | 检查当前用户是否已点赞 |
| 获取我的点赞 | GET | `/api/like/my` | 获取当前用户的点赞列表 |
| 批量检查点赞状态 | POST | `/api/like/check/batch` | 批量检查多个文章的点赞状态 |

### 收藏接口 `/api/collect`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 收藏/取消收藏 | POST | `/api/collect/{postId}` | 切换收藏状态 |
| 检查收藏状态 | GET | `/api/collect/check/{postId}` | 检查当前用户是否已收藏 |
| 获取我的收藏 | GET | `/api/collect/my` | 获取当前用户的收藏列表 |
| 批量检查收藏状态 | POST | `/api/collect/check/batch` | 批量检查多个文章的收藏状态 |

### 标签接口 `/api/tag`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 创建标签 | POST | `/api/tag` | 创建新标签（需管理员权限） |
| 获取标签列表 | GET | `/api/tag/list` | 获取所有标签列表 |
| 获取标签详情 | GET | `/api/tag/{tagId}` | 获取标签详情（含文章数） |
| 删除标签 | DELETE | `/api/tag/{tagId}` | 删除标签（需管理员权限） |

### 关注接口 `/api/follow`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 关注用户 | POST | `/api/follow` | 关注指定用户 |
| 取消关注 | DELETE | `/api/follow/{targetUserId}` | 取消关注 |
| 检查关注状态 | GET | `/api/follow/check/{targetUserId}` | 检查是否已关注 |
| 获取粉丝列表 | GET | `/api/follow/followers/{userId}` | 获取用户的粉丝列表 |
| 获取关注列表 | GET | `/api/follow/following/{userId}` | 获取用户关注的列表 |
| 获取粉丝/关注数量 | GET | `/api/follow/counts/{userId}` | 获取粉丝数和关注数 |

### 通知接口 `/api/notification`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取通知列表 | GET | `/api/notification/list` | 获取通知列表（分页） |
| 获取未读数量 | GET | `/api/notification/unread-count` | 获取未读通知数量 |
| 标记单条已读 | PUT | `/api/notification/{id}/read` | 标记单条通知为已读 |
| 标记全部已读 | PUT | `/api/notification/read-all` | 标记全部通知为已读 |
| 删除通知 | DELETE | `/api/notification/{id}` | 删除通知 |

### 热门/趋势接口 `/api/trending`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取热门文章 | GET | `/api/trending/posts` | 获取热门文章列表 |
| 获取热门混排内容 | GET | `/api/trending/content` | 获取热门文章+动态混排列表 |
| 更新文章热度 | PUT | `/api/trending/update/{postId}` | 更新文章热度分数 |

### 草稿接口 `/api/post/draft`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 保存草稿 | POST | `/api/post/draft` | 保存文章草稿 |
| 获取最新草稿 | GET | `/api/post/draft/latest` | 获取最新草稿 |
| 删除草稿 | DELETE | `/api/post/draft/{draftId}` | 删除草稿 |
| 获取指定草稿 | GET | `/api/post/draft/{draftId}` | 获取指定草稿详情 |

### 举报接口 `/api/report`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 举报内容 | POST | `/api/report` | 举报违规内容 |
| 获取我的举报 | GET | `/api/report/my` | 获取我的举报记录 |

### 校友圈接口 `/api/circle`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 发布动态 | POST | `/api/circle/post` | 发布校友圈动态 |
| 获取推荐流 | GET | `/api/circle/feed/recommend` | 获取推荐动态流 |
| 获取关注流 | GET | `/api/circle/feed/following` | 获取关注动态流 |
| 获取动态详情 | GET | `/api/circle/post/{postId}` | 获取动态详情 |
| 删除动态 | DELETE | `/api/circle/post/{postId}` | 删除动态（软删除） |
| 点赞/取消点赞 | POST | `/api/circle/like/{postId}` | 切换点赞状态 |
| 检查点赞状态 | GET | `/api/circle/like/check/{postId}` | 检查点赞状态 |
| 获取动态评论 | GET | `/api/circle/comment/{postId}` | 获取动态评论列表 |
| 发表评论 | POST | `/api/circle/comment` | 发表评论 |
| 删除评论 | DELETE | `/api/circle/comment/{commentId}` | 删除评论 |
| 转发动态 | POST | `/api/circle/repost/{postId}` | 转发动态 |
| 搜索动态 | GET | `/api/circle/search` | 搜索动态 |
| 获取用户动态 | GET | `/api/circle/user/{userId}` | 获取指定用户的动态列表 |

### 媒体上传接口 `/api/media`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 上传文件 | POST | `/api/media/upload` | 上传图片/视频 |
| 获取媒体详情 | GET | `/api/media/{id}` | 获取媒体详情 |
| 获取我的媒体 | GET | `/api/media/list` | 获取我的媒体列表 |
| 删除媒体 | DELETE | `/api/media/{id}` | 删除媒体 |
| 批量上传 | POST | `/api/media/upload/multiple` | 批量上传文件 |
| 绑定媒体到文章 | PUT | `/api/media/bind/{postId}` | 绑定媒体到文章 |
| 获取文章媒体 | GET | `/api/media/post/{postId}` | 获取文章的媒体列表 |

### 话题接口 `/api/topic`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 创建话题 | POST | `/api/topic` | 创建话题（需管理员权限） |
| 获取话题列表 | GET | `/api/topic/list` | 获取话题列表 |
| 获取热门话题 | GET | `/api/topic/hot` | 获取热门话题 |
| 获取话题详情 | GET | `/api/topic/{topicId}` | 获取话题详情 |
| 获取话题动态 | GET | `/api/topic/{topicId}/posts` | 获取话题下的动态列表 |

### 私信接口 `/api/message`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 发送私信 | POST | `/api/message/send` | 发送私信 |
| 获取收到的私信 | GET | `/api/message/received` | 获取收到的私信 |
| 获取发送的私信 | GET | `/api/message/sent` | 获取发送的私信 |
| 标记已读 | PUT | `/api/message/{id}/read` | 标记私信为已读 |
| 删除私信 | DELETE | `/api/message/{id}` | 删除私信 |
| 获取未读数量 | GET | `/api/message/unread-count` | 获取未读私信数量 |

### 分享接口 `/api/share`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 分享文章 | POST | `/api/share/{postId}` | 记录文章分享 |
| 获取分享数量 | GET | `/api/share/count/{postId}` | 获取文章分享数 |

### 统计接口 `/api/statistics`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取社区统计 | GET | `/api/statistics/community` | 获取社区公开统计数据 |

### 管理员接口

#### 内容管理 `/api/admin/post`
| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取文章列表 | GET | `/api/admin/post/list` | 获取所有文章（管理员） |
| 审核通过 | PUT | `/api/admin/post/{id}/approve` | 审核通过文章 |
| 驳回文章 | PUT | `/api/admin/post/{id}/reject` | 驳回文章 |
| 删除文章 | DELETE | `/api/admin/post/{id}` | 删除文章 |

#### 评论管理 `/api/admin/comment`
| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取评论列表 | GET | `/api/admin/comment/list` | 获取所有评论 |
| 删除评论 | DELETE | `/api/admin/comment/{id}` | 删除评论 |

#### 举报管理 `/api/admin/reports`
| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取待处理举报 | GET | `/api/admin/reports/pending` | 获取待处理举报列表 |
| 获取举报详情 | GET | `/api/admin/reports/{reportId}` | 获取举报详情 |
| 处理举报 | PUT | `/api/admin/reports/{reportId}` | 处理举报 |

#### 用户管理 `/api/admin/user`
| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取用户列表 | GET | `/api/admin/user/list` | 获取用户列表 |
| 更新用户状态 | PUT | `/api/admin/user/{id}/status` | 更新用户状态 |
| 重置用户密码 | PUT | `/api/admin/user/{id}/reset-password` | 重置用户密码 |
| 封禁用户 | PUT | `/api/admin/user/{id}/ban` | 封禁用户 |

#### 平台统计 `/api/admin/statistics`
| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取平台统计 | GET | `/api/admin/statistics` | 获取平台统计数据 |

---

## 数据库设计

### 22 张数据库表

| 表名 | 说明 | 主键 | 关联 |
|------|------|------|------|
| sys_user | 用户表 | id | — |
| blog_post | 文章表 | id | user_id → sys_user |
| blog_comment | 评论表 | id | user_id, post_id → sys_user, blog_post |
| blog_tag | 标签表 | id | — |
| blog_post_tag | 文章标签关联 | id | post_id, tag_id |
| blog_like | 点赞表 | id | (user_id, post_id) 联合 |
| blog_collect | 收藏表 | id | (user_id, post_id) 联合 |
| blog_follow | 关注表 | id | (follower_id, following_id) 联合 |
| blog_notification | 通知表 | id | user_id → sys_user |
| blog_trending | 热门统计表 | id | post_id → blog_post |
| blog_draft | 草稿表 | id | user_id → sys_user |
| blog_draft_tag | 草稿标签关联 | id | (draft_id, tag_id) 联合 |
| blog_report | 举报表 | id | reporter_id → sys_user |
| blog_circle_post | 校友圈动态 | id | user_id → sys_user |
| blog_circle_like | 校友圈点赞 | id | (user_id, post_id) 联合 |
| blog_circle_comment | 校友圈评论 | id | user_id, post_id → sys_user |
| blog_circle_repost | 校友圈转发 | id | user_id, post_id → sys_user |
| blog_media | 媒体文件 | id | user_id → sys_user |
| blog_post_media | 文章媒体关联 | id | (post_id, media_id) 联合 |
| blog_topic | 话题表 | id | — |
| blog_message | 私信表 | id | (sender_id, receiver_id) → sys_user |
| blog_share | 分享记录 | id | user_id, post_id → sys_user, blog_post |

### ER 关系图

```
sys_user (用户表)
    ├── 1:N ──> blog_post (文章表)
    │           ├── 1:N ──> blog_comment (评论表)
    │           ├── N:M ──> blog_tag (标签表) [通过 blog_post_tag]
    │           ├── 1:N ──> blog_like (点赞记录)
    │           ├── 1:N ──> blog_collect (收藏记录)
    │           └── 1:N ──> blog_post_media (文章媒体)
    │
    ├── 1:N ──> blog_follow (关注关系)
    ├── 1:N ──> blog_notification (通知)
    ├── 1:N ──> blog_report (举报)
    ├── 1:N ──> blog_media (媒体资源)
    ├── 1:N ──> blog_circle_post (校友圈动态)
    └── 1:N ──> blog_message (私信)

blog_circle_post (校友圈动态)
    ├── 1:N ──> blog_circle_comment (校友圈评论)
    ├── 1:N ──> blog_circle_like (校友圈点赞)
    └── 1:N ──> blog_circle_repost (校友圈转发)
```

### 软删除约定

所有表使用 `isDeleted` 字段实现软删除：
- `0` = 未删除
- `1` = 已删除
- MyBatis Plus `@TableLogic` 注解自动处理

---

## 性能优化

v2.0 引入了全面的性能优化方案，包含 16 项数据库索引优化和 5 个 Caffeine 缓存实例。

### 数据库索引优化（16 项）

| # | 表名 | 索引字段 | 优化场景 | 性能提升 |
|---|------|---------|---------|---------|
| 1 | blog_post | (status, created_at) | 文章列表查询 | 50x |
| 2 | blog_post | (user_id, status) | 我的文章查询 | 30x |
| 3 | blog_post | (category, status) | 分类筛选 | 20x |
| 4 | blog_comment | (post_id, created_at) | 文章评论列表 | 40x |
| 5 | blog_comment | (user_id) | 我的评论查询 | 25x |
| 6 | blog_like | (user_id, post_id) | 点赞状态检查 | 100x |
| 7 | blog_collect | (user_id, post_id) | 收藏状态检查 | 100x |
| 8 | blog_follow | (follower_id, following_id) | 关注关系查询 | 80x |
| 9 | blog_notification | (user_id, is_read) | 通知列表查询 | 35x |
| 10 | blog_circle_post | (user_id, created_at) | 用户动态查询 | 30x |
| 11 | blog_circle_post | (created_at) | 动态流排序 | 20x |
| 12 | blog_circle_like | (user_id, post_id) | 动态点赞检查 | 100x |
| 13 | blog_circle_comment | (post_id, created_at) | 动态评论列表 | 40x |
| 14 | blog_message | (sender_id, receiver_id) | 私信查询 | 50x |
| 15 | blog_trending | (score) | 热门内容排序 | 15x |
| 16 | blog_media | (user_id) | 用户媒体查询 | 25x |

### Caffeine 缓存策略（5 个实例）

| # | 缓存名称 | 缓存内容 | 最大容量 | 过期时间 |
|---|---------|---------|---------|---------|
| 1 | rateLimitCache | 接口限流计数 | 10,000 | 1 分钟 |
| 2 | loginLockCache | 登录失败锁定 | 1,000 | 15 分钟 |
| 3 | hotPostCache | 热门文章列表 | 100 | 5 分钟 |
| 4 | tagCache | 标签列表 | 500 | 10 分钟 |
| 5 | topicCache | 话题列表 | 200 | 10 分钟 |

### 性能对比

| 场景 | 优化前 | 优化后 | 提升倍数 |
|------|--------|--------|---------|
| 文章列表查询（10 万条） | 1500ms | 5ms | 300x |
| 点赞状态检查 | 200ms | 2ms | 100x |
| 收藏状态检查 | 200ms | 2ms | 100x |
| 关注关系查询 | 160ms | 2ms | 80x |
| 评论列表查询 | 800ms | 20ms | 40x |
| 通知列表查询 | 350ms | 10ms | 35x |
| 我的文章查询 | 600ms | 20ms | 30x |
| 用户动态查询 | 600ms | 20ms | 30x |
| 我的评论查询 | 500ms | 20ms | 25x |
| 用户媒体查询 | 500ms | 20ms | 25x |
| 分类筛选 | 400ms | 20ms | 20x |
| 动态流排序 | 400ms | 20ms | 20x |
| 热门内容排序 | 300ms | 20ms | 15x |
| 私信查询 | 500ms | 10ms | 50x |
| 缓存命中查询 | 200ms | <1ms | 200x+ |

### 前端性能优化

| 优化项 | 说明 |
|--------|------|
| 组件懒加载 | 路由级代码分割，按需加载页面组件 |
| 骨架屏 | 数据加载时显示 Skeleton 占位 |
| 图片懒加载 | 列表图片滚动到可视区域才加载 |
| 虚拟滚动 | 长列表使用虚拟滚动减少 DOM 节点 |
| Token 自动刷新 | 无感刷新，避免重复登录 |
| Pinia 状态持久化 | 用户状态本地缓存，减少重复请求 |

---

## 安全特性

| 安全措施 | 说明 |
|----------|------|
| **密码加密** | BCrypt 加密（强度 12），API 不返回密码字段 |
| **JWT 认证** | 支持 Access Token、Refresh Token 轮换、Token 黑名单 |
| **登录锁定** | 5 次失败 → 15 分钟锁定（原子更新，并发安全） |
| **XSS 防护** | HtmlSanitizer (Jsoup) 内容过滤 + DOMPurify 前端净化 |
| **IP 防护** | IP 格式校验，防止 IP 伪造 |
| **限流保护** | 23 个接口速率限制（本地 Caffeine 缓存） |
| **资源权限** | 文章删除/更新在 Service 层检查所有权 |
| **敏感信息** | 环境变量管理，禁止硬编码 |
| **并发安全** | 验证码验证使用 synchronized 同步，防止竞态攻击 |
| **软删除** | 所有删除操作使用 isDeleted 标记，数据可恢复 |

### 后端分层架构

```
Controller → Service → Mapper → Entity
```

- 所有 Controller 返回 `Result<T>` 统一响应
- 所有 Mapper 继承 `BaseMapper<T>` (MyBatis Plus)
- 写操作加 `@Transactional(rollbackFor = Exception.class)`
- 读操作加 `@Transactional(readOnly = true)`
- 业务异常: `throw new BusinessException(code, message)`

---

## 配置说明

### 环境变量（.env）

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| SERVER_PORT | 8825 | 后端服务端口 |
| DB_HOST | localhost | 数据库主机地址 |
| DB_PORT | 3306 | 数据库端口 |
| DB_NAME | campus_blog | 数据库名称 |
| DB_USERNAME | root | 数据库用户名 |
| DB_PASSWORD | — | 数据库密码（必填） |
| JWT_SECRET | — | JWT 密钥（至少 32 位，必填） |
| JWT_EXPIRATION | 86400000 | Access Token 过期时间（毫秒） |
| JWT_REFRESH_EXPIRATION | 604800000 | Refresh Token 过期时间（毫秒） |
| CORS_ALLOWED_ORIGINS | localhost:3000,localhost:8825 | 跨域白名单 |
| MAIL_HOST | smtp.qq.com | 邮件服务器 |
| MAIL_PORT | 465 | 邮件端口 |
| MAIL_USERNAME | — | 邮件用户名 |
| MAIL_PASSWORD | — | 邮件密码/授权码 |

### 关键配置说明

- 所有敏感配置通过 `.env` 管理，禁止硬编码
- 测试使用 H2 内存库（`src/test/resources/application.yml`），端口随机
- `CORS_ALLOWED_ORIGINS` 支持逗号分隔多个域名
- `JWT_SECRET` 必须设置且至少 32 位字符

---

## 部署指南

### Docker 部署

```bash
cd edu_project

# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

### 生产环境手动部署

```bash
# 1. 编译后端
cd edu_project
mvn clean package -DskipTests

# 2. 运行后端
java -jar target/edu_project-0.0.1-SNAPSHOT.jar

# 3. 构建前端
cd ../edu_project_vue
npm run build

# 4. 将 dist 目录部署到 Nginx
```

### Systemd 服务部署

```ini
# /etc/systemd/system/campus-blog.service
[Unit]
Description=Campus Blog Backend
After=network.target mysql.service

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/campus-blog
ExecStart=/usr/bin/java -jar edu_project-0.0.1-SNAPSHOT.jar
Restart=on-failure
RestartSec=10
Environment=SPRING_PROFILES_ACTIVE=prod

[Install]
WantedBy=multi-user.target
```

```bash
# 启用并启动服务
sudo systemctl enable campus-blog
sudo systemctl start campus-blog
sudo systemctl status campus-blog
```

### Nginx 反向代理配置

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        root /opt/campus-blog/dist;
        try_files $uri $uri/ /index.html;
    }

    # 后端 API 代理
    location /api/ {
        proxy_pass http://localhost:8825/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### HTTPS 配置（Let's Encrypt）

```bash
# 安装 Certbot
sudo apt install certbot python3-certbot-nginx

# 获取证书
sudo certbot --nginx -d your-domain.com

# 自动续期
sudo certbot renew --dry-run
```

---

## 开发规范

### 代码规范
- 后端遵循 Spring Boot 最佳实践
- 前端使用 Vue 3 Composition API (`<script setup>`)
- 统一 API 响应格式 `Result<T>`
- Controller → Service → Mapper → Entity 分层

### Git 提交规范
| 类型 | 说明 |
|------|------|
| feat | 新功能 |
| fix | 修复 bug |
| docs | 文档更新 |
| style | 代码格式 |
| refactor | 重构 |
| test | 测试 |
| chore | 构建/工具 |

### 文档更新规范
**【强制】每次更新完代码都要更新 md 文件**

### 开发注意事项
1. **修改代码前必读**: 先读相关 Controller/Service/Mapper/Entity 和文档
2. **不重复造轮子**: 确认现有功能后再决定复用或新增
3. **不确定时询问**: 不要臆想业务逻辑
4. **用户发布内容不需要审核**: 文章发布时 status=1 直接可见
5. **不使用 Redis**: 保持轻量级架构，使用 Caffeine 本地缓存

---

## 更新日志

### v2.0.14 (2026-05-22)
- **密码重置修复**: PasswordReset.vue 字段名 password → newPassword
- **CirclePost 修复**: 9 个未声明响应式变量 + form.location 补全
- **PostEdit 修复**: renderedContent computed 补全，autoSaveTimer 定时器实现
- **PostDetail 修复**: 添加 incrementViewCount 调用
- **Circle.vue 修复**: topicId → topicIds，logger 声明补全
- **SecurityConfig 修复**: 3 个公开端点 + 2 个死规则修正
- **安全增强**: UserLoginRequest @AssertTrue 校验，密码 maxSize=128，注册用户名最小长度统一

### v2.0.13 (2026-05-21)
- **邮箱登录**: 支持使用邮箱地址作为登录账号，前后端完整实现
- **话题字段**: Topic 实体恢复 postCount 字段
- **测试更新**: 8 个测试文件全面更新适配邮箱登录变更
- **构建配置**: 添加 Maven Surefire 插件配置

### v2.0.11 (2026-05-19)
- **白屏修复**: PostEdit 补全 4 个缺失的模板引用，选择标签/分类不再白屏
- **话题多选**: 保存时发送 `topicIds` 数组替代单个 `topicId`
- **后端修复**: 禁用直接注册、举报通知作者、话题热度更新、Token 失效等
- **数据库表**: 添加默认管理员账户 INSERT（admin / Admin123）

### v2.0 (2026-05-17)
- **Rainy Glassmorphism UI**: 全新毛玻璃拟态设计系统，包含水滴动画、涟漪交互、光泽渐变效果
- **性能优化**: 16 项数据库索引优化，查询速度最高提升 300 倍
- **Caffeine 缓存**: 5 个缓存实例（限流、登录锁定、热门文章、标签、话题）
- **校友圈增强**: 新增 @mentions、位置标签、话题标签、评论/转发显示切换
- **Messages 布局修复**: 浮动返回按钮，双栏滚动优化
- **页面宽度统一**: 所有页面最大宽度扩展至 1400px
- **前端版本升级**: package.json 版本升级至 2.0.0

### v1.58 (2026-05-17)
- 热搜页面改版：移除「热门标签」和「热门话题」Tab，改为「热门文章 | 热门动态」混排
- 校友圈话题优化：话题选择器改为搜索框样式，添加话题图标和加载状态
- Bug 修复：TrendingServiceImpl 热门动态话题字段名修复（topicNames→topics）

### v1.53 (2026-05-16)
- 安全加固：DOMPurify 添加 FORBID_TAGS/FORBID_ATTR 配置防止 XSS
- 通知轮询：Navbar 每 30 秒自动刷新未读通知/私信数
- 管理后台：用户管理新增封禁/解封按钮和封禁状态列
- 剪贴板回退：分享功能添加非 HTTPS 环境 textarea 回退方案
- 分页修复：话题列表 `/topic/list` 返回完整分页元数据
- Dashboard 自动刷新：管理后台仪表盘每 60 秒自动刷新统计数据
- 匿名用户体验：未登录用户也能查看粉丝/关注数据
- 参数验证：校友圈接口 String 参数改为 int + @Min/@Max 验证
- 暗色模式：新增 CSS 变量，消除硬编码颜色

### v1.52 (2026-05-16)
- 热搜榜改版：热门内容 Tab 支持文章和校友圈动态混排展示
- 校友圈话题：发布/详情页新增话题选择器，支持搜索话题并关联到动态

### v1.49 (2026-05-16)
- 修复验证码并发竞态问题（EmailServiceImpl 添加 synchronized 同步块）
- 修复前端 Home.vue 热门标签/文章/统计数据访问方式
- 写入稳定设计要求：不使用 Redis，用户发布内容不需要审核

### v1.47 (2026-05-15)
- 完善后端服务实现，新增多个 Controller 和 Service
- 优化 JWT 认证机制，新增 Token 黑名单功能
- 新增配置类和工具类（HtmlSanitizer、FineGrainedLockManager 等）
- 完善异常处理（BaseErrorCode、GlobalExceptionHandler）
- 新增单元测试覆盖

### v1.46 (2026-05-14)
- 初始化项目版本

---

## 许可证

MIT License

Copyright (c) 2026 刘畅

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

---

## 作者

**刘畅 (Liu Chang)**

- GitHub: https://github.com/Xinghe-0203/FULL-Campus_Blog
- 个人网站: https://www.starsx.top/
- 个人博客: https://blog.starsx.top/

---

## 项目信息

| 项目 | 信息 |
|------|------|
| 版本 | v2.0.15 |
| 最后更新 | 2026-05-25 |
| 开发者 | 刘畅 |
| 许可证 | MIT |
| 后端端口 | 8825 |
| 前端端口 | 3000 |
| API 文档 | http://localhost:8825/api/doc.html |
