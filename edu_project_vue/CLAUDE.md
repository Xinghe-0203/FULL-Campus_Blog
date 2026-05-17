# CLAUDE.md - 校园博客论坛 Vue 3 前端 (v2.0)

## 项目概述

这是校园博客论坛系统的前端项目，基于 Vue 3 + Vite 构建。当前版本 v2.0。

## 常用命令

```bash
npm install        # 安装依赖
npm run dev        # 启动开发服务器 (端口 3000)
npm run build      # 构建生产版本
npm run preview    # 预览生产版本
npm run lint       # 代码检查
```

## 技术栈

- Vue 3.4.21 (Composition API)
- Vite 5.2.0
- Vue Router 4.3.0
- Pinia 2.1.7
- Axios 1.7.4
- Marked 12.0.1 (Markdown)
- Highlight.js 11.9
- DOMPurify 3.0.9 (XSS 防护)

## 项目结构

```
src/
├── api/            # API 接口 (17个模块)
├── components/     # 组件 (共11个)
│   ├── common/     # 通用组件 (8个)
│   │   ├── BackToTop.vue     # 返回顶部
│   │   ├── EmptyState.vue    # 空状态提示
│   │   ├── FileUploader.vue  # 文件上传
│   │   ├── ImagePreview.vue  # 图片预览灯箱
│   │   ├── Modal.vue         # 通用模态框
│   │   ├── PostCard.vue      # 文章卡片
│   │   ├── Skeleton.vue      # 骨架屏
│   │   └── Toast.vue         # Toast通知
│   └── layout/     # 布局组件 (3个)
│       ├── Footer.vue        # 页脚
│       ├── Navbar.vue        # 导航栏
│       └── PageTransition.vue # 页面过渡动画
├── composables/    # 组合式函数
│   └── useConfirm.js         # 确认对话框
├── router/         # 路由配置
├── stores/         # Pinia 状态管理 (3个store)
├── styles/         # 样式文件
├── utils/          # 工具函数
└── views/          # 页面组件 (30个)
```

## v2.0 新特性

- Rainy glassmorphism UI 设计
- Circle 功能增强
- 性能优化 (懒加载、虚拟滚动)

## 设计系统

- CSS 变量定义在 `main.css`
- 毛玻璃效果类: `.glass`, `.glass-card`
- 动画工具类: `.fade-in`, `.slide-up`, `.pulse`

## 开发规范

1. 使用 Vue 3 Composition API (`<script setup>`)
2. 组件命名使用 PascalCase
3. API 调用统一通过 `src/api/` 目录
4. 状态管理使用 Pinia
5. 路由守卫处理登录验证

## API 配置

开发环境自动代理 `/api` 到 `http://localhost:8825`

## 注意事项

- 修改代码前先阅读相关文件
- 遵循现有的代码风格
- 每次修改后更新文档
