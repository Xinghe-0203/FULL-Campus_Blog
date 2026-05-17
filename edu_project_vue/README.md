# 校园博客论坛 - Vue 3 前端

> Vue 3 前端项目，为 Campus Blog 校园博客论坛系统提供用户界面。

> **当前版本**: v2.0

## 项目概述

本项目是校园博客论坛系统的前端部分，基于 **Vue 3 + Vite** 构建，提供完整的用户交互界面，包括文章浏览、发布、评论、用户管理、校友圈、管理后台等功能模块。v2.0 版本引入了全新的 **Rainy Glassmorphism** 设计系统，带来玻璃拟态、水滴、涟漪、光泽等视觉效果。

---

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.4.21 | 渐进式 JavaScript 框架 (Composition API) |
| Vite | 5.2.0 | 下一代前端构建工具 |
| Vue Router | 4.3.0 | 官方路由管理器 |
| Pinia | 2.1.7 | 新一代 Vue 状态管理 |
| Axios | 1.7.4 | HTTP 客户端 |
| Marked | 12.0.1 | Markdown 解析器 |
| DOMPurify | 3.0.9 | HTML 净化器 (XSS 防护) |
| ESLint | 8.57.0 | 代码检查工具 |
| eslint-plugin-vue | 9.23.0 | Vue 代码规范插件 |

---

## 快速开始

### 环境要求

- **Node.js** >= 18
- **npm** >= 9 (或 yarn/pnpm)

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:3000

开发服务器会自动将 `/api` 请求代理到后端 `http://localhost:8825`。

### 构建生产版本

```bash
npm run build
```

构建产物输出到 `dist/` 目录。

### 预览生产构建

```bash
npm run preview
```

### 代码检查

```bash
npm run lint
```

---

## 项目结构

```
edu_project_vue/
├── public/                    # 静态资源
│   └── favicon.svg           # 网站图标
├── src/                       # 源码目录
│   ├── api/                   # API 接口模块 (17个)
│   │   ├── index.js          # Axios 实例配置 (拦截器/Token刷新)
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
│   │   ├── media.js          # 媒体/上传 API
│   │   ├── report.js         # 举报 API
│   │   ├── share.js          # 分享 API
│   │   └── admin.js          # 管理员 API
│   ├── components/            # 可复用组件 (11个)
│   │   ├── common/           # 通用组件 (8个)
│   │   │   ├── BackToTop.vue    # 返回顶部按钮
│   │   │   ├── EmptyState.vue   # 空状态提示
│   │   │   ├── FileUploader.vue # 文件上传组件
│   │   │   ├── ImagePreview.vue # 图片预览灯箱
│   │   │   ├── Modal.vue        # 通用模态框
│   │   │   ├── PostCard.vue     # 文章卡片
│   │   │   ├── Skeleton.vue     # 骨架屏 (6种样式)
│   │   │   └── Toast.vue        # Toast 通知 (进度条/悬停暂停)
│   │   └── layout/           # 布局组件 (3个)
│   │       ├── Footer.vue       # 页脚
│   │       ├── Navbar.vue       # 顶部导航栏
│   │       └── PageTransition.vue # 页面过渡动画
│   ├── composables/           # 组合式函数
│   │   └── useConfirm.js     # 确认对话框 (Promise 风格)
│   ├── router/                # 路由配置
│   │   └── index.js          # 路由定义 + 导航守卫
│   ├── stores/                # Pinia 状态管理 (3个)
│   │   ├── user.js           # 用户状态 (登录/Token/信息)
│   │   ├── theme.js          # 主题状态 (亮色/暗色)
│   │   └── app.js            # 应用状态 (全局配置)
│   ├── styles/                # 样式文件
│   │   └── main.css          # 主样式 (v2.0 玻璃拟态设计系统)
│   ├── utils/                 # 工具函数
│   │   ├── index.js          # 通用工具函数
│   │   └── logger.js         # 前端日志系统
│   ├── views/                 # 页面组件 (30个)
│   │   ├── Home.vue          # 首页
│   │   ├── auth/             # 认证模块 (3个)
│   │   │   ├── Login.vue
│   │   │   ├── Register.vue
│   │   │   └── PasswordReset.vue
│   │   ├── post/             # 文章模块 (3个)
│   │   │   ├── PostDetail.vue
│   │   │   ├── PostEdit.vue
│   │   │   └── PostSearch.vue
│   │   ├── user/             # 用户模块 (10个)
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
│   │   ├── circle/           # 校友圈模块 (3个)
│   │   │   ├── Circle.vue
│   │   │   ├── CircleDetail.vue
│   │   │   └── CirclePost.vue
│   │   ├── search/           # 搜索模块 (1个)
│   │   │   └── Search.vue
│   │   ├── trending/         # 热门模块 (1个)
│   │   │   └── Trending.vue
│   │   ├── admin/            # 管理后台 (4个)
│   │   │   ├── Dashboard.vue
│   │   │   ├── Users.vue
│   │   │   ├── Posts.vue
│   │   │   ├── Reports.vue
│   │   │   └── Statistics.vue
│   │   └── common/           # 公共页面 (2个)
│   │       ├── NotFound.vue
│   │       └── Report.vue
│   ├── App.vue                # 根组件
│   └── main.js                # 应用入口
├── index.html                 # HTML 入口
├── package.json               # npm 依赖配置
├── vite.config.js             # Vite 构建配置
└── .eslintrc.cjs              # ESLint 配置
```

