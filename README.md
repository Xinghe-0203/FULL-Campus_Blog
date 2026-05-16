# 校园博客论坛系统 / Campus Blog Forum System

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Version](https://img.shields.io/badge/version-v1.50-blue)](https://github.com/Xinghe-0203/FULL-Campus_Blog)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-green)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4.21-brightgreen)](https://vuejs.org/)

> **MIT License** - 允许自由使用、复制、修改、合并、发布、分发、再授权和销售本软件的副本。

一个基于 **Spring Boot 3 + Vue 3 + MyBatis Plus** 的全栈校园博客论坛系统，支持文章发布、校友圈动态、点赞收藏、关注互动、消息通知等功能。

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
- [安全特性](#安全特性)
- [部署指南](#部署指南)
- [开发规范](#开发规范)
- [15个Agent团队审计报告](#15个agent团队审计报告)
- [许可证](#许可证)

---

## 功能特性

### 用户模块
- 用户注册/登录 (JWT Token 认证)
- 个人资料编辑、头像上传、封面图上传
- 修改密码、密码找回（邮箱验证码）
- 登录失败锁定（5次失败 → 15分钟锁定）
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

### 校友圈模块
- 发布动态（支持图片）
- 点赞/评论/转发
- 推荐/关注动态流
- 可见性控制（公开/好友/私密）
- 用户动态列表

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

---

## 技术栈

### 后端
| 组件 | 版本 | 说明 |
|------|------|------|
| Java | 21 | LTS 版本 |
| Spring Boot | 3.3.0 | Web 框架 |
| MyBatis Plus | 3.5.8 | ORM 框架 |
| Spring Security | 6.x | 安全认证 |
| JWT (JJWT) | 0.12.3 | Token 认证 |
| Knife4j | 4.5.0 | API 文档 |
| MySQL | 8.x | 数据库 |
| Caffeine | 3.1.8 | 本地缓存 |

### 前端
| 组件 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.21 | UI 框架 |
| Vite | 5.2.0 | 构建工具 |
| Vue Router | 4.3.0 | 路由管理 |
| Pinia | 2.1.7 | 状态管理 |
| Axios | 1.6.8 | HTTP 客户端 |
| Marked | 12.0.1 | Markdown 渲染 |

---

## 快速开始

### 环境要求
- JDK 21+
- Node.js 18+
- MySQL 8.0+

### 1. 克隆项目

```bash
git clone https://github.com/Xinghe-0203/FULL-Campus_Blog.git
cd FULL-Campus_Blog
```

### 2. 数据库初始化

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE campus_blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_blog;

# 导入表结构（Windows）
SOURCE edu_project/数据库表.sql;

# 或在命令行中执行
mysql -u root -p campus_blog < edu_project/数据库表.sql
```

### 3. 后端启动

```bash
cd edu_project

# 复制环境变量模板
copy .env.example .env    # Windows
# cp .env.example .env   # Linux/Mac

# 修改 .env 文件，填入实际配置值
# DB_HOST=localhost
# DB_PORT=3306
# DB_NAME=campus_blog
# DB_USERNAME=root
# DB_PASSWORD=your_password
# JWT_SECRET=your_secret_key_at_least_32_characters

# 启动后端
mvn spring-boot:run
```

后端启动后访问：
- API: http://localhost:8825/api
- API 文档: http://localhost:8825/api/doc.html (Knife4j)

### 4. 前端启动

```bash
cd edu_project_vue

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端启动后访问：http://localhost:3000

### 5. 默认管理员账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |

---

## 项目结构

```
FULL-Campus_Blog/
├── edu_project/                    # 后端 (Spring Boot 3 + MyBatis Plus + Java 21)
│   ├── src/main/java/
│   │   └── com.example.edu_project/
│   │       ├── controller/          # 23个 Controller
│   │       ├── service/            # 17个 Service 接口
│   │       ├── service/impl/      # 17个 Service 实现
│   │       ├── mapper/            # 22个 Mapper 接口
│   │       ├── entity/            # 22个 Entity 实体
│   │       ├── config/           # 配置类 (Security, JWT, Cache, Async等)
│   │       ├── dto/              # 数据传输对象
│   │       ├── vo/               # 视图对象
│   │       ├── event/            # 事件类
│   │       └── utils/            # 工具类
│   ├── src/main/resources/
│   │   └── application.yml       # 主配置
│   ├── src/test/                  # 测试
│   ├── pom.xml                   # Maven 配置
│   ├── Dockerfile               # Docker 镜像
│   ├── docker-compose.yml       # Docker 编排
│   ├── .env.example            # 环境变量模板
│   └── 数据库表.sql             # 22张数据库表
│
└── edu_project_vue/               # 前端 (Vue 3 + Vite)
    ├── src/
    │   ├── api/                  # 17个 API 模块
    │   ├── views/                # 30个页面组件
    │   ├── components/          # 通用组件
    │   ├── stores/              # Pinia 状态管理
    │   ├── router/              # 路由配置
    │   └── styles/              # 样式文件
    ├── package.json
    └── vite.config.js
```

---

## API 接口文档

> 完整API文档（115个端点）已集成到 Knife4j，访问 http://localhost:8825/api/doc.html

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

### 认证接口 `/api/auth`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 用户注册 | POST | `/api/auth/register` | 注册新用户 |
| 用户登录 | POST | `/api/auth/login` | 登录获取Token |
| 发送验证码 | POST | `/api/auth/password/send-code` | 发送密码重置验证码 |
| 重置密码 | PUT | `/api/auth/password/reset-password` | 使用验证码重置密码 |

**请求示例 - 注册**
```json
POST /api/auth/register
{
  "username": "user001",
  "password": "password123",
  "email": "user@example.com"
}
```

**请求示例 - 登录**
```json
POST /api/auth/login
{
  "username": "user001",
  "password": "password123"
}
```

**响应示例 - 登录**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "userId": 1,
    "username": "user001",
    "nickname": "用户001",
    "avatar": "https://example.com/avatar.png",
    "role": "user",
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

### 用户接口 `/api/user`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 刷新Token | POST | `/api/user/refresh` | 使用refreshToken刷新accessToken |
| 获取用户信息 | GET | `/api/user/{id}` | 根据ID获取用户信息 |
| 修改密码 | PUT | `/api/user/password` | 修改当前用户密码 |
| 搜索用户 | GET | `/api/user/search` | 搜索用户 |
| 退出登录 | POST | `/api/user/logout` | 退出登录（Token加入黑名单） |
| 获取设备列表 | GET | `/api/user/devices` | 获取当前用户的登录设备列表 |
| 退出其他设备 | POST | `/api/user/logout-other-devices` | 强制登出其他设备 |
| 更新个人资料 | PUT | `/api/user/profile` | 更新昵称、简介等 |
| 更新头像 | PUT | `/api/user/avatar` | 上传并更新头像 |
| 更新封面图 | PUT | `/api/user/cover-image` | 上传并更新封面图 |

**请求示例 - 搜索用户**
```
GET /api/user/search?keyword=张三&page=1&pageSize=10
```

**请求示例 - 更新个人资料**
```json
PUT /api/user/profile
{
  "nickname": "新昵称",
  "bio": "个人简介"
}
```

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

**请求示例 - 发布文章**
```json
POST /api/post
{
  "title": "Spring Boot 最佳实践",
  "summary": "本文介绍Spring Boot开发中的最佳实践...",
  "content": "# Spring Boot 最佳实践

## 1. 配置管理

...",
  "coverUrl": "https://example.com/cover.jpg",
  "category": "技术",
  "tagIds": [1, 2, 3]
}
```

**请求示例 - 获取文章列表**
```
GET /api/post/list?page=1&pageSize=10&sort=latest
GET /api/post/list?page=1&pageSize=10&sort=hot&category=技术
```

**请求示例 - 高级搜索**
```
GET /api/post/search/advanced?keyword=Spring&tagIds=1,2&category=技术&startDate=2026-01-01&endDate=2026-05-15&page=1&pageSize=10
```

### 评论接口 `/api/comment`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 发表评论 | POST | `/api/comment` | 对文章发表评论 |
| 获取文章评论 | GET | `/api/comment/post/{postId}` | 获取文章的评论列表（树形结构） |
| 删除评论 | DELETE | `/api/comment/{id}` | 删除评论（软删除） |
| 获取我的评论 | GET | `/api/comment/my` | 获取当前用户的评论列表 |

**请求示例 - 发表评论**
```json
POST /api/comment
{
  "postId": 1,
  "parentId": null,
  "content": "写的很好，收藏了！"
}
```

**请求示例 - 回复评论**
```json
POST /api/comment
{
  "postId": 1,
  "parentId": 123,
  "content": "谢谢你的回复！"
}
```

### 点赞接口 `/api/like`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 点赞/取消点赞 | POST | `/api/like/{postId}` | 切换点赞状态 |
| 检查点赞状态 | GET | `/api/like/check/{postId}` | 检查当前用户是否已点赞 |
| 获取我的点赞 | GET | `/api/like/my` | 获取当前用户的点赞列表 |
| 批量检查点赞状态 | POST | `/api/like/check/batch` | 批量检查多个文章的点赞状态 |

**请求示例 - 批量检查点赞状态**
```json
POST /api/like/check/batch
{
  "postIds": [1, 2, 3, 4, 5]
}
```

**响应示例**
```json
{
  "code": 200,
  "data": {
    "results": [true, false, true, false, true]
  }
}
```

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
| 获取热门标签 | GET | `/api/trending/hot-tags` | 获取热门标签列表 |
| 更新文章热度 | PUT | `/api/trending/update/{postId}` | 更新文章热度分数 |

### 草稿接口 `/api/post/draft`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 保存草稿 | POST | `/api/post/draft` | 保存文章草稿 |
| 获取最新草稿 | GET | `/api/post/draft/latest` | 获取最新草稿 |
| 删除草稿 | DELETE | `/api/post/draft/{draftId}` | 删除草稿 |
| 获取指定草稿 | GET | `/api/post/draft/{draftId}` | 获取指定草稿详情 |

**请求示例 - 保存草稿**
```json
POST /api/post/draft
{
  "title": "草稿标题",
  "content": "草稿内容...",
  "tagIds": [1, 2]
}
```

### 举报接口 `/api/report`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 举报内容 | POST | `/api/report` | 举报违规内容 |
| 获取我的举报 | GET | `/api/report/my` | 获取我的举报记录 |

**请求示例 - 举报**
```json
POST /api/report
{
  "targetType": "post",
  "targetId": 1,
  "reason": "垃圾广告"
}
```

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

**请求示例 - 发布动态**
```json
POST /api/circle/post
{
  "content": "今天天气真好！",
  "mediaIds": [1, 2, 3],
  "visibility": "public",
  "topicId": 1
}
```

**visibility可选值**: `public`(公开), `friends`(好友), `private`(私密)

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

**请求示例 - 发送私信**
```json
POST /api/message/send
{
  "receiverId": 1,
  "content": "你好，请问..."
}
```

### 分享接口 `/api/share`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 分享文章 | POST | `/api/share/{postId}` | 记录文章分享 |
| 获取分享数量 | GET | `/api/share/count/{postId}` | 获取文章分享数 |

### 统计接口 `/api/statistics`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取社区统计 | GET | `/api/statistics/community` | 获取社区公开统计数据 |

**响应示例**
```json
{
  "code": 200,
  "data": {
    "userCount": 1000,
    "postCount": 5000,
    "commentCount": 15000,
    "activeUsers": 500
  }
}
```

### 管理员接口

#### 内容管理 `/api/admin/post`
| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取文章列表 | GET | `/api/admin/post/list` | 获取所有文章（管理员） |
| 审核通过 | PUT | `/api/admin/post/{id}/approve` | 审核通过文章 |
| 驳回文章 | PUT | `/api/admin/post/{id}/reject` | 驳回文章 |
| 删除文章 | DELETE | `/api/admin/post/{id}` | 删除文章 |

**请求示例 - 驳回文章**
```json
PUT /api/admin/post/1/reject
{
  "reason": "包含违规内容"
}
```

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

### 22张数据库表

| 表名 | 说明 | 主键 | 关联 |
|------|------|------|------|
| sys_user | 用户表 | id | - |
| blog_post | 文章表 | id | user_id → sys_user |
| blog_comment | 评论表 | id | user_id, post_id → sys_user, blog_post |
| blog_tag | 标签表 | id | - |
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
| blog_topic | 话题表 | id | - |
| blog_message | 私信表 | id | (sender_id, receiver_id) → sys_user |
| blog_share | 分享记录 | id | user_id, post_id → sys_user, blog_post |

### ER关系图

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

---

## 安全特性

| 安全措施 | 说明 |
|----------|------|
| **密码加密** | BCrypt 加密（强度12），API不返回密码字段 |
| **JWT认证** | 支持Access Token、Refresh Token轮换、Token黑名单 |
| **登录锁定** | 5次失败 → 15分钟锁定（原子更新，并发安全） |
| **XSS防护** | HtmlSanitizer (Jsoup) 内容过滤 |
| **IP防护** | IP格式校验，防止IP伪造 |
| **限流保护** | 23个接口速率限制（本地Caffeine缓存） |
| **资源权限** | 文章删除/更新在Service层检查所有权 |
| **敏感信息** | 环境变量管理，禁止硬编码 |
| **并发安全** | 验证码验证使用synchronized同步，防止竞态攻击 |

---

## 核心设计

### 缓存策略

- **Caffeine 本地缓存**: 1000条记录，5分钟过期
- 用于速率限制、热门内容缓存
- **不使用 Redis**（保持轻量级架构，生产环境可按需引入）

### 内容发布策略

- **用户发布内容不需要审核**：文章发布时 `status=1`（已发布），直接可见
- 管理员保留审核功能（`status=0` 待审核），但默认不启用

---

## 部署指南

### Docker部署

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
# 编译后端
cd edu_project
mvn clean package -DskipTests

# 运行
java -jar target/edu_project-0.0.1-SNAPSHOT.jar

# 构建前端
cd ../edu_project_vue
npm run build
# 将 dist 目录部署到 Nginx
```

---

## 开发规范

### 代码规范
- 后端遵循 Spring Boot 最佳实践
- 前端使用 Vue 3 Composition API (`<script setup>`)
- 统一API响应格式 `Result<T>`
- Controller → Service → Mapper → Entity 分层

### Git提交规范
| 类型 | 说明 |
|------|------|
| feat | 新功能 |
| fix | 修复bug |
| docs | 文档更新 |
| style | 代码格式 |
| refactor | 重构 |
| test | 测试 |
| chore | 构建/工具 |

### 文档更新规范
**【强制】每次更新完代码都要更新md文件**

---

## 15个Agent团队审计报告

| 审计维度 | 评分 | 主要发现 |
|---------|------|---------|
| 后端架构 | 8.5/10 | 分层清晰，22张表设计完整，字段命名略有不一致 |
| 前端架构 | 7/10 | 状态管理集中度高，API缺少类型定义 |
| 安全 | 7.5/10 | JWT实现规范，Refresh Token轮换待加强 |
| 数据库 | 8.5/10 | 设计规范，软删除统一，FULLTEXT已实现 |
| API设计 | 8/10 | 统一Result封装，路径和分页参数略有不统一 |
| 性能 | 7/10 | 异步配置合理，缺少分布式缓存 |
| 测试覆盖 | 4/10 | 基础单元测试良好，Controller/Mapper完全缺失 |
| 配置部署 | 8/10 | 环境变量管理规范，依赖版本部分偏旧 |
| UI/UX | 7/10 | CSS变量完善，响应式断点单一 |
| 业务逻辑 | 7/10 | 核心流程完整，审核流程未实际使用 |
| 认证授权 | 7.5/10 | JWT规范，资源级权限控制部分实现 |
| 代码规范 | 8/10 | 分层清晰，注释一致性待改进 |
| 依赖管理 | 8/10 | 依赖精简，部分依赖建议升级到最新版本 |
| 文档完整性 | 5.5/10 | 文档基本完整，README内容待扩展 |

**综合评分: 7.5/10**

### 主要改进建议
1. **测试覆盖**: 补充Controller集成测试和Mapper层测试
2. **性能优化**: 引入Redis分布式缓存替代本地Caffeine
3. **前端架构**: 拆分user store，按功能领域创建专门stores
4. **API规范**: 统一分页参数命名（pageNum/pageSize）
5. **Refresh Token**: 实现Refresh Token轮换机制

---

## 许可证

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

## 更新日志

### v1.50 (2026-05-16)

### v1.49 (2026-05-16)
- 修复验证码并发竞态问题（EmailServiceImpl添加synchronized同步块）
- 修复前端Home.vue热门标签/文章/统计数据访问方式
- **写入稳定设计要求**：
  - 不使用Redis（保持Caffeine本地缓存）
  - 用户发布内容不需要审核（status=1直接发布）
- 更新全部md文档，版本统一为v1.47

### v1.47 (2026-05-15)
- 完善后端服务实现，新增多个Controller和Service
- 优化JWT认证机制，新增Token黑名单功能
- 新增配置类和工具类（HtmlSanitizer、FineGrainedLockManager等）
- 完善异常处理（BaseErrorCode、GlobalExceptionHandler）
- 新增单元测试覆盖（GlobalExceptionHandlerTest、JwtUtilsTest等）
- 更新前端API模块，优化用户状态管理

### v1.46 (2026-05-14)
- 初始化项目版本

---

## 项目信息

- **版本**: v1.50
- **最后更新**: 2026-05-16
- **开发者**: 刘畅
- **GitHub**: https://github.com/Xinghe-0203/FULL-Campus_Blog
- **API文档**: http://localhost:8825/api/doc.html (Knife4j)