# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

- **每次更新完代码都要更新md文件** - Update README.md and 其他文档

---

## 代码修改规范

**【强制】每次修改代码前，必须先阅读相关文档和现有代码：**

1. **阅读项目文档** - 修改前先阅读 `全部文档`，了解项目架构和已有设计
2. **阅读相关代码** - 修改某个模块前，先完整阅读该模块的所有相关文件（Controller、Service、Mapper、Entity）
3. **了解关联关系** - 不要臆想或猜测代码之间的关联，务必通过阅读源码确认
4. **避免重复造轮子** - 确认现有功能后再决定是复用还是新增

---

## 开发荣耻

以瞎清接口为耻，以认真查询为荣。
以模糊执行为耻，以寻求确认为荣。
以想业务为耻，以人类确认为荣。
以创造接口为耻，以复用现有为荣。
以跳过验证为耻，以主动测试为荣。
以破坏架构为耻，以遵循规范为荣。
以假装理解为耻，以诚实无知为荣。
以盲目修改为耻，以谨慎重构为荣。
以忘记更文档为耻，以及时更新为荣。

---

## 项目架构

### 整体结构

```
FULL-Campus_Blog/
├── edu_project/           # Spring Boot 3 后端 (Java 21)
│   └── src/main/java/com/example/edu_project/
│       ├── controller/   # 23个 REST Controller
│       ├── service/      # Service 接口
│       ├── service/impl/ # Service 实现
│       ├── mapper/       # MyBatis Plus Mapper (22个)
│       ├── entity/       # JPA/MyBatis Plus Entity (22个)
│       ├── dto/          # 请求数据传输对象
│       ├── vo/           # 响应视图对象
│       ├── config/       # 配置类 (Security, JWT, Cache, CORS)
│       ├── event/        # 事件类
│       ├── listener/     # 事件监听器
│       └── utils/        # 工具类 (JwtUtils, SecurityUtils, LogUtils)
│
└── edu_project_vue/       # Vue 3 前端 (Vite)
    └── src/
        ├── api/          # 17个 API 模块 (Axios)
        ├── views/        # 33个页面组件
        ├── components/   # 通用组件 (common/, layout/)
        ├── stores/       # Pinia 状态管理 (user.js, theme.js)
        ├── router/       # Vue Router 配置
        └── composables/  # 组合式函数
```

### 后端分层架构

```
请求 → Controller → Service → Mapper → MyBatis Plus → MySQL
              ↓
          统一响应 (Result<T>)
```

**数据流**：
1. Controller 接收请求，参数校验 (@Valid)
2. 调用 Service 业务逻辑
3. Service 通过 Mapper 操作数据库
4. MyBatis Plus 提供 CRUD 能力，支持逻辑删除 (isDeleted)
5. 响应统一封装为 `Result<T>` 格式

### 前端状态管理

- **user.js store**: 用户认证状态 (token, userInfo)、登录/登出
- **theme.js store**: 主题偏好
- API 调用统一通过 `src/api/` 模块，Axios 实例配置在 `api/index.js`

### 认证架构

**JWT 双 Token 机制**：
- Access Token: 24小时有效，用于 API 认证
- Refresh Token: 7天有效，仅用于刷新 Access Token
- Refresh Token 不能用于普通 API 访问 (JwtAuthenticationFilter 拦截拒绝)

**Security Config** (`config/SecurityConfig.java`)：
- CSRF 已禁用 (JWT Bearer Token 认证，浏览器同源策略保护)
- CORS 配置允许的环境变量: `CORS_ALLOWED_ORIGINS`
- `/admin/**` 路径需要 `ROLE_ADMIN` 权限
- 大多数 GET 请求和部分 POST 请求允许匿名访问

### 缓存策略

- **Caffeine 本地缓存**: 1000条记录，5分钟过期
- 用于速率限制、热门内容缓存
- 生产环境建议迁移到 Redis 分布式缓存

### 数据库

- **22张表**，MyBatis Plus 自动管理 CRUD
- 逻辑删除: `isDeleted` 字段 (0=未删除, 1=已删除)
- 关联表: `blog_post_tag`, `blog_draft_tag`, `blog_post_media`

---

## 常用命令

### 后端 (edu_project/)

```bash
cd edu_project

# 启动开发服务器 (端口 8825，context-path: /api)
mvn spring-boot:run

# 编译打包
mvn clean package -DskipTests

# 运行 JAR
java -jar target/edu_project-0.0.1-SNAPSHOT.jar

# 查看依赖树
mvn dependency:tree

# 查看有效 POM
mvn help:effective-pom
```

**环境配置**: 复制 `.env.example` 到 `.env`，配置数据库和 JWT 密钥

### 前端 (edu_project_vue/)

```bash
cd edu_project_vue

# 安装依赖
npm install

# 启动开发服务器 (端口 3000，代理 /api 到 localhost:8825)
npm run dev

# 构建生产版本
npm run build

# 预览生产版本
npm run preview

# 代码检查和修复
npm run lint
```

### Docker 部署

```bash
cd edu_project
docker-compose up -d      # 启动所有服务
docker-compose logs -f   # 查看日志
docker-compose down      # 停止服务
```

---

## 关键配置

| 文件 | 用途 |
|------|------|
| `application.yml` | 主配置，数据库/JWT/上传/缓存等 |
| `.env` | 敏感信息 (DB密码, JWT密钥) |
| `SecurityConfig.java` | HTTP 安全策略、CORS、路径权限 |
| `JwtAuthenticationFilter.java` | JWT 解析、Token 黑名单检查 |
| `JwtUtils.java` | Token 生成、验证、刷新 |

### API 文档

启动后端后访问: http://localhost:8825/api/doc.html (Knife4j)

### 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |

---

## 开发注意事项

1. **修改 Controller/Service 前**: 先完整阅读该模块的所有相关文件
2. **新增接口**: 在对应 Service 添加业务逻辑，遵循现有异常处理模式
3. **数据库变更**: 同步更新 Entity 类的字段注解
4. **前端 API 调用**: 统一使用 `src/api/` 下的模块，通过 Pinia store 管理状态
5. **安全**: 密码加密使用 BCrypt (强度12)，敏感信息禁止硬编码