---

## 页面列表 (30个)

### 首页

| 页面 | 路由路径 | 说明 |
|------|----------|------|
| 首页 | `/` | 文章列表流、热门文章侧栏、热门标签 |

### 认证模块 (3个)

| 页面 | 路由路径 | 说明 |
|------|----------|------|
| 登录 | `/login` | 用户登录 (账号/密码) |
| 注册 | `/register` | 用户注册 (账号/邮箱/密码) |
| 找回密码 | `/password-reset` | 通过邮箱重置密码 |

### 文章模块 (3个)

| 页面 | 路由路径 | 说明 |
|------|----------|------|
| 文章详情 | `/post/:id` | 文章内容、Markdown 渲染、评论、点赞/收藏 |
| 写文章 | `/post-edit` | 创建/编辑文章 (Markdown 编辑器) |
| 热门文章 | `/post-search` | 热门文章列表 |

### 用户模块 (10个)

| 页面 | 路由路径 | 说明 |
|------|----------|------|
| 个人主页 | `/profile` | 当前用户信息、统计、文章列表 |
| 编辑资料 | `/profile-edit` | 修改头像、昵称、简介等 |
| 他人主页 | `/user/:id` | 查看其他用户信息和文章 |
| 修改密码 | `/password-change` | 修改当前用户密码 |
| 我的草稿 | `/drafts` | 草稿箱管理 |
| 我的收藏 | `/collections` | 收藏的文章列表 |
| 我的关注 | `/following` | 关注的用户列表 |
| 我的粉丝 | `/followers` | 粉丝用户列表 |
| 我的举报 | `/my-reports` | 举报记录与状态 |
| 消息通知 | `/notifications` | 系统通知、互动通知 |
| 私信聊天 | `/messages` | 私信列表与聊天界面 |

### 校友圈模块 (3个)

| 页面 | 路由路径 | 说明 |
|------|----------|------|
| 校友圈 | `/circle` | 动态流、@提及、位置、标签 |
| 动态详情 | `/circle/:id` | 动态内容、评论互动 |
| 发布动态 | `/circle/post` | 发布新动态 |

### 搜索与热门 (2个)

| 页面 | 路由路径 | 说明 |
|------|----------|------|
| 全站搜索 | `/search?q=关键词` | 搜索文章、用户、标签 |
| 热门排行 | `/trending` | 热门内容排行 |

### 标签与话题

| 页面 | 路由路径 | 说明 |
|------|----------|------|
| 标签列表 | `/tags` | 所有标签云 |
| 标签文章 | `/tag/:id` | 标签下文章列表 |
| 话题列表 | `/topics` | 所有话题 |
| 话题详情 | `/topic/:id` | 话题下文章列表 |

### 管理后台 (5个，需管理员权限)

