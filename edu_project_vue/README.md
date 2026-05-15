# 校园博客论坛 - Vue 3 前端

基于 Vue 3 + Vite 的校园博客论坛前端项目。

## 技术栈

- **Vue 3.4** - 渐进式 JavaScript 框架
- **Vite 5.2** - 下一代前端构建工具
- **Vue Router 4.3** - 官方路由管理器
- **Pinia 2.1** - 新一代状态管理
- **Axios 1.6** - HTTP 客户端
- **Marked 12.0** - Markdown 解析器
- **Highlight.js 11.9** - 代码语法高亮
- **DOMPurify 3.0** - HTML 净化器

## 快速开始

### 环境要求
- Node.js 18+
- npm 或 yarn

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:3000

### 构建生产版本

```bash
npm run build
```

构建产物将输出到 `dist/` 目录。

## 项目结构

```
edu_project_vue/
├── public/                    # 静态资源
│   └── favicon.svg           # 网站图标
├── src/                       # 源码目录
│   ├── api/                   # API 接口模块
│   │   ├── index.js          # Axios 实例配置
│   │   ├── user.js           # 用户 API
│   │   ├── post.js           # 文章 API
│   │   ├── comment.js        # 评论 API
│   │   ├── like.js           # 点赞 API
│   │   ├── collect.js        # 收藏 API
│   │   ├── follow.js         # 关注 API
│   │   ├── notification.js   # 通知 API
│   │   ├── message.js        # 私信 API
│   │   ├── circle.js         # 校友圈 API
│   │   ├── tag.js            # 标签 API
│   │   ├── topic.js          # 话题 API
│   │   ├── trending.js       # 热门 API
│   │   ├── media.js          # 媒体 API
│   │   ├── report.js         # 举报 API
│   │   ├── share.js          # 分享 API
│   │   └── admin.js          # 管理员 API
│   ├── components/            # 组件
│   │   ├── common/           # 通用组件 (8个)
│   │   │   ├── BackToTop.vue    # 返回顶部
│   │   │   ├── EmptyState.vue   # 空状态提示
│   │   │   ├── FileUploader.vue # 文件上传
│   │   │   ├── ImagePreview.vue # 图片预览灯箱
│   │   │   ├── Modal.vue        # 通用模态框
│   │   │   ├── PostCard.vue     # 文章卡片
│   │   │   ├── Skeleton.vue     # 骨架屏 (6种样式)
│   │   │   └── Toast.vue        # Toast 通知
│   │   └── layout/           # 布局组件 (3个)
│   │       ├── Footer.vue    # 页脚
│   │       ├── Navbar.vue    # 导航栏
│   │       └── PageTransition.vue # 页面过渡动画
│   ├── composables/           # 组合式函数
│   │   └── useConfirm.js     # 确认对话框组合式函数
│   ├── router/                # 路由配置
│   │   └── index.js
│   ├── stores/                # Pinia 状态管理
│   │   ├── user.js           # 用户状态
│   │   ├── theme.js          # 主题状态
│   │   └── app.js            # 应用状态
│   ├── styles/                # 样式文件
│   │   └── main.css          # 主样式
│   ├── utils/                 # 工具函数
│   │   ├── index.js          # 通用工具
│   │   └── logger.js         # 日志系统
│   ├── views/                 # 页面组件
│   │   ├── Home.vue          # 首页
│   │   ├── auth/             # 认证页面
│   │   │   ├── Login.vue
│   │   │   ├── Register.vue
│   │   │   └── PasswordReset.vue
│   │   ├── post/             # 文章页面
│   │   │   ├── PostDetail.vue
│   │   │   ├── PostEdit.vue
│   │   │   └── PostSearch.vue
│   │   ├── user/             # 用户页面
│   │   │   ├── Profile.vue
│   │   │   ├── ProfileEdit.vue
│   │   │   ├── UserProfile.vue
│   │   │   ├── PasswordChange.vue
│   │   │   ├── Drafts.vue
│   │   │   ├── Collections.vue
│   │   │   ├── Following.vue
│   │   │   ├── Followers.vue
│   │   │   ├── MyReports.vue
│   │   │   ├── Notifications.vue
│   │   │   └── Messages.vue
│   │   ├── circle/           # 校友圈页面
│   │   │   ├── Circle.vue
│   │   │   ├── CircleDetail.vue
│   │   │   └── CirclePost.vue
│   │   ├── tag/              # 标签页面
│   │   │   ├── Tags.vue
│   │   │   └── TagPosts.vue
│   │   ├── topic/            # 话题页面
│   │   │   ├── Topics.vue
│   │   │   └── TopicDetail.vue
│   │   ├── search/           # 搜索页面
│   │   │   └── Search.vue
│   │   ├── common/           # 公共页面
│   │   │   ├── NotFound.vue
│   │   │   └── Report.vue
│   │   └── admin/            # 管理后台
│   │       ├── Dashboard.vue
│   │       ├── Users.vue
│   │       ├── Posts.vue
│   │       ├── Reports.vue
│   │       └── Statistics.vue
│   ├── App.vue                # 根组件
│   └── main.js                # 应用入口
├── index.html                 # HTML 入口
├── package.json               # npm 配置
└── vite.config.js             # Vite 配置
```

