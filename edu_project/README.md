# 校园博客论坛系统 — 后端

> Spring Boot 3 后端服务，为 Campus Blog 提供完整的 RESTful API。
>
> **当前版本**: v2.0 | **Java**: 21 | **Spring Boot**: 3.3.0

---

## 目录

- [项目概述](#项目概述)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [架构设计](#架构设计)
- [安全机制](#安全机制)
- [性能优化 v2.0](#性能优化-v20)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [数据库初始化](#数据库初始化)
- [API 文档](#api-文档)
- [测试](#测试)
- [Docker 部署](#docker-部署)
- [日志系统](#日志系统)
- [开发规范](#开发规范)
- [更新日志](#更新日志)

---

## 项目概述

校园博客论坛系统是一个面向高校师生的全栈博客与社区平台，后端基于 **Spring Boot 3** 构建，提供用户管理、文章发布、评论互动、校友圈、消息通知、数据统计等完整功能。

### 核心模块

| 模块 | 功能 |
|------|------|
| 用户系统 | 注册/登录、JWT 认证、个人资料、头像上传、密码找回 |
| 文章系统 | 发布/编辑/删除、Markdown 支持、标签分类、封面图、草稿箱 |
| 互动系统 | 点赞、收藏、评论/回复、关注/粉丝 |
| 校友圈 | 动态发布、图片上传、点赞评论、转发、推荐流 |
| 通知系统 | 点赞/评论/关注通知、未读计数、私信 |
| 搜索系统 | 全站搜索、高级搜索、热门文章、热门标签 |
| 管理后台 | 用户管理、文章审核、举报处理、数据统计 |
| 话题系统 | 话题创建、话题下文章聚合 |

### 规模统计

| 组件 | 数量 |
|------|------|
| Controller | 23 |
| Service | 17 |
| Mapper | 22 |
| Entity | 22 |
| API 端点 | 120+ |
| 数据库表 | 22 |
| 配置类 | 16 |

---

## 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| Java | 21 | LTS |
| Spring Boot | 3.3.0 | Web 框架 |
| MyBatis Plus | 3.5.8 | ORM |
| Spring Security | 6.x | 安全认证 |
| JJWT | 0.12.6 | JWT |
| Hutool | 5.8.40 | 工具库 |
| Caffeine | 3.2.0 | 本地缓存 |
| Lombok | 1.18.42 | 代码简化 |
| Thumbnailator | 0.4.20 | 图片压缩 |
| Knife4j | 4.5.0 | API 文档 |
| Jsoup | 1.18.3 | XSS 防护 |
| MySQL Connector | 8.x | 数据库驱动 |
| H2 Database | (test) | 测试内存库 |

---

## 项目结构

```
edu_project/
├── src/main/java/com/example/edu_project/
│   ├── controller/          # 23 个 Controller (Auth, User, Post, Comment, Like, Collect, Tag, Follow, Notification, Message, Circle, Topic, Report, Share, Media, Trending, Statistics, Admin*)
│   ├── service/             # 17 个 Service 接口 + impl
│   ├── mapper/              # 22 个 Mapper 接口
│   ├── entity/              # 22 个实体类
│   ├── dto/                 # 数据传输对象
│   ├── vo/                  # 视图对象
│   ├── common/              # Result, BusinessException, PageResult, HttpStatus
│   ├── config/              # 16 个配置类 (Security, Caffeine, MyBatis Plus, RateLimit, Async, etc.)
│   ├── util/                # SecurityUtils, JwtUtils, HtmlSanitizer, LogUtils, FileUploadUtils
│   └── EduProjectApplication.java
├── src/main/resources/
│   ├── application.yml
│   ├── application-test.yml         # 测试配置 (H2)
│   └── mapper/                      # MyBatis XML 映射文件
├── src/test/java/                   # 单元测试
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── .env.example
├── 数据库表.sql
└── README.md
```

---

## 架构设计

### 分层架构

```
Controller → Service → Mapper → Entity
```

### 核心约定

| 约定 | 说明 |
|------|------|
| 统一响应 | `Result<T>` (`Result.success(data)` / `Result.error(code, msg)`) |
| Mapper 继承 | `BaseMapper<T>` (MyBatis Plus) |
| 软删除 | `isDeleted` + `@TableLogic` (0=未删除, 1=已删除) |
| 事务-写 | `@Transactional(rollbackFor = Exception.class)` |
| 事务-读 | `@Transactional(readOnly = true)` |
| 业务异常 | `throw new BusinessException(code, message)` |
| 当前用户 | `SecurityUtils.getCurrentUserIdOrNull()` |

### 统一响应格式

```json
{ "code": 200, "message": "success", "data": { ... }, "timestamp": 1716000000000 }
```

分页响应中 `data` 包含 `records`, `total`, `size`, `current`, `pages` 字段。

---

## 安全机制

### 认证与授权

| 机制 | 实现 |
|------|------|
| 密码加密 | BCrypt (strength 12) |
| JWT Access Token | 有效期 24 小时 |
| JWT Refresh Token | 有效期 7 天，支持轮换 |
| Token 黑名单 | 登出时加入，防止重放 |
| 登录失败锁定 | 5 次失败 → 锁定 15 分钟 (原子更新) |

### 请求防护

| 防护 | 实现 |
|------|------|
| XSS 防护 | `HtmlSanitizer` (Jsoup) 净化 HTML |
| 限流保护 | `RateLimitInterceptor`，23 个端点 |
| IP 伪造防护 | `LogUtils.getClientIp()` 格式校验 |
| 参数校验 | Spring Validation (`@Valid`, `@NotBlank`) |
| CORS 控制 | 可配置白名单，生产禁止 `*` |

---

## 性能优化 v2.0

预期查询性能提升 **15-300 倍**。

### 数据库索引 (16 个)

覆盖高频查询: 用户文章列表、分类文章、热门/最新排序、评论列表、点赞/收藏/关注查询、通知列表、动态列表、草稿列表、待处理举报、私信对话等。

### Caffeine 本地缓存 (5 个)

| 缓存 | 内容 | 过期 |
|------|------|------|
| `hotTagsCache` | 热门标签 | 30 分钟 |
| `categoryCache` | 分类列表 | 1 小时 |
| `userCache` | 用户信息 | 30 分钟 |
| `trendingCache` | 热门统计 | 10 分钟 |
| `statsCache` | 系统统计 | 5 分钟 |

### 缓存注解

- `@Cacheable`: 12+ 查询方法
- `@CacheEvict`: 15+ 写操作方法

### 其他

- `@Async` 异步处理 (通知发送、统计更新)
- `PerformanceMonitoringAspect` 慢查询监控
- HikariCP 连接池

---

## 快速开始

### 环境要求

JDK 21 | Maven 3.8+ | MySQL 8.0+ | Node.js 18+ (前端)

### 1. 配置

```bash
cd edu_project
cp .env.example .env          # Linux/macOS
copy .env.example .env        # Windows
# 编辑 .env，至少设置 DB_PASSWORD 和 JWT_SECRET (至少 32 位)
```

### 2. 初始化数据库

```bash
mysql -u root -p
CREATE DATABASE campus_blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_blog;
SOURCE 数据库表.sql;
```

### 3. 启动后端

```bash
mvn spring-boot:run             # 开发模式
mvn clean package -DskipTests   # 生产打包
java -jar target/edu_project-0.0.1-SNAPSHOT.jar
```

访问: API http://localhost:8825/api | 文档 http://localhost:8825/api/doc.html | 健康检查 http://localhost:8825/api/actuator/health

### 4. 启动前端 (可选)

```bash
cd ../edu_project_vue && npm install && npm run dev
```

前端访问: http://localhost:3000

### 默认管理员: `admin` / `admin123`

---

## 配置说明

### 环境变量 (.env)

| 变量 | 默认值 | 必需 | 说明 |
|------|--------|------|------|
| `DB_HOST` | `localhost` | 是 | 数据库主机 |
| `DB_PORT` | `3306` | 是 | 数据库端口 |
| `DB_NAME` | `campus_blog` | 是 | 数据库名 |
| `DB_USERNAME` | `root` | 是 | 数据库用户名 |
| `DB_PASSWORD` | — | **是** | 数据库密码 |
| `JWT_SECRET` | — | **是** | JWT 密钥 (至少 32 位) |
| `JWT_EXPIRATION` | `86400000` | 否 | Access Token 有效期 (ms)，默认 24h |
| `JWT_REFRESH_EXPIRATION` | `604800000` | 否 | Refresh Token 有效期 (ms)，默认 7d |
| `SERVER_PORT` | `8825` | 否 | 服务端口 |
| `MAIL_HOST` | `smtp.example.com` | 否 | SMTP 服务器 |
| `MAIL_PORT` | `587` | 否 | SMTP 端口 |
| `MAIL_USERNAME` | — | 否 | 发件邮箱 |
| `MAIL_PASSWORD` | — | 否 | 邮箱密码/授权码 |
| `MAIL_FROM` | `noreply@example.com` | 否 | 发件人地址 |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:8825` | 否 | 跨域白名单 |
| `AVATAR_ALLOWED_DOMAINS` | `localhost,127.0.0.1` | 否 | 头像域名白名单 |
| `UPLOAD_MAX_FILE_SIZE` | `524288000` | 否 | 最大上传大小 (字节)，默认 500MB |
| `ENV_VALIDATION_ENABLED` | `true` | 否 | 是否启用环境变量验证 |

### 生产环境配置建议

```env
DB_HOST=your-production-db-host
DB_PASSWORD=<strong-random-password>
JWT_SECRET=<64-character-random-string>
CORS_ALLOWED_ORIGINS=https://yourdomain.com
ENV_VALIDATION_ENABLED=true
MAIL_HOST=smtp.your-provider.com
MAIL_USERNAME=noreply@yourdomain.com
MAIL_PASSWORD=<api-key>
MAIL_FROM=noreply@yourdomain.com
```

---

## 数据库初始化

完整建表脚本在 `数据库表.sql` 中，包含 22 张表的 DDL。

### 表清单

| 表名 | 说明 | 表名 | 说明 |
|------|------|------|------|
| `sys_user` | 用户表 | `blog_notification` | 通知表 |
| `blog_post` | 文章表 | `blog_trending` | 热门统计表 |
| `blog_comment` | 评论表 | `blog_draft` | 草稿表 |
| `blog_tag` | 标签表 | `blog_draft_tag` | 草稿标签关联 |
| `blog_post_tag` | 文章标签关联 | `blog_report` | 举报表 |
| `blog_like` | 点赞表 | `blog_circle_post` | 校友圈动态 |
| `blog_collect` | 收藏表 | `blog_circle_like` | 校友圈点赞 |
| `blog_follow` | 关注表 | `blog_circle_comment` | 校友圈评论 |
| `blog_message` | 私信表 | `blog_circle_repost` | 校友圈转发 |
| `blog_topic` | 话题表 | `blog_media` | 媒体文件 |
| `blog_share` | 分享记录 | `blog_post_media` | 文章媒体关联 |

### 软删除

所有涉及删除的表使用 `isDeleted` 字段: `0`=未删除, `1`=已删除。MyBatis Plus 通过 `@TableLogic` 自动处理。

---

## API 文档

启动后访问: **http://localhost:8825/api/doc.html**

Knife4j 提供 120+ API 端点的完整文档、请求/响应示例、在线调试、模型定义。

### API 模块分组

| 分组 | 端点数 | 分组 | 端点数 |
|------|--------|------|--------|
| Auth | 5 | Follow | 5 |
| User | 8 | Notification | 5 |
| Post | 12 | Message | 5 |
| Comment | 6 | Circle | 8 |
| Like | 4 | Topic | 5 |
| Collect | 4 | Report | 4 |
| Tag | 5 | Share | 3 |
| Media | 4 | Admin | 15+ |
| Trending | 3 | Statistics | 4 |

需要认证的接口: `Authorization: Bearer <access_token>`

---

## 测试

```bash
mvn test                        # 运行所有测试
mvn test -Dtest=AuthControllerTest  # 运行单个测试类
```

测试使用 H2 内存数据库 (配置见 `application-test.yml`)，端口随机，不影响生产数据。
测试目录: `src/test/java/` 包含 controller/, service/, mapper/, util/ 测试。

---

## Docker 部署

### 方式一: Docker Compose (推荐)

```bash
cd edu_project
cp .env.example .env        # 配置 DB_PASSWORD 和 JWT_SECRET
docker-compose up -d        # 构建并启动 MySQL + App
docker-compose logs -f app  # 查看日志
docker-compose down         # 停止
docker-compose down -v      # 停止并清除数据卷
```

### 方式二: 手动构建

```bash
docker build -t campus-blog:latest .
docker run -d --name campus-blog-app -p 8825:8825 --env-file .env -e DB_HOST=host.docker.internal campus-blog:latest
```

### Docker 特性

| 特性 | 说明 |
|------|------|
| 多阶段构建 | Build (JDK 21) → Runtime (JRE 21 Alpine) |
| 非 root 运行 | `appuser` (UID 1001) |
| 健康检查 | `/api/actuator/health` |
| 资源限制 | MySQL: 512M/0.5CPU, App: 1G/1.0CPU |
| 数据持久化 | `mysql_data`, `app_uploads` volumes |
| 自动初始化 | 首次启动执行 `数据库表.sql` |

---

## 日志系统

| 配置项 | 值 | 说明 |
|--------|-----|------|
| 日志文件 | `logs/application.log` | 应用运行日志 |
| 日志框架 | Logback | Spring Boot 默认 |
| 日志级别 | INFO (生产), DEBUG (开发) | 可通过配置调整 |

开启 SQL 日志: 在 `.env` 中添加 `MYBATIS_SQL_LOG=org.apache.ibatis.logging.stdout.StdOutImpl`

`HttpRequestLoggingInterceptor` 记录所有 HTTP 请求 (方法、路径、状态码、耗时、IP)。
`PerformanceMonitoringAspect` 切面记录 Service 方法执行时间，慢查询告警。

---

## 开发规范

- 所有 Controller 返回 `Result<T>` 统一响应
- 写操作加 `@Transactional(rollbackFor = Exception.class)`，读操作加 `@Transactional(readOnly = true)`
- 业务异常使用 `BusinessException`，敏感配置通过 `.env` 管理
- 密码字段绝不返回给前端

### Git 提交规范

| 类型 | 说明 | 示例 |
|------|------|------|
| `feat` | 新功能 | `feat: 添加文章草稿自动保存功能` |
| `fix` | Bug 修复 | `fix: 修复评论分页查询 NPE` |
| `docs` | 文档更新 | `docs: 更新 API 文档` |
| `refactor` | 重构 | `refactor: 重构用户服务层` |
| `perf` | 性能优化 | `perf: 添加热门标签 Caffeine 缓存` |
| `test` | 测试 | `test: 添加认证模块单元测试` |
| `chore` | 构建/工具 | `chore: 升级 MyBatis Plus 到 3.5.8` |

### 修改代码前必读

1. 阅读相关 Controller/Service/Mapper/Entity
2. 查阅 `campus_blog.md` API 文档
3. 确认现有功能后再决定复用或新增
4. 每次代码更新后更新 README.md, campus_blog.md, CHANGELOG.md
5. 运行 `mvn test` 确保测试通过

---

## 更新日志

### v2.0 (当前版本)

**性能优化**
- 新增 16 个数据库索引，覆盖高频查询场景
- 集成 Caffeine 本地缓存，5 个缓存区 (热门标签/分类/用户/热门统计/系统统计)
- `@Cacheable` 应用于 12+ 查询方法
- `@CacheEvict` 应用于 15+ 写操作方法
- 预期查询性能提升 15-300 倍

**安全增强**
- 23 个端点配置限流保护
- JWT Refresh Token 轮换机制
- 登录失败原子锁定 (5 次 → 15 分钟)

**基础设施**
- 性能监控切面 (慢查询告警)
- 请求日志拦截器
- 环境变量验证
- Docker 多阶段构建优化

### v1.x 历史版本

- **v1.58**: 最新稳定版，完整功能集
- **v1.52**: 基础功能完善
- **v1.0**: 初始版本发布

---

## 许可证

本项目仅供学习交流使用。
