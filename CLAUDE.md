# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

**Version**: v2.0.20 | **GitHub**: https://github.com/Xinghe-0203/FULL-Campus_Blog

---

## 代码修改规范

**【强制】修改代码前必须完整阅读相关文件：**

1. 阅读项目文档（README.md、CLAUDE.md）
2. 修改某个模块前，完整阅读该模块的 Controller → Service → Mapper → Entity
3. 通过源码确认关联关系，不要猜测
4. 确认现有功能后再决定复用还是新增

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

### 技术栈
- **后端**: Spring Boot 3 + MyBatis Plus + Java 21
- **前端**: Vue 3 + Vite + Pinia
- **数据库**: MySQL 8 (22张表，逻辑删除 `is_deleted`)
- **缓存**: Caffeine (5个缓存实例)
- **API文档**: Knife4j (http://localhost:8825/api/doc.html)

### 数据流
```
Controller → Service → Mapper → MyBatis Plus → MySQL
              ↓
          统一响应 Result<T>
```

### 认证
JWT双Token: Access(24h) + Refresh(7d)，Refresh仅用于刷新AccessToken

### 性能优化
- **数据库索引**: 16个索引覆盖高频查询
- **Caffeine缓存**: 5个缓存实例 (用户/帖子/标签/评论/配置)

### 关键文件
| 文件 | 用途 |
|------|------|
| `application.yml` | 主配置 |
| `.env` | 敏感信息 (DB密码, JWT密钥) |
| `SecurityConfig.java` | 安全策略、CORS、路径权限 |
| `JwtAuthenticationFilter.java` | Token解析、黑名单检查 |

### 22张数据库表
sys_user, blog_post, blog_comment, blog_tag, blog_post_tag,
blog_like, blog_collect, blog_follow, blog_notification,
blog_trending, blog_draft, blog_draft_tag, blog_report,
blog_circle_post, blog_circle_like, blog_circle_comment, blog_circle_repost,
blog_media, blog_post_media, blog_topic, blog_message, blog_share

---

## 常用命令

### 后端 (edu_project/)
```bash
mvn spring-boot:run           # 启动 (端口8825, context-path: /api)
mvn clean package -DskipTests # 打包
java -jar target/*.jar        # 运行JAR
```

### 前端 (edu_project_vue/)
```bash
npm install && npm run dev     # 启动 (端口3000, 代理/api到localhost:8825)
npm run build                 # 生产构建
npm run lint                  # 代码检查
```

### Docker
```bash
docker-compose up -d          # 启动MySQL+后端
docker-compose logs -f        # 查看日志
```

---

## 默认账号
admin / Admin123 (ROLE_ADMIN)

---

## 开发注意事项

1. **用户发布内容不需要审核**: status=1 直接发布
2. **密码加密**: BCrypt强度12
3. **禁止硬编码敏感信息**: 使用环境变量
4. **每次更新代码后同步更新md文档**
5. **API文档**: 启动后访问 http://localhost:8825/api/doc.html