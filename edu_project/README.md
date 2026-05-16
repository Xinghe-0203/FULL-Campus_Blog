# 校园博客论坛系统

一个基于 Spring Boot 3 + Vue 3 的全栈校园博客论坛系统。

> **当前版本**: v1.52

## 项目结构

```
edu_project/               # 后端 (Spring Boot 3 + MyBatis Plus)
│   ├── src/               # Java 源码
│   ├── pom.xml            # Maven 配置
│   ├── docker-compose.yml # Docker 部署
│   ├── Dockerfile         # Docker 镜像
│   └── .env.example       # 环境变量模板
│
└── edu_project_vue/        # 前端 (Vue 3 + Vite)
    ├── src/               # Vue 源码
    ├── package.json       # npm 依赖
    └── vite.config.js     # Vite 配置
```

## 技术栈

### 后端
| 组件 | 版本 | 说明 |
|------|------|------|
| Java | 21 | LTS 版本 |
| Spring Boot | 3.3.0 | Web 框架 |
| MyBatis Plus | 3.5.7 | ORM 框架 |
| Spring Security | 6.x | 安全认证 |
| JWT (JJWT) | 0.12.3 | Token 认证 |
| Knife4j | 4.5.0 | API 文档 |
| MySQL | 8.x | 数据库 |
| Caffeine | 3.1.8 | 本地缓存 |

### 前端
| 组件 | 版本 | 说明 |
|------|------|------|
| Vue | ^3.4.21 | UI 框架 |
| Vite | ^5.2.0 | 构建工具 |
| Vue Router | ^4.3.0 | 路由管理 |
| Pinia | ^2.1.7 | 状态管理 |
| Axios | ^1.6.8 | HTTP 客户端 |
| Marked | ^12.0.1 | Markdown 渲染 |

## 快速开始

### 环境要求
- JDK 21+
- Node.js 18+
- MySQL 8.0+

### 1. 启动后端

```bash
# 进入后端目录
cd edu_project

# 复制环境变量配置
cp .env.example .env

# 或 Windows:
copy .env.example .env

# 修改 .env 文件中的数据库配置
# DB_HOST=localhost
# DB_PORT=3306
# DB_NAME=campus_blog
# DB_USERNAME=root
# DB_PASSWORD=your_password
# JWT_SECRET=your_secret_key_at_least_32_chars

# 编译并运行
mvn spring-boot:run
```

后端启动后访问：
- API: http://localhost:8825/api
- API 文档: http://localhost:8825/api/doc.html

### 2. 启动前端

```bash
# 进入前端目录
cd edu_project_vue

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端启动后访问：http://localhost:3000

### 3. 默认管理员账号

- 用户名: `admin`
- 密码: `admin123`

## 功能特性

### 用户模块
- 用户注册/登录
- JWT Token 认证 + 自动刷新
- 个人资料编辑
- 头像上传
- 修改密码
- 密码找回（邮箱验证码）

### 文章模块
- 文章发布/编辑/删除
- Markdown 编辑器 + 实时预览
- 文章封面图
- 文章标签
- 草稿自动保存
- 文章浏览量统计

### 互动模块
- 点赞/取消点赞
- 收藏/取消收藏
- 评论/回复
- 关注/取消关注
- 粉丝/关注列表

### 校友圈模块
- 发布动态（支持图片）
- 点赞/评论
- 转发
- 推荐/关注动态流

### 通知模块
- 消息通知（点赞、评论、关注）
- 未读消息数
- 私信功能

### 搜索模块
- 全站搜索
- 高级搜索
- 热门文章
- 热门标签

### 管理后台
- 用户管理（启用/禁用）
- 文章管理（审核/删除）
- 举报管理
- 数据统计

## 后端 API

完整的 API 文档请参考 [campus_blog.md](campus_blog.md) 第 7 节（共 100+ 端点，覆盖 22 个模块）。

API 文档页面（Knife4j）：http://localhost:8825/api/doc.html

## 数据库

### 表结构
| 表名 | 说明 |
|------|------|
| sys_user | 用户表 |
| blog_post | 文章表 |
| blog_comment | 评论表 |
| blog_tag | 标签表 |
| blog_post_tag | 文章标签关联表 |
| blog_like | 点赞表 |
| blog_collect | 收藏表 |
| blog_follow | 关注表 |
| blog_notification | 通知表 |
| blog_trending | 热门统计表 |
| blog_draft | 草稿表 |
| blog_draft_tag | 草稿标签关联表 |
| blog_report | 举报表 |
| blog_circle_post | 校友圈动态表 |
| blog_circle_like | 校友圈点赞表 |
| blog_circle_comment | 校友圈评论表 |
| blog_circle_repost | 校友圈转发表 |
| blog_media | 媒体文件表 |
| blog_post_media | 文章媒体关联表 |
| blog_topic | 话题表 |
| blog_message | 私信表 |
| blog_share | 分享记录表 |

### 初始化数据库

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE campus_blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入表结构
USE campus_blog;
SOURCE edu_project/数据库表.sql;
```

## 部署

### Docker 部署

```bash
cd edu_project

# 构建并启动
docker-compose up -d

# 查看日志
docker-compose logs -f
```

### 手动部署

```bash
# 编译后端
cd edu_project
mvn clean package -DskipTests

# 运行
java -jar target/edu_project-0.0.1-SNAPSHOT.jar

# 构建前端
cd ../edu_project_vue
npm run build

# 将 dist 目录部署到 Nginx
```

## 日志系统

### 日志
应用日志输出到 `logs/application.log`，通过 Logback 配置。

## 安全特性

### IP 伪造防护
- `LogUtils.getClientIp()` 对所有获取的 IP 进行格式校验
- 支持 IPv4 和 IPv6 格式验证
- 拒绝 `unknown`、非法格式的 IP
- 多级代理场景下只返回第一个有效且格式合法的客户端 IP
- 无法获取有效 IP 时返回默认值 `0.0.0.0`

## 开发规范

### 代码规范
- 后端遵循 Spring Boot 最佳实践
- 前端使用 Vue 3 Composition API
- 统一的 API 响应格式 `Result<T>`
- JWT Token 自动刷新机制

### Git 提交规范
- `feat`: 新功能
- `fix`: 修复 bug
- `docs`: 文档更新
- `style`: 代码格式
- `refactor`: 重构
- `test`: 测试
- `chore`: 构建/工具

## 许可证

本项目仅供学习交流使用。