| 页面 | 路由路径 | 说明 |
|------|----------|------|
| 仪表盘 | `/admin` | 数据概览、快捷操作 |
| 用户管理 | `/admin/users` | 用户列表、封禁/解封 |
| 文章管理 | `/admin/posts` | 文章审核、删除 |
| 举报管理 | `/admin/reports` | 举报处理 |
| 数据统计 | `/admin/statistics` | 详细数据统计图表 |

### 公共页面 (2个)

| 页面 | 路由路径 | 说明 |
|------|----------|------|
| 404 | `/404` 或任意未匹配路径 | 页面不存在 |
| 举报 | `/report/:type/:id` | 举报内容 (文章/评论/动态) |

---

## 可复用组件 (11个)

### 通用组件 (8个)

| 组件 | 文件 | 说明 |
|------|------|------|
| BackToTop | `BackToTop.vue` | 滚动返回顶部按钮，带平滑动画 |
| EmptyState | `EmptyState.vue` | 空状态占位提示，支持自定义图标和文字 |
| FileUploader | `FileUploader.vue` | 文件上传组件，支持拖拽、预览、进度 |
| ImagePreview | `ImagePreview.vue` | 图片灯箱预览，支持键盘导航和缩放 |
| Modal | `Modal.vue` | 通用模态框，支持自定义内容和按钮 |
| PostCard | `PostCard.vue` | 文章卡片组件，展示标题、摘要、作者、标签 |
| Skeleton | `Skeleton.vue` | 骨架屏加载组件，6种预设样式 |
| Toast | `Toast.vue` | Toast 通知组件，支持进度条、悬停暂停、操作按钮 |

### 布局组件 (3个)

| 组件 | 文件 | 说明 |
|------|------|------|
| Footer | `Footer.vue` | 全局页脚，版权信息和链接 |
| Navbar | `Navbar.vue` | 顶部导航栏，Logo、菜单、用户头像、主题切换 |
| PageTransition | `PageTransition.vue` | 页面切换过渡动画 |

---

## API 模块 (17个)

所有 API 调用统一通过 `src/api/` 目录管理，基于 Axios 封装。

| 模块 | 文件 | 主要功能 |
|------|------|----------|
| Axios 实例 | `index.js` | 基础配置、请求/响应拦截器、Token 自动刷新、401 处理 |
| 用户 | `user.js` | 登录、注册、密码重置、用户信息、头像上传 |
| 文章 | `post.js` | 文章 CRUD、列表、详情、草稿 |
| 评论 | `comment.js` | 评论 CRUD、回复、列表 |
| 点赞 | `like.js` | 点赞/取消、点赞状态 |
| 收藏 | `collect.js` | 收藏/取消、收藏列表 |
| 关注 | `follow.js` | 关注/取消、关注列表、粉丝列表 |
| 通知 | `notification.js` | 通知列表、标记已读、未读数 |
| 私信 | `message.js` | 私信列表、发送、会话管理 |
| 校友圈 | `circle.js` | 动态 CRUD、评论、@提及 |
| 标签 | `tag.js` | 标签列表、标签详情 |
| 话题 | `topic.js` | 话题列表、话题详情 |
| 热门 | `trending.js` | 热门内容排行 |
| 媒体 | `media.js` | 文件上传、图片处理 |
| 举报 | `report.js` | 提交举报、举报列表 |
| 分享 | `share.js` | 分享统计、分享链接 |
| 管理员 | `admin.js` | 用户管理、文章审核、举报处理、统计数据 |

---

## v2.0 新特性

### Rainy Glassmorphism 设计系统

v2.0 引入了全新的 **Rainy Glassmorphism** 玻璃拟态设计系统，模拟雨天玻璃的视觉效果：

- **玻璃拟态背景**: 半透明磨砂玻璃效果， backdrop-filter blur
- **水滴效果**: 随机分布的水滴装饰元素
- **涟漪动画**: 交互时的水波纹扩散效果
- **光泽动画**: 玻璃表面的光线扫过效果
- **湿润发光**: 边框和阴影模拟湿润玻璃的发光感

### 视觉增强

