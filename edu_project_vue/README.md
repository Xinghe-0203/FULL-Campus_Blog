# Campus Blog - Frontend

Vue 3 frontend for the Campus Blog platform with a Rainy Glassmorphism design system.

**Version**: v2.0 | **Vue**: 3.4.21 | **Vite**: 5.2.0

## Tech Stack

| Component | Version | Purpose |
|-----------|---------|---------|
| Vue | 3.4.21 | UI framework (Composition API) |
| Vite | 5.2.0 | Build tool |
| Vue Router | 4.3.0 | Routing |
| Pinia | 2.1.7 | State management |
| Axios | 1.7.4 | HTTP client |
| Marked | 12.0.1 | Markdown rendering |
| DOMPurify | 3.0.9 | XSS protection |
| Highlight.js | 11.9 | Code highlighting |
| TypeScript | 5.x | Type safety |

## Quick Start

```bash
npm install         # Install dependencies
npm run dev         # Dev server (http://localhost:3000)
npm run build       # Production build (→ dist/)
npm run preview     # Preview production build
npm run lint        # ESLint check
```

Dev server proxies `/api` requests to `http://localhost:8825`.

## Project Structure

```
src/
├── api/                # API modules (17)
│   ├── index.ts        # Axios instance, interceptors, token refresh
│   ├── user.ts         # Auth, profile, avatar
│   ├── post.ts         # Article CRUD, drafts
│   ├── comment.ts      # Comments
│   ├── like.ts         # Likes
│   ├── collect.ts      # Collections
│   ├── follow.ts       # Follow/followers
│   ├── notification.ts # Notifications
│   ├── message.ts      # Private messages
│   ├── circle.ts       # Campus circle
│   ├── tag.ts          # Tags
│   ├── topic.ts        # Topics
│   ├── trending.ts     # Trending
│   ├── media.ts        # File upload
│   ├── report.ts       # Reports
│   ├── share.ts        # Sharing
│   └── admin.ts        # Admin API
├── components/
│   ├── common/         # 8 reusable components
│   │   ├── BackToTop.vue
│   │   ├── EmptyState.vue
│   │   ├── FileUploader.vue
│   │   ├── ImagePreview.vue
│   │   ├── Modal.vue
│   │   ├── PostCard.vue
│   │   ├── Skeleton.vue
│   │   └── Toast.vue
│   └── layout/         # 3 layout components
│       ├── Footer.vue
│       ├── Navbar.vue
│       └── PageTransition.vue
├── composables/
│   └── useConfirm.ts   # Promise-based confirm dialog
├── router/
│   ├── index.ts        # Router entry
│   ├── guards.ts       # Navigation guards
│   ├── helpers.ts      # Lazy loading helper
│   └── modules/        # Route modules
│       ├── auth.ts     # Login, register, password reset
│       ├── home.ts     # Home page
│       ├── post.ts     # Post detail, edit, search
│       ├── circle.ts   # Campus circle
│       ├── user.ts     # Profile, drafts, collections, messages
│       ├── discover.ts # Search, trending, topics, tags
│       └── admin.ts    # Admin panel (8 child routes)
├── stores/
│   ├── user.ts         # Auth state, token management
│   ├── theme.ts        # Light/dark theme
│   └── app.ts          # Global app state
├── styles/
│   └── main.css        # Global styles, CSS variables
├── types/              # TypeScript type definitions
│   ├── common.ts       # ApiResponse, PaginatedData
│   ├── user.ts         # User types
│   ├── post.ts         # Post types
│   ├── circle.ts       # Circle types
│   └── ...             # Domain-specific types
├── utils/
│   ├── index.ts        # General utilities
│   └── logger.ts       # Frontend logging
├── views/              # 30 page components
│   ├── Home.vue
│   ├── auth/           # Login, Register, PasswordReset
│   ├── post/           # PostDetail, PostEdit, PostSearch
│   ├── circle/         # Circle, CircleDetail, CirclePost
│   ├── user/           # Profile, Drafts, Collections, Messages, etc.
│   ├── admin/          # Dashboard, Users, Posts, Reports, Statistics
│   ├── search/         # Search
│   ├── trending/       # Trending
│   ├── topic/          # TopicDetail
│   ├── tag/            # TagDetail
│   └── common/         # NotFound, Report
├── App.vue
└── main.ts
```

## Pages (30)

| Module | Pages | Routes |
|--------|-------|--------|
| Home | 1 | `/` |
| Auth | 3 | `/login`, `/register`, `/password-reset` |
| Post | 3 | `/post/:id`, `/post-edit`, `/post-search` |
| User | 11 | `/profile`, `/profile-edit`, `/user/:id`, `/drafts`, `/collections`, `/following`, `/followers`, `/notifications`, `/messages`, `/password-change`, `/my-reports` |
| Circle | 3 | `/circle`, `/circle/:id`, `/circle/post` |
| Discover | 4 | `/search`, `/trending`, `/topic/:id`, `/tag/:id` |
| Admin | 5 | `/admin`, `/admin/users`, `/admin/posts`, `/admin/reports`, `/admin/statistics` |
| Common | 2 | `/404`, `/report/:type/:id` |

## State Management (Pinia)

### `user` store

```ts
const userStore = useUserStore()
userStore.token          // JWT access token
userStore.isLoggedIn     // Auth status
userStore.login(credentials)
userStore.logout()
userStore.fetchUser()
```

### `theme` store

```ts
const themeStore = useThemeStore()
themeStore.isDark        // Dark mode status
themeStore.toggleTheme() // Toggle light/dark
```

### `app` store

```ts
const appStore = useAppStore()
appStore.loading         // Global loading state
appStore.toggleSidebar() // Sidebar toggle
```

## Design System

Rainy Glassmorphism UI with glass effects, water drops, ripple animations, and light sweeps.

### CSS Variables

```css
:root {
  --primary: #4a90d9;
  --glass-bg: rgba(255, 255, 255, 0.15);
  --glass-blur: 20px;
  --page-max-width: 1400px;
}
[data-theme="dark"] { /* Dark mode overrides */ }
```

### Utility Classes

| Class | Effect |
|-------|--------|
| `.glass` | Glass morphism (blur 20px) |
| `.glass-rain` | Rain glass (blur 24px + drops) |
| `.water-drops` | Water drop decorations |
| `.glass-shine` | Light sweep animation |
| `.ripple` | Click ripple animation |
| `.wet-glow` | Wet glow shadow |

## Security

- **XSS**: All user HTML sanitized via DOMPurify before rendering
- **Markdown**: `marked.parse()` → `DOMPurify.sanitize()` pipeline
- **Token**: Access token in memory, refresh token in localStorage, auto-refresh on 401

## Router Guards

- `requiresAuth` — Redirects to `/login` if not authenticated
- `requiresAdmin` — Redirects to `/` if not admin
- `guestOnly` — Redirects to `/` if already logged in

## Build Output

| Asset | Size (gzip) |
|-------|-------------|
| JS | ~150-250 KB |
| CSS | ~30-50 KB |

Optimizations: route-based code splitting, tree shaking, auto-compression, small asset inlining.

## Conventions

1. Vue 3 Composition API (`<script setup>`)
2. PascalCase component names
3. API calls via `src/api/` modules
4. State management via Pinia
5. Scoped styles, global styles in `main.css`
