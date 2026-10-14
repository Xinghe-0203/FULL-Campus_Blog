# Campus Blog

校园博客与校友圈平台，包含 Spring Boot API 和 Vue 单页应用。前后端是两个独立项目，没有根级构建或 npm workspace。

## 功能

- 用户注册、登录、资料与权限管理
- 文章、标签、搜索、草稿和热门内容
- 校友圈动态、话题、评论、点赞、收藏、关注
- 私信、通知、媒体上传和管理后台

## 技术栈

- 后端：Java 21、Spring Boot 3、MyBatis Plus；本地默认 SQLite，也支持 MySQL
- 前端：Vue 3、TypeScript、Vite、Pinia

版本与依赖以 `edu_project/pom.xml`、`edu_project_vue/package.json` 和 lock 文件为准。

## 本地运行

需要 JDK 21、Maven，以及 Node.js 22（22.13+）或 Node.js 24+。

1. 在 `edu_project/` 复制 `.env.example` 为 `.env`，配置至少 32 字符的 `JWT_SECRET`。默认使用 SQLite。
2. 在 `edu_project/` 运行 `mvn spring-boot:run`。
3. 在 `edu_project_vue/` 运行 `npm ci` 和 `npm run dev`。
4. 打开 <http://localhost:3000>；后端 API 地址为 <http://localhost:8825/api>。

开发服务器默认代理 API 到 `http://localhost:8825`，可通过 `VITE_API_TARGET` 修改。

## Docker 运行

在 `edu_project/` 将 `.env.example` 复制为 `.env`，设置至少 8 字符的 `DB_PASSWORD` 和至少 32 字符的 `JWT_SECRET`，然后运行：

```sh
docker compose up -d --build
```

Compose 使用 MySQL 并在空数据卷首次启动时初始化 `schema.sql`。修改现有 schema 不会迁移已存在的数据卷。默认管理员为 `admin / Admin123`；生产环境首次启动前请设置 `ADMIN_DEFAULT_PASSWORD` 或立即修改密码。

## 检查

在 `edu_project/`：`mvn test`

在 `edu_project_vue/`：

```sh
npm run lint:check
npm run format:check
npm run type-check
npm run test:run
npm run build
```

## 文档

- [部署说明](edu_project/DEPLOY.md)
- [后端说明](edu_project/README.md)
- [前端说明](edu_project_vue/README.md)
- [变更记录](CHANGELOG.md)
- [Agent 工作指南](AGENTS.md)