| 特性 | 说明 |
|------|------|
| 增强模糊 | backdrop-filter blur 提升至 16-24px |
| 湿润发光阴影 | 多层 box-shadow 模拟水光反射 |
| 湿润边框 | 半透明渐变边框，模拟水珠边缘 |
| 页面宽度 | 最大宽度增加至 1400px |
| 消息布局 | 修复私信聊天界面布局问题 |

### 校友圈增强

- **@提及**: 支持在动态中 @ 其他用户
- **位置标签**: 动态可附加位置信息
- **话题标签**: 动态支持 #话题 标签
- **开关控制**: 新增可见性、评论权限等开关

---

## 设计系统

### CSS 变量 (主题色)

```css
:root {
  /* 主色调 */
  --primary: #4a90d9;
  --primary-light: #6ba5e7;
  --primary-dark: #357abd;

  /* 玻璃拟态 */
  --glass-bg: rgba(255, 255, 255, 0.15);
  --glass-border: rgba(255, 255, 255, 0.25);
  --glass-blur: 20px;
  --glass-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);

  /* 湿润发光 */
  --wet-glow: 0 0 15px rgba(74, 144, 217, 0.3);
  --wet-border: 1px solid rgba(255, 255, 255, 0.3);

  /* 文字颜色 */
  --text-primary: #1a1a2e;
  --text-secondary: #4a4a6a;
  --text-muted: #8a8aaa;

  /* 背景 */
  --bg-base: #f0f4f8;
  --bg-card: rgba(255, 255, 255, 0.6);

  /* 页面最大宽度 */
  --page-max-width: 1400px;
}

/* 暗色模式 */
[data-theme="dark"] {
  --glass-bg: rgba(0, 0, 0, 0.3);
  --glass-border: rgba(255, 255, 255, 0.1);
  --text-primary: #e8e8f0;
  --text-secondary: #a8a8c0;
  --bg-base: #0d0d1a;
  --bg-card: rgba(20, 20, 40, 0.6);
}
```

### 玻璃效果工具类

| 类名 | 效果 |
|------|------|
| `.glass` | 基础玻璃拟态效果 (blur 20px) |
| `.glass-rain` | 雨天玻璃效果 (blur 24px + 水滴) |
| `.water-drops` | 水滴装饰元素 |
| `.glass-shine` | 光泽扫过动画 |
| `.ripple` | 涟漪点击动画 |
| `.wet-glow` | 湿润发光阴影 |

### 动画工具类

| 类名 | 效果 |
|------|------|
| `.fade-in` | 淡入动画 |
| `.slide-up` | 从下滑入 |
| `.scale-in` | 缩放进入 |
| `.shine-sweep` | 光泽扫过 |
| `.drop-fall` | 水滴下落 |
| `.ripple-expand` | 涟漪扩散 |

### 暗色模式

通过 `theme` store 切换，在 `<html>` 上设置 `data-theme="dark"` 属性。所有玻璃效果自动适配暗色主题。

```js
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()
themeStore.toggleTheme() // 切换亮/暗
```

---

## 状态管理 (Pinia)

### user store (`stores/user.js`)

管理用户认证状态和信息。

```js
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 状态
userStore.token          // JWT access token
userStore.refreshToken   // JWT refresh token
userStore.user           // 用户信息对象
userStore.isLoggedIn     // 是否已登录

// 方法
userStore.login(credentials)      // 登录
userStore.logout()                // 登出 (清空 token 和用户信息)
userStore.fetchUser()             // 获取当前用户信息
userStore.updateProfile(data)     // 更新用户资料
```

### theme store (`stores/theme.js`)

管理主题切换状态。

```js
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()

// 状态
themeStore.isDark        // 是否暗色模式
themeStore.theme         // 'light' | 'dark'

// 方法
themeStore.toggleTheme() // 切换主题
themeStore.setTheme('dark') // 设置指定主题
```

### app store (`stores/app.js`)

管理应用全局配置。

```js
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

// 状态
appStore.loading         // 全局加载状态
appStore.sidebarOpen     // 侧边栏开关
appStore.config          // 应用配置

// 方法
appStore.setLoading(true)
appStore.toggleSidebar()
```

