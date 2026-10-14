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

## Standalone backend

Install JDK 21 and Maven, configure `.env`, then run `mvn clean package -DskipTests` in this directory. Start the resulting JAR with `java -jar target/edu_project-0.0.1-SNAPSHOT.jar`. Use `DB_TYPE=mysql`, configure all MySQL connection variables, and apply `schema.sql` manually. Local development may use the template's SQLite mode.

## Frontend

In `edu_project_vue/`, use Node.js 22.13+ or 24+, then run `npm ci` and `npm run build`. Deploy `dist/` as an SPA and proxy `/api/*` to the backend API and `/uploads/*` to `/api/uploads/*`. The included `vercel.json` has a preconfigured API origin; review it before deploying your own instance. For local development, `VITE_API_TARGET` selects the backend proxy target.

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