## 页面列表

### 公开页面
| 页面 | 路径 | 说明 |
|------|------|------|
| 首页 | `/` | 文章列表、热门文章、热门标签 |
| 文章详情 | `/post/:id` | 文章内容、评论、点赞/收藏 |
| 文章搜索 | `/post-search` | 热门文章列表 |
| 全站搜索 | `/search?q=关键词` | 搜索文章、用户 |
| 标签列表 | `/tags` | 所有标签 |
| 标签文章 | `/tag/:id` | 标签下文章 |
| 话题列表 | `/topics` | 所有话题 |
| 话题详情 | `/topic/:id` | 话题下文章 |
| 校友圈 | `/circle` | 动态流 |
| 动态详情 | `/circle/:id` | 动态内容、评论 |
| 用户主页 | `/user/:id` | 用户信息、文章 |

### 需要登录
| 页面 | 路径 | 说明 |
|------|------|------|
| 登录 | `/login` | 用户登录 |
| 注册 | `/register` | 用户注册 |
| 找回密码 | `/password-reset` | 密码重置 |
| 个人主页 | `/profile` | 我的信息 |
| 编辑资料 | `/profile-edit` | 修改资料 |
| 修改密码 | `/password-change` | 修改密码 |
| 我的草稿 | `/drafts` | 草稿管理 |
| 我的收藏 | `/collections` | 收藏列表 |
| 我的关注 | `/following` | 关注列表 |
| 我的粉丝 | `/followers` | 粉丝列表 |
| 消息通知 | `/notifications` | 通知列表 |
| 私信 | `/messages` | 私信聊天 |
| 写文章 | `/post-edit` | 创建/编辑文章 |
| 发布动态 | `/circle/post` | 发布校友圈动态 |
| 举报 | `/report/:type/:id` | 举报内容 |

### 管理后台（需要管理员权限）
| 页面 | 路径 | 说明 |
|------|------|------|
| 仪表盘 | `/admin` | 数据概览 |
| 用户管理 | `/admin/users` | 用户列表 |
| 文章管理 | `/admin/posts` | 文章审核 |
| 举报管理 | `/admin/reports` | 举报处理 |
| 数据统计 | `/admin/statistics` | 详细统计 |

## 配置

### 环境变量

创建 `.env` 文件：

```env
# API 基础路径
VITE_API_BASE=/api
```

### 代理配置

开发服务器已配置代理，将 `/api` 请求转发到后端 `http://localhost:8825`。

## 功能特性

- ✅ 响应式设计（支持移动端）
- ✅ 暗色模式
- ✅ JWT Token 自动刷新
- ✅ Markdown 渲染 + 代码高亮
- ✅ 图片上传
- ✅ 无限滚动
- ✅ 骨架屏加载 (6种样式)
- ✅ Toast 通知 (进度条/悬停暂停/操作按钮)
- ✅ 通用模态框
- ✅ 图片预览灯箱 (键盘导航)
- ✅ 确认对话框 (Promise 风格)
- ✅ 页面过渡动画
- ✅ 空状态提示
- ✅ 前端日志系统

## 开发命令

```bash
# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产版本
npm run preview

# 代码检查
npm run lint
```