---

## 路由 (Vue Router)

### 路由配置

路由定义在 `src/router/index.js`，使用 Vue Router 4.3。

```js
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', component: () => import('@/views/Home.vue') },
  { path: '/login', component: () => import('@/views/auth/Login.vue') },
  { path: '/post/:id', component: () => import('@/views/post/PostDetail.vue') },
  // ... 更多路由
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    return savedPosition || { top: 0 }
  }
})
```

### 导航守卫

```js
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 需要登录的页面
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  // 需要管理员权限的页面
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    next({ path: '/' })
    return
  }

  // 已登录用户访问登录/注册页，重定向到首页
  if (to.meta.guestOnly && userStore.isLoggedIn) {
    next({ path: '/' })
    return
  }

  next()
})
```

### 路由元信息

| meta 字段 | 类型 | 说明 |
|-----------|------|------|
| `requiresAuth` | boolean | 需要登录 |
| `requiresAdmin` | boolean | 需要管理员权限 |
| `guestOnly` | boolean | 仅未登录用户可访问 |
| `title` | string | 页面标题 (用于 document.title) |

---

## 安全

### XSS 防护

所有用户生成的 HTML 内容通过 **DOMPurify 3.0.9** 净化：

```js
import DOMPurify from 'dompurify'

const safeHtml = DOMPurify.sanitize(userContent)
```

Markdown 渲染流程：

```js
import { marked } from 'marked'
import DOMPurify from 'dompurify'

const renderMarkdown = (text) => {
  const rawHtml = marked.parse(text)
  return DOMPurify.sanitize(rawHtml)
}
```

### Token 管理

- **Access Token**: 短期有效，存储在内存中
- **Refresh Token**: 长期有效，存储在 localStorage
- **自动刷新**: Axios 响应拦截器检测 401，自动使用 refresh token 获取新 access token
- **登出清理**: 登出时清空 Pinia user store 和 localStorage

```js
// Axios 响应拦截器 (src/api/index.js)
axios.interceptors.response.use(
  response => response,
  async error => {
    if (error.response?.status === 401) {
      await userStore.refreshToken()
      // 重试原请求
      return axios.request(error.config)
    }
    return Promise.reject(error)
  }
)
```

---

## 构建输出

生产构建 (`npm run build`) 输出到 `dist/` 目录：

| 资源类型 | 预估大小 | 说明 |
|----------|----------|------|
| JS (chunk) | ~150-250 KB (gzip) | 应用代码 + 依赖，代码分割 |
| CSS | ~30-50 KB (gzip) | 样式文件 |
| 静态资源 | 视情况而定 | 图片、字体等 |
| index.html | ~1 KB | 入口 HTML |

构建优化：
- **代码分割**: 按路由懒加载，减少首屏体积
- **Tree shaking**: 移除未使用的代码
- **压缩**: JS/CSS 自动压缩
- **资源内联**: 小资源自动 base64 内联

---

## 开发规范

1. **Vue 3 Composition API**: 所有组件使用 `<script setup>` 语法
2. **组件命名**: PascalCase (如 `PostCard.vue`)
3. **文件命名**: kebab-case (如 `post-card.vue`，但 Vue 组件推荐 PascalCase)
4. **API 调用**: 统一通过 `src/api/` 模块
5. **状态管理**: 使用 Pinia，避免直接操作 localStorage
6. **样式**: 使用 scoped 样式，全局样式写在 `main.css`
7. **路由守卫**: 登录验证在 `beforeEach` 统一处理

---

## 开发命令速查

```bash
# 安装依赖
npm install

# 启动开发服务器 (http://localhost:3000)
npm run dev

# 构建生产版本 (输出到 dist/)
npm run build

# 预览生产构建
npm run preview

# ESLint 代码检查
npm run lint
```

---

## 后端 API 文档

前端通过 `/api` 代理访问后端服务，后端 API 文档：

- **本地**: http://localhost:8825/api/doc.html (Knife4j)
- **源码**: 查看后端项目 `edu_project/`

---

## 许可证

本项目为校园博客论坛系统的一部分。
