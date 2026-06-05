# Campus Blog - Backend

Spring Boot 3 backend service providing RESTful APIs for the Campus Blog platform.

**Version**: v2.0.18 | **Java**: 21 | **Spring Boot**: 3.3.13

## Tech Stack

| Component | Version | Purpose |
|-----------|---------|---------|
| Java | 21 | LTS |
| Spring Boot | 3.3.13 | Web framework |
| MyBatis Plus | 3.5.8 | ORM |
| Spring Security | 6.x | Authentication |
| JJWT | 0.12.6 | JWT |
| Caffeine | 3.2.0 | Local cache |
| Knife4j | 4.5.0 | API documentation |
| Jsoup | 1.18.3 | XSS protection |
| Hutool | 5.8.40 | Utility library |
| Lombok | 1.18.42 | Code simplification |

## Quick Start

### 1. Configuration

```bash
cp .env.example .env
# Edit .env: set DB_PASSWORD and JWT_SECRET (min 32 chars)
```

### 2. Database

```bash
mysql -u root -p
CREATE DATABASE campus_blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_blog;
SOURCE schema.sql;
```

### 3. Run

```bash
mvn spring-boot:run             # Dev mode
mvn clean package -DskipTests   # Production build
java -jar target/edu_project-0.0.1-SNAPSHOT.jar
```

### 4. Access

| Endpoint | URL |
|----------|-----|
| API | http://localhost:8825/api |
| API Docs | http://localhost:8825/api/doc.html |
| Health | http://localhost:8825/api/actuator/health |

## Project Structure

```
src/main/java/com/example/edu_project/
├── controller/
│   ├── auth/         # AuthController, PasswordController
│   ├── user/         # SysUserController
│   ├── post/         # BlogPost, BlogComment, BlogLike, BlogCollect, BlogTag
│   ├── circle/       # CircleController
│   ├── social/       # Follow, Message, Notification, Report
│   ├── content/      # Topic, Trending, Media, Share, Statistics
│   └── admin/        # 8 Admin*Controller
├── service/
│   ├── auth/         # SysUserService, EmailService
│   ├── post/         # BlogPostService, BlogTagService, PostInteractionService, PostQueryService
│   ├── circle/       # CircleService, CircleQueryService, CircleInteractionService
│   ├── social/       # BlogCollect/Comment/Like, Follow, Message, Notification, Report
│   └── content/      # Media, Share, Statistics, Topic, Trending
├── mapper/           # 22 MyBatis Plus mappers
├── entity/           # 22 entity classes
├── dto/              # Request DTOs (domain-grouped)
├── vo/               # Response VOs (domain-grouped)
├── config/
│   ├── security/     # SecurityConfig, JwtAuthenticationFilter, XssFilter
│   ├── web/          # WebMvcConfig, RateLimitInterceptor
│   ├── cache/        # CaffeineCacheConfig
│   └── db/           # DatabaseConfig, MybatisPlusConfig
├── common/           # Result, BusinessException, PageResult
├── annotation/       # Custom annotations
├── aspect/           # AOP aspects
├── event/            # Application events
├── listener/         # Event listeners
└── util/             # Utility classes
```

## Architecture

```
Controller → Service → Mapper → Entity
```

### Conventions

| Convention | Implementation |
|-----------|---------------|
| Unified response | `Result<T>` - `Result.success(data)` / `Result.error(code, msg)` |
| Mapper inheritance | `BaseMapper<T>` (MyBatis Plus) |
| Soft delete | `isDeleted` + `@TableLogic` (0=active, 1=deleted) |
| Write transactions | `@Transactional(rollbackFor = Exception.class)` |
| Read transactions | `@Transactional(readOnly = true)` |
| Business exceptions | `throw new BusinessException(code, message)` |
| Current user | `SecurityUtils.getCurrentUserIdOrNull()` |

### Response Format

```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": 1716000000000
}
```

Paginated responses include `records`, `total`, `size`, `current`, `pages` in `data`.

## Security

| Feature | Implementation |
|---------|---------------|
| Password | BCrypt (strength 12), never returned in API |
| JWT | Access Token (24h) + Refresh Token (7d) with rotation |
| Token blacklist | Added on logout, prevents replay |
| Login lock | 5 failures → 15 min lock (atomic update) |
| XSS | HtmlSanitizer (Jsoup) for server-side purification |
| Rate limiting | 23 endpoints via `RateLimitInterceptor` |
| IP protection | Format validation against IP spoofing |
| Validation | Spring Validation (`@Valid`, `@NotBlank`) |
| CORS | Configurable whitelist, `*` forbidden in production |

## Environment Variables

| Variable | Default | Required | Description |
|----------|---------|----------|-------------|
| `DB_HOST` | localhost | Yes | Database host |
| `DB_PORT` | 3306 | Yes | Database port |
| `DB_NAME` | campus_blog | Yes | Database name |
| `DB_USERNAME` | root | Yes | Database user |
| `DB_PASSWORD` | — | **Yes** | Database password |
| `JWT_SECRET` | — | **Yes** | JWT secret (min 32 chars) |
| `SERVER_PORT` | 8825 | No | Server port |
| `CORS_ALLOWED_ORIGINS` | localhost:3000,localhost:8825 | No | CORS whitelist |
| `MAIL_*` | — | No | SMTP configuration |

## Performance

### Database Indexes (16)

Covering high-frequency queries: user post lists, category filtering, hot/latest sorting, comment lists, like/collect/follow checks, notification lists, circle posts, draft lists, report queries, messaging.

### Caffeine Cache (5 instances)

| Cache | Content | TTL |
|-------|---------|-----|
| `hotTagsCache` | Hot tags | 30 min |
| `categoryCache` | Categories | 1 hour |
| `userCache` | User info | 30 min |
| `trendingCache` | Trending stats | 10 min |
| `statsCache` | System stats | 5 min |

- `@Cacheable` on 12+ query methods
- `@CacheEvict` on 15+ write methods
- Expected 15-300x query performance improvement

## Testing

```bash
mvn test                            # All tests
mvn test -Dtest=AuthControllerTest  # Single test class
```

Tests use H2 in-memory database (`application-test.yml`), random port, no impact on production data.

## Docker

```bash
cp .env.example .env        # Configure DB_PASSWORD and JWT_SECRET
docker-compose up -d        # Start MySQL + App
docker-compose logs -f      # View logs
docker-compose down         # Stop
```

Multi-stage build: JDK 21 (build) → JRE 21 Alpine (runtime). Runs as non-root user `appuser` (UID 1001).

## API Modules

| Group | Endpoints | Group | Endpoints |
|-------|-----------|-------|-----------|
| Auth | 5 | Follow | 5 |
| User | 8 | Notification | 5 |
| Post | 12 | Message | 5 |
| Comment | 6 | Circle | 8 |
| Like | 4 | Topic | 5 |
| Collect | 4 | Report | 4 |
| Tag | 5 | Share | 3 |
| Media | 4 | Statistics | 4 |
| Trending | 3 | Admin | 15+ |

Total: **120+ endpoints**. Authenticated endpoints require `Authorization: Bearer <access_token>`.
