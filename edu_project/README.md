# Backend

Spring Boot 3 REST API. Requires JDK 21 and Maven; there is no Maven wrapper.

## Local run

Copy `.env.example` to `.env` and set `JWT_SECRET` to at least 32 characters. The template selects SQLite; its schema is initialized from `src/main/resources/schema-sqlite.sql`.

```sh
mvn spring-boot:run
mvn test
```

The API is served under `http://localhost:8825/api`. Knife4j is at `/api/doc.html` except when the `prod` profile is active.

## MySQL

Set `DB_TYPE=mysql` and configure `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, and an at-least-8-character `DB_PASSWORD`. Initialize a standalone MySQL database with `schema.sql`. `docker compose up -d --build` configures MySQL and initializes the schema on an empty volume.

`.env` is parsed as Java Properties: leave values unquoted. SQLite schema changes do not migrate existing database files.

For deployment and reverse-proxy guidance, see [DEPLOY.md](DEPLOY.md). For test-specific datasource requirements, see the root [AGENTS.md](../AGENTS.md).
