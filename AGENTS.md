# AGENTS.md — 校园博客论坛系统

> Spring Boot 3 + Vue 3 全栈项目。当前版本 v2.0.11。

## 项目结构

```
edu_project/          # 后端 (Spring Boot 3 + MyBatis Plus + Java 21)
edu_project_vue/      # 前端 (Vue 3 + Vite + Node.js 18+)
```

根目录 `package.json` 为空 `{}`，不是 npm workspace。两个子项目独立管理。

## 启动命令

### 后端 (edu_project/)
```bash
cd edu_project
copy .env.example .env          # 首次必须：复制并填写 .env
mvn spring-boot:run             # 开发启动
mvn clean package -DskipTests   # 生产打包
mvn test                        # 运行测试 (H2 内存库)
```
- 访问: http://localhost:8825/api
- API 文档: http://localhost:8825/api/doc.html (Knife4j)

### 前端 (edu_project_vue/)
```bash
cd edu_project_vue
npm install
npm run dev                     # 开发服务器 (端口 3000)
npm run build                   # 生产构建
npm run lint                    # ESLint 检查
```
- 访问: http://localhost:3000
- `/api` 请求自动代理到后端 localhost:8825

### 数据库初始化
```sql
CREATE DATABASE campus_blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_blog;
SOURCE edu_project/数据库表.sql;
```
- 默认管理员: `admin` / `admin123`

## 架构要点

### 后端分层
```
Controller → Service → Mapper → Entity
```
- 所有 Controller 返回 `Result<T>` 统一响应
- 所有 Mapper 继承 `BaseMapper<T>` (MyBatis Plus)
- 软删除: `isDeleted` 字段 + `@TableLogic` (值 1=删除, 0=未删除)
- 写操作加 `@Transactional(rollbackFor = Exception.class)`
- 读操作加 `@Transactional(readOnly = true)`
- 业务异常: `throw new BusinessException(code, message)`

### 安全
- BCrypt 密码加密 (strength 12)，API 不返回密码字段
- JWT 认证 via `JwtAuthenticationFilter`，支持 refresh token 轮换和黑名单
- 登录失败锁定: 5 次失败 → 15 分钟锁定 (原子更新)
- XSS 防护: `HtmlSanitizer` (Jsoup)
- 当前用户: `SecurityUtils.getCurrentUserIdOrNull()`
- 22 张数据库表，完整表结构见 `edu_project/数据库表.sql`

### 性能优化
- 16 个数据库索引优化查询性能
- 5 个 Caffeine 本地缓存 (用户、帖子、分类、标签、配置)
- `@Cacheable`/`@CacheEvict` 注解管理缓存
- `statsCache` 专门缓存统计数据

### 前端
- Vue 3 Composition API (`<script setup>`)
- 路径别名: `@` → `src/`
- 状态管理: Pinia (user, theme, app 三个 store)
- API 模块: `src/api/` 下 17 个文件
- 确认对话框: `useConfirm()` composable
- Token 自动刷新 + 401 时清空 Pinia store
- Rainy glassmorphism UI 设计系统 (毛玻璃拟态风格)
- 30 个页面组件, 11 个可复用组件
- Circle 增强功能: @mentions, 位置标签, 话题标签, 显示切换
- Messages 布局修复 (双栏滚动)

## 关键配置

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| SERVER_PORT | 8825 | 后端端口 |
| DB_NAME | campus_blog | 数据库名 |
| JWT_SECRET | (必须设置) | 至少 32 位 |
| CORS_ALLOWED_ORIGINS | localhost:3000,localhost:8825 | 跨域白名单 |
| CACHE_TTL | 300 | 缓存过期时间(秒) |

- 所有敏感配置通过 `.env` 管理，禁止硬编码
- 测试使用 H2 内存库 (`src/test/resources/application.yml`)，端口随机

## 开发规范

1. **修改代码前必读**: 先读相关 Controller/Service/Mapper/Entity 和文档 (`campus_blog.md`, `README.md`)
2. **每次代码更新后必须更新文档**: README.md, campus_blog.md, CHANGELOG.md
3. **不重复造轮子**: 确认现有功能后再决定复用或新增
4. **不确定时询问用户**: 不要臆想业务逻辑

## 指令文件来源

- `CLAUDE.md` (根目录) — 全局开发荣耻和代码修改规范
- `edu_project/CLAUDE.md` — 后端详细架构、安全约定、关键文件清单
- `edu_project_vue/CLAUDE.md` — 前端项目结构和开发规范
- `edu_project/campus_blog.md` — 完整开发计划和 100+ API 端点文档
