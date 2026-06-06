# 校园博客论坛系统 - 部署文档 v2.0
# Campus Blog Forum System - Deployment Guide

> **版本**: v2.0.18 | **最后更新**: 2026-06-05
>
> 本文档涵盖从本地开发到生产环境的全流程部署指南。

---

## 目录

1. [前置要求](#1-前置要求)
2. [数据库初始化](#2-数据库初始化)
3. [后端部署](#3-后端部署)
4. [前端部署](#4-前端部署)
5. [Docker 部署](#5-docker-部署)
6. [Nginx 配置](#6-nginx-配置)
7. [systemd 服务](#7-systemd-服务)
8. [生产检查清单](#8-生产检查清单)
9. [故障排查](#9-故障排查)
10. [性能调优](#10-性能调优)
11. [备份与恢复](#11-备份与恢复)

---

## 1. 前置要求

### 1.1 必需软件

| 软件 | 最低版本 | 用途 | 安装验证命令 |
|------|----------|------|-------------|
| JDK | 21 LTS | 后端编译与运行 | `java -version` |
| Maven | 3.8+ | 后端构建工具 | `mvn -version` |
| MySQL | 8.0+ | 数据库服务器 | `mysql --version` |
| Node.js | 18+ | 前端构建与开发 | `node -v` |
| npm | 9+ | 前端包管理 | `npm -v` |

### 1.2 可选软件

| 软件 | 版本 | 用途 |
|------|------|------|
| Docker | 20.10+ | 容器化部署 |
| Docker Compose | 2.0+ | 多容器编排 |
| Nginx | 1.20+ | 反向代理与静态文件服务 |

### 1.3 硬件要求

| 环境 | CPU | 内存 | 磁盘 |
|------|-----|------|------|
| 开发 | 2 核 | 4GB | 10GB |
| 生产 (最小) | 2 核 | 4GB | 20GB |
| 生产 (推荐) | 4 核 | 8GB | 50GB+ |

### 1.4 安装 JDK 21

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-21-jdk

# CentOS/RHEL/AlmaLinux
sudo dnf install java-21-openjdk

# macOS (Homebrew)
brew install openjdk@21

# 验证安装
java -version
# 应输出: openjdk version "21.x.x"
```

### 1.5 安装 Maven

```bash
# Ubuntu/Debian
sudo apt install maven

# macOS (Homebrew)
brew install maven

# 手动安装 (所有平台)
wget https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz
tar -xzf apache-maven-3.9.6-bin.tar.gz
sudo mv apache-maven-3.9.6 /opt/maven
echo 'export PATH=/opt/maven/bin:$PATH' >> ~/.bashrc
source ~/.bashrc

# 验证安装
mvn -version
```

### 1.6 安装 Node.js 18+

```bash
# 使用 nvm (推荐)
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.7/install.sh | bash
source ~/.bashrc
nvm install 18
nvm use 18

# Ubuntu/Debian (NodeSource)
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs

# 验证安装
node -v   # 应 >= v18.0.0
npm -v    # 应 >= 9.0.0
```

---

## 2. 数据库初始化

### 2.1 安装 MySQL 8.0

```bash
# Ubuntu/Debian
sudo apt install mysql-server
sudo systemctl start mysql
sudo systemctl enable mysql

# CentOS/RHEL/AlmaLinux
sudo dnf install mysql-server
sudo systemctl start mysqld
sudo systemctl enable mysqld

# 安全初始化 (设置 root 密码等)
sudo mysql_secure_installation
```

### 2.2 创建数据库

```bash
# 登录 MySQL
mysql -u root -p
```

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS `campus_blog`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 创建专用用户 (生产环境推荐)
CREATE USER IF NOT EXISTS 'campus_blog'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON campus_blog.* TO 'campus_blog'@'localhost';
FLUSH PRIVILEGES;
```

### 2.3 导入数据库表结构

```bash
# 方式一：使用 root 用户
mysql -u root -p campus_blog < schema.sql

# 方式二：使用专用用户
mysql -u campus_blog -p campus_blog < schema.sql
```

### 2.4 应用性能优化索引 (必须)

```bash
# 导入性能索引脚本
mysql -u root -p campus_blog < 性能优化索引.sql
```

> **重要**: `性能优化索引.sql` 包含 16 个关键索引，可将慢查询从 15 秒+ 降至 50ms 以内。
> 每次数据库初始化后都必须执行此脚本。

### 2.5 验证导入

```bash
mysql -u root -p campus_blog -e "SHOW TABLES;"
# 应显示 22 张表
```

### 2.6 默认管理员账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |

> **安全警告**: 首次部署后请立即修改默认管理员密码！

---

## 3. 后端部署

### 3.1 项目结构

```
edu_project/
├── src/main/java/          # Java 源代码
├── src/main/resources/     # 配置文件
├── src/test/               # 测试代码 (H2 内存库)
├── pom.xml                 # Maven 配置
├── .env.example            # 环境变量模板
├── schema.sql             # 数据库初始化脚本
├── 性能优化索引.sql         # 性能索引脚本
├── docker-compose.yml      # Docker 编排配置
└── Dockerfile              # Docker 镜像构建
```

### 3.2 环境变量配置

```bash
# 进入后端目录
cd edu_project

# 复制环境变量模板
cp .env.example .env

# 编辑配置文件
nano .env
```

#### 必需环境变量

| 变量 | 说明 | 示例值 |
|------|------|--------|
| `DB_HOST` | 数据库主机地址 | `localhost` |
| `DB_PORT` | 数据库端口 | `3306` |
| `DB_NAME` | 数据库名称 | `campus_blog` |
| `DB_USERNAME` | 数据库用户名 | `campus_blog` |
| `DB_PASSWORD` | 数据库密码 | `your_secure_password` |
| `JWT_SECRET` | JWT 密钥 (至少 32 位) | `a_random_32_char_secret_key_here` |

#### 可选环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `SERVER_PORT` | 应用端口 | `8825` |
| `JWT_EXPIRATION` | Token 过期时间 (ms) | `86400000` (24h) |
| `JWT_REFRESH_EXPIRATION` | 刷新 Token 过期 (ms) | `604800000` (7d) |
| `CORS_ALLOWED_ORIGINS` | 允许的跨域来源 | `http://localhost:3000,http://localhost:8825` |
| `MAIL_HOST` | 邮件服务器 | `smtp.example.com` |
| `MAIL_PORT` | 邮件端口 | `587` |
| `MAIL_USERNAME` | 邮件用户名 | - |
| `MAIL_PASSWORD` | 邮件密码/授权码 | - |
| `MAIL_FROM` | 发件人地址 | `noreply@example.com` |
| `AVATAR_ALLOWED_DOMAINS` | 头像域名白名单 | `localhost,127.0.0.1` |
| `UPLOAD_MAX_FILE_SIZE` | 最大上传大小 (字节) | `524288000` (500MB) |
| `ENV_VALIDATION_ENABLED` | 启用环境变量验证 | `true` |

#### 生成安全的 JWT_SECRET

```bash
# Linux/macOS
openssl rand -base64 48

# 或使用 OpenSSL 生成 64 位随机字符串
openssl rand -hex 32
```

### 3.3 开发环境启动

```bash
cd edu_project

# 确保 .env 已配置
cp .env.example .env
# 编辑 .env 填入数据库密码和 JWT_SECRET

# Maven 开发模式启动 (支持热重载)
mvn spring-boot:run

# 或使用 IDE 直接运行 EduProjectApplication.java
```

启动后访问:
- API 接口: http://localhost:8825/api
- API 文档 (Knife4j): http://localhost:8825/api/doc.html
- Swagger UI: http://localhost:8825/api/swagger-ui.html
- 健康检查: http://localhost:8825/api/actuator/health

### 3.4 生产环境打包与部署

```bash
cd edu_project

# 清理并打包 (跳过测试)
mvn clean package -DskipTests

# 打包产物位于
ls -la target/edu_project-0.0.1-SNAPSHOT.jar

# 运行 JAR
java -jar target/edu_project-0.0.1-SNAPSHOT.jar

# 指定外部配置文件 (可选)
java -jar target/edu_project-0.0.1-SNAPSHOT.jar --spring.config.location=file:./application.yml

# 后台运行
nohup java -jar target/edu_project-0.0.1-SNAPSHOT.jar > app.log 2>&1 &

# 或使用 screen/tmux
screen -S campus-blog
java -jar target/edu_project-0.0.1-SNAPSHOT.jar
# Ctrl+A, D 分离会话
```

### 3.5 JVM 启动参数 (生产推荐)

```bash
java \
  -Xms512m \
  -Xmx1024m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/var/log/campus-blog/heapdump.hprof \
  -jar target/edu_project-0.0.1-SNAPSHOT.jar
```

| 参数 | 说明 |
|------|------|
| `-Xms512m` | 初始堆内存 |
| `-Xmx1024m` | 最大堆内存 |
| `-XX:+UseG1GC` | 使用 G1 垃圾回收器 |
| `-XX:MaxGCPauseMillis=200` | 最大 GC 停顿时间 |
| `-XX:+HeapDumpOnOutOfMemoryError` | OOM 时自动 dump |

---

## 4. 前端部署

### 4.1 项目结构

```
edu_project_vue/
├── src/
│   ├── api/              # API 接口模块 (17个)
│   ├── components/       # Vue 组件
│   │   ├── common/       # 通用组件 (8个)
│   │   └── layout/       # 布局组件 (3个)
│   ├── composables/      # 组合式函数
│   ├── router/           # 路由配置
│   ├── stores/           # Pinia 状态管理
│   ├── styles/           # 全局样式
│   ├── utils/            # 工具函数
│   └── views/            # 页面组件 (33个)
├── vite.config.js        # Vite 配置
├── package.json          # 依赖配置
└── dist/                 # 构建输出 (npm run build 后生成)
```

### 4.2 开发环境启动

```bash
cd edu_project_vue

# 安装依赖
npm install

# 启动开发服务器 (端口 3000)
npm run dev
```

开发服务器特性:
- 热模块替换 (HMR)
- 自动代理 `/api` 到 `http://localhost:8825`
- 自动代理 `/uploads` 到后端

Vite 代理配置 (`vite.config.js`):
```javascript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8825',
      changeOrigin: true
    },
    '/uploads': {
      target: 'http://localhost:8825',
      changeOrigin: true,
      rewrite: (path) => '/api' + path
    }
  }
}
```

### 4.3 生产环境构建

```bash
cd edu_project_vue

# 安装依赖 (如未安装)
npm install

# 构建生产版本
npm run build

# 构建产物位于 dist/ 目录
ls -la dist/
# index.html
# assets/
#   ├── index-<hash>.css
#   ├── vendor-<hash>.js
#   ├── markdown-<hash>.js
#   └── ...
```

构建配置说明:
- 输出目录: `dist/`
- 静态资源目录: `assets/`
- Source map: 已启用 (生产可关闭以减小体积)
- 代码分割: vendor (vue/vue-router/pinia), markdown (marked)

### 4.4 前端部署到 Nginx

```bash
# 将 dist 目录复制到 Nginx 站点目录
sudo mkdir -p /var/www/campus-blog
sudo cp -r edu_project_vue/dist/* /var/www/campus-blog/
sudo chown -R www-data:www-data /var/www/campus-blog
sudo chmod -R 755 /var/www/campus-blog
```

### 4.5 代码检查

```bash
# ESLint 检查
npm run lint

# 自动修复
npm run lint -- --fix
```

---

## 5. Docker 部署

### 5.1 前置条件

```bash
# 安装 Docker (Ubuntu/Debian)
sudo apt install docker.io docker-compose
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER
# 重新登录使组权限生效

# 验证安装
docker --version
docker compose version
```

### 5.2 配置环境变量

```bash
cd edu_project

# 复制并编辑 .env 文件
cp .env.example .env
nano .env
```

确保 `.env` 中至少包含:
```env
DB_HOST=mysql
DB_PORT=3306
DB_NAME=campus_blog
DB_USERNAME=campus_blog
DB_PASSWORD=your_secure_password
JWT_SECRET=your_jwt_secret_key_minimum_32_characters
SERVER_PORT=8825
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8825
```

### 5.3 启动服务

```bash
cd edu_project

# 构建并启动所有服务 (MySQL + 应用)
docker compose up -d --build

# 查看服务状态
docker compose ps

# 预期输出:
# NAME                IMAGE                  STATUS
# campus-blog-mysql   mysql:8.0              Up (healthy)
# campus-blog-app     edu_project-app        Up (healthy)
```

### 5.4 查看日志

```bash
# 查看所有服务日志
docker compose logs -f

# 仅查看应用日志
docker compose logs -f app

# 仅查看 MySQL 日志
docker compose logs -f mysql

# 查看最近 100 行
docker compose logs --tail=100 app
```

### 5.5 数据库初始化 (Docker)

Docker Compose 配置已包含自动初始化:
- `schema.sql` 映射到 `/docker-entrypoint-initdb.d/01-init.sql`
- 首次启动时 MySQL 容器会自动执行初始化脚本

如需手动执行性能索引:
```bash
# 复制索引脚本到容器
docker cp 性能优化索引.sql campus-blog-mysql:/tmp/perf-index.sql

# 在容器内执行
docker exec -i campus-blog-mysql mysql -u campus_blog -p"$DB_PASSWORD" campus_blog < /tmp/perf-index.sql
```

### 5.6 停止与清理

```bash
# 停止所有服务
docker compose down

# 停止并删除数据卷 (会清除所有数据库数据!)
docker compose down -v

# 停止并删除镜像
docker compose down --rmi all

# 清理未使用的 Docker 资源
docker system prune -f
```

### 5.7 更新应用

```bash
# 拉取最新代码
git pull

# 重新构建并启动
docker compose up -d --build app

# 查看新版本
docker compose logs app | head -20
```

### 5.8 Docker 资源限制

docker-compose.yml 已配置资源限制:

| 服务 | CPU 限制 | 内存限制 |
|------|----------|----------|
| MySQL | 0.5 核 | 512MB |
| App | 1.0 核 | 1GB |

---

## 6. Nginx 配置

### 6.1 安装 Nginx

```bash
# Ubuntu/Debian
sudo apt install nginx
sudo systemctl start nginx
sudo systemctl enable nginx

# CentOS/RHEL/AlmaLinux
sudo dnf install nginx
sudo systemctl start nginx
sudo systemctl enable nginx
```

### 6.2 完整 Nginx 配置

创建配置文件 `/etc/nginx/sites-available/campus-blog`:

```nginx
# HTTP → HTTPS 重定向
server {
    listen 80;
    server_name your-domain.com www.your-domain.com;

    # Let's Encrypt 证书验证路径
    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    # 其他所有请求重定向到 HTTPS
    location / {
        return 301 https://$server_name$request_uri;
    }
}

# HTTPS 主配置
server {
    listen 443 ssl http2;
    server_name your-domain.com www.your-domain.com;

    # SSL 证书 (Let's Encrypt)
    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;
    ssl_trusted_certificate /etc/letsencrypt/live/your-domain.com/chain.pem;

    # SSL 安全配置
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 1d;
    ssl_session_tickets off;
    ssl_stapling on;
    ssl_stapling_verify on;

    # 安全头
    add_header Strict-Transport-Security "max-age=63072000; includeSubDomains; preload" always;
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;

    # 日志配置
    access_log /var/log/nginx/campus-blog.access.log;
    error_log /var/log/nginx/campus-blog.error.log warn;

    # 上传文件大小限制 (与后端一致: 500MB)
    client_max_body_size 500m;

    # 代理超时配置
    proxy_connect_timeout 60s;
    proxy_send_timeout 60s;
    proxy_read_timeout 60s;

    # Gzip 压缩
    gzip on;
    gzip_vary on;
    gzip_proxied any;
    gzip_comp_level 6;
    gzip_min_length 256;
    gzip_types
        text/plain
        text/css
        text/xml
        text/javascript
        application/json
        application/javascript
        application/xml
        application/rss+xml
        image/svg+xml;

    # ============================================
    # 后端 API 反向代理
    # ============================================
    location /api {
        proxy_pass http://127.0.0.1:8825;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # 禁用代理缓冲 (实时流式响应)
        proxy_buffering off;

        # 隐藏后端服务器信息
        proxy_hide_header X-Powered-By;
    }

    # ============================================
    # 上传文件代理
    # ============================================
    location /uploads {
        proxy_pass http://127.0.0.1:8825/api/uploads;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # 静态文件缓存
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    # ============================================
    # 前端静态文件
    # ============================================
    location / {
        root /var/www/campus-blog;
        index index.html;
        try_files $uri $uri/ /index.html;

        # 静态资源缓存策略
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
            expires 30d;
            add_header Cache-Control "public, immutable";
        }

        # HTML 文件不缓存
        location ~* \.html$ {
            expires -1;
            add_header Cache-Control "no-cache, no-store, must-revalidate";
        }
    }

    # ============================================
    # 健康检查端点 (可选, 限内网访问)
    # ============================================
    location /api/actuator/health {
        proxy_pass http://127.0.0.1:8825;
        allow 127.0.0.1;
        allow ::1;
        deny all;
    }
}
```

### 6.3 启用配置

```bash
# 创建符号链接启用站点
sudo ln -sf /etc/nginx/sites-available/campus-blog /etc/nginx/sites-enabled/

# 删除默认站点 (可选)
sudo rm -f /etc/nginx/sites-enabled/default

# 测试配置语法
sudo nginx -t

# 重新加载 Nginx
sudo systemctl reload nginx
```

### 6.4 配置 Let's Encrypt HTTPS

```bash
# 安装 Certbot
sudo apt install certbot python3-certbot-nginx

# 获取证书 (会自动修改 Nginx 配置)
sudo certbot --nginx -d your-domain.com -d www.your-domain.com

# 验证自动续期
sudo certbot renew --dry-run

# 查看证书状态
sudo certbot certificates
```

自动续期已通过 systemd timer 配置，无需手动设置 cron。

---

## 7. systemd 服务

### 7.1 创建服务文件

创建 `/etc/systemd/system/campus-blog.service`:

```ini
[Unit]
Description=Campus Blog Forum System - Backend API
Documentation=https://github.com/Xinghe-0203/Campus_Blog
After=network.target mysql.service
Wants=mysql.service

[Service]
Type=simple
User=campus-blog
Group=campus-blog
WorkingDirectory=/opt/campus-blog
EnvironmentFile=/opt/campus-blog/.env

# JVM 参数
ExecStart=/usr/bin/java \
    -Xms512m \
    -Xmx1024m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/var/log/campus-blog/heapdump.hprof \
    -jar /opt/campus-blog/edu_project-0.0.1-SNAPSHOT.jar

# 重启策略
Restart=always
RestartSec=10
SuccessExitStatus=143

# 安全加固
NoNewPrivileges=true
ProtectSystem=strict
ProtectHome=true
ReadWritePaths=/opt/campus-blog/uploads /var/log/campus-blog

# 日志
StandardOutput=journal
StandardError=journal
SyslogIdentifier=campus-blog

[Install]
WantedBy=multi-user.target
```

### 7.2 创建专用用户和目录

```bash
# 创建专用系统用户
sudo useradd --system --no-create-home --shell /bin/false campus-blog

# 创建应用目录
sudo mkdir -p /opt/campus-blog
sudo mkdir -p /opt/campus-blog/uploads
sudo mkdir -p /var/log/campus-blog

# 设置权限
sudo chown -R campus-blog:campus-blog /opt/campus-blog
sudo chown -R campus-blog:campus-blog /var/log/campus-blog
sudo chmod 750 /opt/campus-blog
sudo chmod 750 /var/log/campus-blog
```

### 7.3 部署应用

```bash
# 复制 JAR 和环境变量文件
sudo cp edu_project/target/edu_project-0.0.1-SNAPSHOT.jar /opt/campus-blog/
sudo cp edu_project/.env /opt/campus-blog/

# 设置权限
sudo chown campus-blog:campus-blog /opt/campus-blog/edu_project-0.0.1-SNAPSHOT.jar
sudo chown campus-blog:campus-blog /opt/campus-blog/.env
sudo chmod 600 /opt/campus-blog/.env
```

### 7.4 管理服务

```bash
# 重新加载 systemd 配置
sudo systemctl daemon-reload

# 启动服务
sudo systemctl start campus-blog

# 设置开机自启
sudo systemctl enable campus-blog

# 查看服务状态
sudo systemctl status campus-blog

# 查看日志
sudo journalctl -u campus-blog -f

# 查看最近 100 行日志
sudo journalctl -u campus-blog -n 100

# 重启服务
sudo systemctl restart campus-blog

# 停止服务
sudo systemctl stop campus-blog
```

### 7.5 日志轮转配置

创建 `/etc/logrotate.d/campus-blog`:

```conf
/var/log/campus-blog/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 0640 campus-blog campus-blog
    sharedscripts
    postrotate
        systemctl reload campus-blog > /dev/null 2>&1 || true
    endscript
}
```

---

## 8. 生产检查清单

### 8.1 部署前检查

- [ ] Java 21 已安装并验证 (`java -version`)
- [ ] MySQL 8.0 已安装并运行
- [ ] 数据库 `campus_blog` 已创建
- [ ] 数据库表结构已导入 (`schema.sql`)
- [ ] 性能索引已应用 (`性能优化索引.sql`)
- [ ] `.env` 文件已配置并填写所有必需变量
- [ ] `JWT_SECRET` 已设置为强随机字符串 (至少 32 位)
- [ ] `DB_PASSWORD` 已设置为强密码
- [ ] `CORS_ALLOWED_ORIGINS` 已设置为实际域名 (非 `*`)
- [ ] 默认管理员密码已修改
- [ ] 后端打包成功 (`mvn clean package -DskipTests`)
- [ ] 前端构建成功 (`npm run build`)

### 8.2 安全配置

- [ ] `.env` 文件权限设置为 `600` (仅所有者可读写)
- [ ] 数据库用户权限最小化 (仅 `campus_blog` 数据库)
- [ ] 防火墙仅开放必要端口 (80, 443)
- [ ] SSH 已配置密钥认证，禁用密码登录
- [ ] fail2ban 已安装并配置
- [ ] SSL 证书已配置且自动续期
- [ ] 安全响应头已配置 (HSTS, X-Frame-Options 等)
- [ ] Nginx 已隐藏版本信息 (`server_tokens off`)
- [ ] 数据库禁止远程访问 (`bind-address = 127.0.0.1`)

### 8.3 数据库备份

- [ ] 自动备份脚本已配置
- [ ] Cron 定时任务已设置 (建议每日凌晨)
- [ ] 备份保留策略已配置 (建议 30 天)
- [ ] 备份恢复已测试
- [ ] 异地备份已配置 (可选)

### 8.4 监控与告警

- [ ] 应用健康检查端点可访问 (`/api/actuator/health`)
- [ ] 系统资源监控已配置 (CPU, 内存, 磁盘)
- [ ] 数据库慢查询日志已启用
- [ ] Nginx 访问日志已配置
- [ ] 应用错误日志已配置
- [ ] SSL 证书到期告警已设置

### 8.5 防火墙配置

```bash
# UFW (Ubuntu)
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw enable

# firewalld (CentOS/RHEL)
sudo firewall-cmd --permanent --add-service=ssh
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --reload

# 验证规则
sudo ufw status          # UFW
sudo firewall-cmd --list-all  # firewalld
```

> **注意**: 后端端口 8825 不应对外开放，仅通过 Nginx 反向代理访问。

---

## 9. 故障排查

### 9.1 数据库连接失败

**症状**: 应用启动时报 `Connection refused` 或 `Access denied`

**排查步骤**:

```bash
# 1. 检查 MySQL 服务状态
sudo systemctl status mysql

# 2. 检查端口是否监听
sudo netstat -tlnp | grep 3306
# 或
sudo ss -tlnp | grep 3306

# 3. 测试连接
mysql -u campus_blog -p -h localhost campus_blog

# 4. 检查用户权限
mysql -u root -p -e "SHOW GRANTS FOR 'campus_blog'@'localhost';"

# 5. 检查 .env 中的数据库配置
cat .env | grep DB_

# 6. 检查 MySQL 绑定地址
grep bind-address /etc/mysql/mysql.conf.d/mysqld.cnf
# 应为: bind-address = 127.0.0.1
```

**常见原因**:
- MySQL 服务未启动
- 用户名或密码错误
- 用户权限不足
- 数据库不存在
- 防火墙阻止连接

### 9.2 端口冲突

**症状**: `Port 8825 is already in use`

**排查步骤**:

```bash
# 查找占用端口的进程
sudo lsof -i :8825
# 或
sudo netstat -tlnp | grep 8825
# 或
sudo ss -tlnp | grep 8825

# 结束占用进程
sudo kill -9 <PID>

# 或修改 .env 中的 SERVER_PORT
nano .env
# SERVER_PORT=8826
```

### 9.3 CORS 跨域错误

**症状**: 浏览器控制台报 CORS 错误，前端无法调用 API

**排查步骤**:

```bash
# 1. 检查 .env 中的 CORS 配置
cat .env | grep CORS_ALLOWED_ORIGINS

# 2. 确保包含前端域名
# CORS_ALLOWED_ORIGINS=http://your-domain.com,http://localhost:3000

# 3. 重启应用使配置生效
sudo systemctl restart campus-blog

# 4. 检查 Nginx 是否正确代理
curl -I http://your-domain.com/api/actuator/health
# 应返回 200 OK
```

**注意**: 生产环境禁止使用 `*` 通配符，必须指定具体域名。

### 9.4 JWT 认证失败

**症状**: 登录后请求 API 返回 401 Unauthorized

**排查步骤**:

```bash
# 1. 检查 JWT_SECRET 是否配置
cat .env | grep JWT_SECRET

# 2. 检查 JWT_SECRET 长度 (至少 32 字符)
echo -n "your_jwt_secret" | wc -c

# 3. 检查请求头是否正确
# 应为: Authorization: Bearer <token>

# 4. 检查 Token 是否过期
# 默认 access token 24 小时, refresh token 7 天

# 5. 查看应用日志中的认证错误
sudo journalctl -u campus-blog | grep -i "jwt\|auth\|token"
```

### 9.5 前端白屏

**症状**: 访问前端页面显示空白

**排查步骤**:

```bash
# 1. 检查浏览器控制台错误
# F12 → Console 查看 JavaScript 错误

# 2. 检查 dist/ 目录是否完整
ls -la /var/www/campus-blog/

# 3. 检查 Nginx 配置
sudo nginx -t
sudo cat /var/log/nginx/campus-blog.error.log

# 4. 检查 API 是否可访问
curl http://127.0.0.1:8825/api/actuator/health

# 5. 重新构建前端
cd edu_project_vue
npm run build
sudo cp -r dist/* /var/www/campus-blog/
```

### 9.6 文件上传失败

**症状**: 上传图片或文件时返回错误

**排查步骤**:

```bash
# 1. 检查 uploads 目录权限
ls -la /opt/campus-blog/uploads/
sudo chown -R campus-blog:campus-blog /opt/campus-blog/uploads/

# 2. 检查 Nginx 上传大小限制
grep client_max_body_size /etc/nginx/sites-available/campus-blog
# 应 >= 后端配置 (默认 500MB)

# 3. 检查后端上传配置
cat .env | grep UPLOAD_MAX_FILE_SIZE

# 4. 查看错误日志
sudo journalctl -u campus-blog | grep -i "upload\|file"
```

### 9.7 日志位置汇总

| 组件 | 日志位置 | 查看命令 |
|------|----------|----------|
| 后端 (systemd) | journal | `sudo journalctl -u campus-blog -f` |
| 后端 (nohup) | app.log | `tail -f app.log` |
| Docker 应用 | Docker logs | `docker compose logs -f app` |
| Nginx 访问 | /var/log/nginx/campus-blog.access.log | `tail -f /var/log/nginx/campus-blog.access.log` |
| Nginx 错误 | /var/log/nginx/campus-blog.error.log | `tail -f /var/log/nginx/campus-blog.error.log` |
| MySQL 错误 | /var/log/mysql/error.log | `tail -f /var/log/mysql/error.log` |

### 9.8 快速诊断脚本

```bash
#!/bin/bash
echo "=== Campus Blog 诊断报告 ==="
echo ""

echo "[1] Java 版本:"
java -version 2>&1 | head -1

echo ""
echo "[2] MySQL 状态:"
sudo systemctl is-active mysql 2>/dev/null || echo "MySQL 未运行"

echo ""
echo "[3] 后端服务状态:"
sudo systemctl is-active campus-blog 2>/dev/null || echo "后端服务未运行"

echo ""
echo "[4] Nginx 状态:"
sudo systemctl is-active nginx 2>/dev/null || echo "Nginx 未运行"

echo ""
echo "[5] 端口监听:"
sudo ss -tlnp | grep -E ':(80|443|3000|3306|8825) '

echo ""
echo "[6] .env 文件检查:"
if [ -f .env ]; then
    echo "  .env 存在"
    grep -c "=" .env | xargs -I{} echo "  配置项数量: {}"
else
    echo "  .env 不存在!"
fi

echo ""
echo "[7] 磁盘使用:"
df -h / | tail -1

echo ""
echo "[8] 内存使用:"
free -h | grep Mem
```

---

## 10. 性能调优

### 10.1 HikariCP 数据库连接池

在 `application.yml` 或环境变量中配置:

```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 5              # 最小空闲连接
      maximum-pool-size: 20        # 最大连接池大小
      idle-timeout: 600000         # 空闲超时 (10分钟)
      max-lifetime: 1800000        # 连接最大生命周期 (30分钟)
      connection-timeout: 30000    # 连接超时 (30秒)
      validation-timeout: 5000     # 验证超时 (5秒)
```

**调优建议**:

| 场景 | minimum-idle | maximum-pool-size |
|------|-------------|-------------------|
| 开发 | 2 | 10 |
| 生产 (小型) | 5 | 20 |
| 生产 (中型) | 10 | 50 |
| 生产 (大型) | 20 | 100 |

> 公式参考: `pool_size = CPU核心数 * 2 + 磁盘数`

### 10.2 Caffeine 本地缓存

项目使用 Caffeine 缓存热点数据，配置位于 `application.yml`:

```yaml
spring:
  cache:
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=30m
```

**缓存策略说明**:

| 参数 | 说明 | 推荐值 |
|------|------|--------|
| `maximumSize` | 最大缓存条目数 | 1000-5000 |
| `expireAfterWrite` | 写入后过期时间 | 30m |
| `expireAfterAccess` | 访问后过期时间 (可选) | 10m |

**已缓存的数据**:
- 用户信息
- 文章详情
- 标签列表
- 系统配置

### 10.3 MySQL 优化

创建 `/etc/mysql/conf.d/custom.cnf`:

```ini
[mysqld]
# 字符集
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci

# 连接配置
max_connections = 200
max_connect_errors = 1000
wait_timeout = 600
interactive_timeout = 600

# InnoDB 配置
innodb_buffer_pool_size = 1G          # 设置为物理内存的 50-70%
innodb_log_file_size = 256M
innodb_flush_log_at_trx_commit = 2    # 1=最安全, 2=性能更好
innodb_flush_method = O_DIRECT
innodb_io_capacity = 2000
innodb_io_capacity_max = 4000

# 查询缓存 (MySQL 8.0 已移除，使用应用层缓存)
# query_cache_type = 0

# 慢查询日志
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow.log
long_query_time = 2
log_queries_not_using_indexes = 0

# 临时表配置
tmp_table_size = 64M
max_heap_table_size = 64M

# 排序和连接缓冲
sort_buffer_size = 4M
join_buffer_size = 4M
read_buffer_size = 2M
read_rnd_buffer_size = 4M

# 二进制日志 (按需开启)
# server-id = 1
# log_bin = mysql-bin
# binlog_format = ROW
# expire_logs_days = 7
```

应用配置并重启:
```bash
sudo systemctl restart mysql
```

### 10.4 Nginx 调优

在 `/etc/nginx/nginx.conf` 的 `http` 块中添加:

```nginx
http {
    # 工作进程数 (通常等于 CPU 核心数)
    worker_processes auto;
    worker_rlimit_nofile 65535;

    events {
        worker_connections 4096;
        multi_accept on;
        use epoll;
    }

    # 文件缓存
    open_file_cache max=10000 inactive=30s;
    open_file_cache_valid 60s;
    open_file_cache_min_uses 2;
    open_file_cache_errors on;

    # 客户端配置
    client_body_buffer_size 16k;
    client_header_buffer_size 1k;
    client_max_body_size 500m;
    large_client_header_buffers 4 16k;

    # 超时配置
    client_body_timeout 12;
    client_header_timeout 12;
    keepalive_timeout 65;
    send_timeout 10;

    # 发送优化
    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;

    # ... 其余配置
}
```

验证并重启:
```bash
sudo nginx -t
sudo systemctl reload nginx
```

### 10.5 JVM 调优

生产环境推荐 JVM 参数:

```bash
java \
  -Xms512m \
  -Xmx1024m \
  -XX:MetaspaceSize=256m \
  -XX:MaxMetaspaceSize=512m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:G1HeapRegionSize=4m \
  -XX:InitiatingHeapOccupancyPercent=45 \
  -XX:+ParallelRefProcEnabled \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/var/log/campus-blog/heapdump.hprof \
  -XX:ErrorFile=/var/log/campus-blog/hs_err_pid%p.log \
  -Xlog:gc*:file=/var/log/campus-blog/gc.log:time,uptime,level,tags:filecount=5,filesize=10m \
  -jar edu_project-0.0.1-SNAPSHOT.jar
```

### 10.6 性能基准参考

| 接口 | 优化前 | 优化后 | 提升倍数 |
|------|--------|--------|----------|
| `/api/message/unread-count` | 15248ms | <50ms | 300x |
| `/api/trending/*` | 15976ms | <200ms | 80x |
| `/api/post/list` | 18117ms | <300ms | 60x |
| `/api/statistics/community` | 7855ms | <500ms | 15x |
| `/api/notification/unread-count` | 未知 | <50ms | 缓存命中 |

---

## 11. 备份与恢复

### 11.1 手动备份

```bash
# 数据库备份
mysqldump -u campus_blog -p \
  --single-transaction \
  --routines \
  --triggers \
  --events \
  campus_blog > backup_$(date +%Y%m%d_%H%M%S).sql

# 压缩备份
mysqldump -u campus_blog -p campus_blog | gzip > backup_$(date +%Y%m%d_%H%M%S).sql.gz

# 上传文件备份
tar -czf uploads_$(date +%Y%m%d_%H%M%S).tar.gz /opt/campus-blog/uploads/
```

### 11.2 手动恢复

```bash
# 恢复数据库 (从 SQL 文件)
mysql -u campus_blog -p campus_blog < backup_20260517_030000.sql

# 恢复数据库 (从压缩文件)
gunzip < backup_20260517_030000.sql.gz | mysql -u campus_blog -p campus_blog

# 恢复上传文件
tar -xzf uploads_20260517_030000.tar.gz -C /opt/campus-blog/
```

### 11.3 Docker 环境备份与恢复

```bash
# 备份数据库
docker exec campus-blog-mysql mysqldump -u campus_blog -p"$DB_PASSWORD" campus_blog > backup.sql

# 恢复数据库
cat backup.sql | docker exec -i campus-blog-mysql mysql -u campus_blog -p"$DB_PASSWORD" campus_blog

# 备份数据卷
docker run --rm -v campus-blog_mysql_data:/data -v $(pwd):/backup alpine tar czf /backup/mysql-data.tar.gz -C /data .

# 恢复数据卷
docker run --rm -v campus-blog_mysql_data:/data -v $(pwd):/backup alpine tar xzf /backup/mysql-data.tar.gz -C /data
```

### 11.4 自动化备份脚本

创建 `/opt/campus-blog/backup.sh`:

```bash
#!/bin/bash
# ============================================
# 校园博客系统 - 自动备份脚本
# ============================================

set -e

# 配置
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR=/opt/campus-blog/backups
DB_USER="campus_blog"
DB_NAME="campus_blog"
DB_PASS=$(grep DB_PASSWORD /opt/campus-blog/.env | cut -d'=' -f2)
RETENTION_DAYS=30

# 创建备份目录
mkdir -p "$BACKUP_DIR/database"
mkdir -p "$BACKUP_DIR/uploads"

# 备份数据库
echo "[$(date)] 开始数据库备份..."
mysqldump -u "$DB_USER" -p"$DB_PASS" \
  --single-transaction \
  --routines \
  --triggers \
  --events \
  --add-drop-database \
  "$DB_NAME" | gzip > "$BACKUP_DIR/database/backup_${DATE}.sql.gz"

echo "[$(date)] 数据库备份完成: backup_${DATE}.sql.gz"

# 备份上传文件
echo "[$(date)] 开始上传文件备份..."
tar -czf "$BACKUP_DIR/uploads/uploads_${DATE}.tar.gz" \
  -C /opt/campus-blog uploads/

echo "[$(date)] 上传文件备份完成: uploads_${DATE}.tar.gz"

# 清理过期备份
echo "[$(date)] 清理 ${RETENTION_DAYS} 天前的备份..."
find "$BACKUP_DIR/database" -name "backup_*.sql.gz" -mtime +$RETENTION_DAYS -delete
find "$BACKUP_DIR/uploads" -name "uploads_*.tar.gz" -mtime +$RETENTION_DAYS -delete

echo "[$(date)] 备份完成并清理过期文件"

# 显示备份大小
echo ""
echo "=== 最新备份 ==="
ls -lh "$BACKUP_DIR/database/backup_${DATE}.sql.gz"
ls -lh "$BACKUP_DIR/uploads/uploads_${DATE}.tar.gz"
```

设置权限并测试:
```bash
sudo chmod +x /opt/campus-blog/backup.sh
sudo chown campus-blog:campus-blog /opt/campus-blog/backup.sh
sudo /opt/campus-blog/backup.sh
```

### 11.5 定时备份 (Cron)

```bash
# 编辑 crontab
sudo crontab -e

# 添加以下行 (每天凌晨 3:00 执行备份)
0 3 * * * /opt/campus-blog/backup.sh >> /var/log/campus-blog/backup.log 2>&1
```

### 11.6 备份验证

定期验证备份文件有效性:

```bash
# 验证数据库备份 (不实际导入)
gunzip -t backup_20260517_030000.sql.gz

# 在测试环境恢复验证
mysql -u root -p -e "CREATE DATABASE test_restore;"
mysql -u root -p test_restore < backup_20260517_030000.sql
mysql -u root -p -e "USE test_restore; SHOW TABLES;"
mysql -u root -p -e "DROP DATABASE test_restore;"
```

---

## 附录

### A. 快速启动命令速查

```bash
# === 开发环境 ===
cd edu_project && cp .env.example .env && mvn spring-boot:run
cd edu_project_vue && npm install && npm run dev

# === 生产环境 ===
cd edu_project && mvn clean package -DskipTests
sudo systemctl start campus-blog
cd edu_project_vue && npm run build && sudo cp -r dist/* /var/www/campus-blog/
sudo systemctl reload nginx

# === Docker ===
cd edu_project && docker compose up -d --build

# === 数据库 ===
mysql -u root -p campus_blog < schema.sql
mysql -u root -p campus_blog < 性能优化索引.sql

# === 服务管理 ===
sudo systemctl status campus-blog
sudo journalctl -u campus-blog -f
sudo systemctl restart campus-blog
```

### B. 端口汇总

| 端口 | 服务 | 说明 |
|------|------|------|
| 80 | Nginx | HTTP (重定向到 HTTPS) |
| 443 | Nginx | HTTPS |
| 3000 | Vite | 前端开发服务器 |
| 3306 | MySQL | 数据库 (仅本地) |
| 8825 | Spring Boot | 后端 API (仅本地/Nginx代理) |

### C. 关键文件路径

| 文件 | 路径 |
|------|------|
| 后端 JAR | `/opt/campus-blog/edu_project-0.0.1-SNAPSHOT.jar` |
| 环境变量 | `/opt/campus-blog/.env` |
| 上传文件 | `/opt/campus-blog/uploads/` |
| 前端静态文件 | `/var/www/campus-blog/` |
| Nginx 配置 | `/etc/nginx/sites-available/campus-blog` |
| systemd 服务 | `/etc/systemd/system/campus-blog.service` |
| 备份脚本 | `/opt/campus-blog/backup.sh` |
| 后端日志 | `journalctl -u campus-blog` |
| Nginx 日志 | `/var/log/nginx/campus-blog.*.log` |

### D. 相关链接

- 项目 GitHub: https://github.com/Xinghe-0203/Campus_Blog
- Spring Boot 文档: https://docs.spring.io/spring-boot/docs/current/reference/html/
- Vue 3 文档: https://vuejs.org/
- Nginx 文档: https://nginx.org/en/docs/
- MySQL 文档: https://dev.mysql.com/doc/

---

*文档版本: v2.0 | 最后更新: 2026-05-17 | 开发者: 刘畅*
