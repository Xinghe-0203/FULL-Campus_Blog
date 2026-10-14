# Campus Blog — agent notes

## Scope and existing rules

- Two independent projects: `edu_project/` (Spring Boot/MyBatis Plus) and `edu_project_vue/` (Vue/Vite). There is no root build manifest or npm workspace; run commands inside the relevant project.
- `CLAUDE.md` files point to this guide. Before backend source edits, read the related Controller → Service → Mapper → Entity chain; update affected README files and `CHANGELOG.md` for code changes.

## Backend: run and verify

Requires JDK 21 and installed Maven; there is no Maven wrapper. Commands below run from `edu_project/`.

```sh
mvn spring-boot:run
mvn test
mvn test "-Dtest=AuthControllerTest"
mvn test "-Dtest=AuthControllerTest#sendRegisterCode_Success_Returns200"
mvn clean package -DskipTests
```

- For local runtime, copy `.env.example` to `.env` and fill `JWT_SECRET` (at least 32 characters); retain the supplied JWT expiration values. The template selects `DB_TYPE=sqlite`, not MySQL. SQLite initializes from `src/main/resources/schema-sqlite.sql` and defaults to `campus_blog.db` in the working directory.
- `config/DotenvConfig.java` parses `.env` with Java `Properties`, not shell dotenv: use unquoted values. It searches the working directory, then the user's home directory. File values become JVM properties, outranking OS environment variables; existing JVM `-D` properties win.
- MySQL requires `DB_TYPE=mysql`, all `DB_HOST/DB_PORT/DB_NAME/DB_USERNAME/DB_PASSWORD` values, and manual initialization with `schema.sql`; startup validation requires a password of at least 8 characters. Compose sets `DB_TYPE=mysql` and initializes MySQL from `schema.sql`.
- Tests use H2 via `src/test/resources/application.yml`, optional `application-test.yml`, and test `schema.sql`; test request paths omit `/api`. Keep `DB_TYPE` unset for H2: a runtime database selection activates `DatabaseConfig` and replaces the test datasource, even with the `test` profile.
- Runtime API: `http://localhost:8825/api`; Knife4j: `/api/doc.html` (disabled under the `prod` profile). `/api` is the servlet context, not part of controller mappings or Spring Security matchers.

## Backend: change boundaries

Java source root: `edu_project/src/main/java/com/example/edu_project/`.

- Post/circle CRUD services delegate queries and interactions to `PostQueryService`/`PostInteractionService` and `CircleQueryService`/`CircleInteractionService`; modify the owning service rather than duplicating logic in the facade.
- Use `common.result.Result<T>` and `common.exception.BusinessException`; obtain identity through `utils.SecurityUtils` (`getCurrentUserId()` requires login; `getCurrentUserIdOrNull()` allows guests). Public endpoints also need an explicit rule in `config/security/SecurityConfig.java`.
- User articles and circle posts publish directly with `status=1`; do not introduce a pending-review gate despite the existing moderation APIs/status fields.
- Preserve service transaction conventions: writes use `@Transactional(rollbackFor = Exception.class)`, reads use `@Transactional(readOnly = true)`.
- Database changes must account for MySQL `edu_project/schema.sql`, SQLite `src/main/resources/schema-sqlite.sql`, and H2 `src/test/resources/schema.sql`. SQLite initialization uses `CREATE TABLE IF NOT EXISTS`, so schema edits do not migrate existing files.
- Mapper XML supports `databaseId="mysql"`/`"sqlite"`; H2 has no mapped database ID and uses untagged fallback statements. Do not put MySQL-only SQL in shared statements. Custom SQL must handle soft deletion explicitly (`is_deleted=0`); generated MyBatis Plus queries use `isDeleted`/`@TableLogic`.

## Frontend: run and verify

Commands below run from `edu_project_vue/`. Use Node 22 (>=22.13) or Node 24+: `.npmrc` enables `engine-strict`, and locked jsdom/Vitest dependencies reject the README's Node 18 minimum.

```sh
npm ci
npm run dev
npm run lint:check
npm run format:check
npm run type-check
npm run test:run
npm run test:run -- src/utils/__tests__/crypto.test.ts
npm run build
```

- `lint` auto-fixes all `src`; `format` rewrites files. Use the `*:check` scripts for non-mutating verification. `build` runs `vue-tsc --noEmit` before Vite; `type-check` is the focused check.
- `npm test` starts watch mode; `test:run` exits. Vitest configuration lives in `vite.config.ts`, uses jsdom, and loads `src/test/setup.ts` (router/matchMedia mocks).
- Dev runs on port 3000. `VITE_API_TARGET` controls `/api` proxying and `/uploads` → `/api/uploads` rewriting. Axios hardcodes `/api` in `src/api/index.ts`; production proxying is configured separately in `vercel.json`/the reverse proxy.

## Frontend: integration traps

- Use `src/api/` modules: the shared Axios interceptor returns the backend response envelope, not `AxiosResponse`; `response.data` is the business payload. It also owns queued token refresh and auth-failure cleanup of Pinia/storage.
- `src/main.ts` restores auth state before installing the router; preserve this order. Routes are assembled from `src/router/modules/`, with guards in `src/router/guards.ts`.
- `src/styles/main.css` is only the import entrypoint; design tokens live in `variables.css`, dark overrides in `dark-mode.css`. Confirmations use `src/composables/useConfirm.ts`; render its returned `ConfirmDialog` component as well as calling `confirm()`.
