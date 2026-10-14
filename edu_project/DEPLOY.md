# Deployment

## Docker Compose (MySQL)

1. Copy `.env.example` to `.env`.
2. Set `DB_PASSWORD` (at least 8 characters), `JWT_SECRET` (at least 32 characters), and production `CORS_ALLOWED_ORIGINS`.
3. Set `ADMIN_DEFAULT_PASSWORD` to a unique initial password, or change the generated `admin / Admin123` credentials immediately after the first start.
4. Run:

   ```sh
   docker compose up -d --build
   docker compose logs -f app
   ```

Compose explicitly selects MySQL and initializes `schema.sql` only when the MySQL data volume is empty. Schema edits do not migrate an existing volume. The API binds to `127.0.0.1:8825`; uploaded files persist in the `app_uploads` volume. The `prod` profile disables Knife4j.

## Single JAR production deployment (SQLite)

Build with JDK 21 and Maven from `edu_project/`:

```sh
mvn -Psingle-jar -DskipTests package
```

The `single-jar` profile builds the Vue app and embeds it in the Spring Boot JAR; it downloads its pinned Node.js/npm build runtime and does not require Node.js on the production host. The artifact is `target/edu_project-0.0.1-SNAPSHOT.jar`.

For isolated validation, override the Maven build directory so generated files never touch an existing production `target/` folder:

```sh
mvn -Psingle-jar -DskipTests -Dcampus.buildDirectory=/safe/build/campus-blog package
```

For replacement, stop the old process and back up the existing JAR, SQLite database, and `uploads/` directory. Replace only the JAR; keep the existing `.env`, database file, and upload directory unchanged. Confirm the existing runtime configuration selects `DB_TYPE=sqlite`. Start the JAR from the same application working directory so the existing `.env`, relative SQLite path, uploads, and logs continue to resolve there:

```sh
java -jar target/edu_project-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

The Vue site is available at `http://localhost:8825/`; API URLs remain under `http://localhost:8825/api`. The JAR serves the SPA shell at the root and translates `/api/*` internally to the existing controller mappings. No separate frontend server or reverse proxy is required. The app continues to use the existing SQLite schema; this build adds no database migration.

## Separate frontend deployment (optional)

For an independent frontend deployment, in `edu_project_vue/` use Node.js 22.22.2 or 24+, then run `npm ci` and `npm run build`. Deploy `dist/` as an SPA and proxy `/api/*` to the backend API and `/uploads/*` to `/api/uploads/*`. The included `vercel.json` has a preconfigured API origin; review it before deploying your own instance. For local development, `VITE_API_TARGET` selects the backend proxy target. Production frontend builds use `/` as the base path.

## Reverse proxy example (Nginx)

```nginx
location / {
    root /path/to/edu_project_vue/dist;
    try_files $uri $uri/ /index.html;
}

location /api/ {
    proxy_pass http://127.0.0.1:8825/api/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}

location /uploads/ {
    proxy_pass http://127.0.0.1:8825/api/uploads/;
}
```

Expose HTTPS at the proxy/load balancer, restrict CORS to deployed origins, and keep `.env`, database files, and uploaded media out of source control.
