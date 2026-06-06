# AGENTS.md — Campus Blog Forum System

> Spring Boot 3 + Vue 3 full-stack project. Current version v2.0.18.

## Project Structure

```
edu_project/          # Backend (Spring Boot 3 + MyBatis Plus + Java 21)
edu_project_vue/      # Frontend (Vue 3 + Vite + TypeScript)
```

Root `package.json` is empty `{}`, not an npm workspace. Two sub-projects are independent.

## Quick Start

### Backend (edu_project/)
```bash
cd edu_project
cp .env.example .env            # Required: copy and fill .env
mvn spring-boot:run             # Dev start
mvn clean package -DskipTests   # Production build
mvn test                        # Run tests (H2 in-memory)
```
- API: http://localhost:8825/api
- API Docs: http://localhost:8825/api/doc.html (Knife4j)

### Frontend (edu_project_vue/)
```bash
cd edu_project_vue
npm install
npm run dev                     # Dev server (port 3000)
npm run build                   # Production build
npm run lint                    # ESLint check
```
- App: http://localhost:3000
- `/api` requests auto-proxy to localhost:8825

### Database Setup
```sql
CREATE DATABASE campus_blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_blog;
SOURCE edu_project/schema.sql;
```
- Default admin: `admin` / `Admin123`

## Architecture

### Backend Layering
```
Controller → Service → Mapper → Entity
```
- All controllers return `Result<T>` unified response
- All mappers extend `BaseMapper<T>` (MyBatis Plus)
- Soft delete: `isDeleted` field + `@TableLogic` (1=deleted, 0=active)
- Write ops: `@Transactional(rollbackFor = Exception.class)`
- Read ops: `@Transactional(readOnly = true)`
- Business exceptions: `throw new BusinessException(code, message)`
- Current user: `SecurityUtils.getCurrentUserIdOrNull()`

### Backend Package Structure (domain-grouped)

```
controller/
  auth/       # AuthController, PasswordController
  user/       # SysUserController
  post/       # BlogPost, BlogComment, BlogLike, BlogCollect, BlogTag
  circle/     # CircleController
  social/     # Follow, Message, Notification, Report
  content/    # Topic, Trending, Media, Share, Statistics
  admin/      # 8 Admin*Controller

service/
  auth/       # SysUserService, EmailService
  post/       # BlogPostService, BlogTagService, PostInteractionService, PostQueryService
  circle/     # CircleService, CircleQueryService, CircleInteractionService
  social/     # BlogCollect/Comment/Like, Follow, Message, Notification, Report
  content/    # Media, Share, Statistics, Topic, Trending

dto/
  auth/       # Login, register, password DTOs
  user/       # User query DTOs
  post/       # Post CRUD DTOs
  circle/     # Circle DTOs
  social/     # Social interaction DTOs
  content/    # Content DTOs

vo/
  user/       # UserVO, UserLoginResponse, AdminUserVO
  post/       # PostDetailResponse, CommentVO, LikeVO, CollectVO
  circle/     # CirclePostVO, CircleCommentVO
  social/     # FollowStatusVO, MessageVO, NotificationVO, ReportVO
  content/    # HotContentVO, MediaVO, StatisticsVO

config/
  security/   # SecurityConfig, JwtAuthenticationFilter, XssFilter
  web/        # WebMvcConfig, RateLimitInterceptor
  cache/      # CaffeineCacheConfig
  db/         # DatabaseConfig, MybatisPlusConfig
```

### Security
- BCrypt password encryption (strength 12), API never returns password fields
- JWT auth via `JwtAuthenticationFilter`, refresh token rotation and blacklist
- Login lock: 5 failures → 15 min lock (atomic update)
- XSS protection: `HtmlSanitizer` (Jsoup) server-side + DOMPurify client-side
- Current user: `SecurityUtils.getCurrentUserIdOrNull()`
- 22 database tables, full schema in `edu_project/schema.sql`

### Performance
- 16 database indexes for high-frequency queries
- 5 Caffeine local caches (user, post, category, tag, stats)
- `@Cacheable`/`@CacheEvict` annotations for cache management
- `statsCache` dedicated to statistics data

### Frontend
- Vue 3 Composition API (`<script setup>`)
- Path alias: `@` → `src/`
- State management: Pinia (user, theme, app stores)
- API modules: 17 files in `src/api/`
- Modular routing: `router/modules/` (auth, home, post, circle, user, discover, admin)
- TypeScript with strict type checking
- Confirm dialog: `useConfirm()` composable
- Token auto-refresh + 401 clears Pinia store
- Rainy Glassmorphism UI design system
- 30 page components, 11 reusable components

## Key Configuration

| Config | Default | Description |
|--------|---------|-------------|
| SERVER_PORT | 8825 | Backend port |
| DB_NAME | campus_blog | Database name |
| JWT_SECRET | (required) | Min 32 chars |
| CORS_ALLOWED_ORIGINS | localhost:3000,localhost:8825 | CORS whitelist |

- All sensitive config via `.env`, never hardcode
- Tests use H2 in-memory DB (`src/test/resources/application.yml`), random port

## Development Rules

1. **Read before modifying**: Check related Controller/Service/Mapper/Entity and docs first
2. **Update docs on changes**: README.md, CHANGELOG.md
3. **Don't reinvent**: Confirm existing functionality before adding new code
4. **Ask when uncertain**: Don't assume business logic
5. **User content no review needed**: Articles publish with status=1 directly

## Instruction Files

- `CLAUDE.md` (root) — Global development principles and code modification rules
- `edu_project/README.md` — Backend documentation
- `edu_project_vue/CLAUDE.md` — Frontend project structure and conventions
