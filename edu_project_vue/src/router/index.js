import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useLogger } from '../utils/logger'
import { toast } from '../utils/toast'

const logger = useLogger('Router')

// 路由配置
const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue'),
    meta: { title: '登录', guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/auth/Register.vue'),
    meta: { title: '注册', guest: true }
  },
  {
    path: '/password-reset',
    name: 'PasswordReset',
    component: () => import('../views/auth/PasswordReset.vue'),
    meta: { title: '找回密码', guest: true }
  },
  {
    path: '/post/:id',
    name: 'PostDetail',
    component: () => import('../views/post/PostDetail.vue'),
    meta: { title: '文章详情' }
  },
  {
    path: '/post-edit',
    name: 'PostEdit',
    component: () => import('../views/post/PostEdit.vue'),
    meta: { title: '写文章', requiresAuth: true }
  },
  {
    path: '/post-edit/:id',
    name: 'PostEditId',
    component: () => import('../views/post/PostEdit.vue'),
    meta: { title: '编辑文章', requiresAuth: true }
  },
  {
    path: '/post-search',
    name: 'PostSearch',
    component: () => import('../views/post/PostSearch.vue'),
    meta: { title: '文章搜索' }
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('../views/search/Search.vue'),
    meta: { title: '搜索' }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/user/Profile.vue'),
    meta: { title: '我的主页', requiresAuth: true }
  },
  {
    path: '/profile-edit',
    name: 'ProfileEdit',
    component: () => import('../views/user/ProfileEdit.vue'),
    meta: { title: '编辑资料', requiresAuth: true }
  },
  {
    path: '/user/:id',
    name: 'UserProfile',
    component: () => import('../views/user/UserProfile.vue'),
    meta: { title: '用户主页' }
  },
  {
    path: '/password-change',
    name: 'PasswordChange',
    component: () => import('../views/user/PasswordChange.vue'),
    meta: { title: '修改密码', requiresAuth: true }
  },
  {
    path: '/drafts',
    name: 'Drafts',
    component: () => import('../views/user/Drafts.vue'),
    meta: { title: '我的草稿', requiresAuth: true }
  },
  {
    path: '/collections',
    name: 'Collections',
    component: () => import('../views/user/Collections.vue'),
    meta: { title: '我的收藏', requiresAuth: true }
  },
  {
    path: '/following',
    name: 'Following',
    component: () => import('../views/user/Following.vue'),
    meta: { title: '我的关注', requiresAuth: true }
  },
  {
    path: '/followers',
    name: 'Followers',
    component: () => import('../views/user/Followers.vue'),
    meta: { title: '我的粉丝', requiresAuth: true }
  },
  {
    path: '/my-reports',
    name: 'MyReports',
    component: () => import('../views/user/MyReports.vue'),
    meta: { title: '我的举报', requiresAuth: true }
  },
  {
    path: '/notifications',
    name: 'Notifications',
    component: () => import('../views/user/Notifications.vue'),
    meta: { title: '消息通知', requiresAuth: true }
  },
  {
    path: '/messages',
    name: 'Messages',
    component: () => import('../views/user/Messages.vue'),
    meta: { title: '私信', requiresAuth: true }
  },
  {
    path: '/circle',
    name: 'Circle',
    component: () => import('../views/circle/Circle.vue'),
    meta: { title: '校友圈' }
  },
  {
    path: '/circle/post',
    name: 'CirclePost',
    component: () => import('../views/circle/CirclePost.vue'),
    meta: { title: '发布动态', requiresAuth: true }
  },
  {
    path: '/circle/:id',
    name: 'CircleDetail',
    component: () => import('../views/circle/CircleDetail.vue'),
    meta: { title: '动态详情' }
  },
  {
    path: '/trending',
    name: 'Trending',
    component: () => import('../views/trending/TrendingPage.vue'),
    meta: { title: '热搜' }
  },
  {
    path: '/report/:type/:id',
    name: 'Report',
    component: () => import('../views/common/Report.vue'),
    meta: { title: '举报', requiresAuth: true }
  },
  // 管理员路由
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: () => import('../views/admin/Dashboard.vue'),
    meta: { title: '管理后台', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: () => import('../views/admin/Users.vue'),
    meta: { title: '用户管理', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/posts',
    name: 'AdminPosts',
    component: () => import('../views/admin/Posts.vue'),
    meta: { title: '文章管理', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/reports',
    name: 'AdminReports',
    component: () => import('../views/admin/Reports.vue'),
    meta: { title: '举报管理', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/statistics',
    name: 'AdminStatistics',
    component: () => import('../views/admin/Statistics.vue'),
    meta: { title: '数据统计', requiresAuth: true, requiresAdmin: true }
  },
  // 404页面
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/common/NotFound.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    if (to.hash) {
      return { el: to.hash, behavior: 'smooth' }
    }
    return { top: 0 }
  }
})

// 路由守卫
router.beforeEach((to, from) => {
  const userStore = useUserStore()
  
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 校园博客` : '校园博客'
  
  logger.debug('Navigation', { from: from.path, to: to.path })
  
  // 检查是否需要登录
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    logger.info('Redirecting to login', { returnPath: to.fullPath })
    toast.warning('请先登录')
    return { name: 'Login', query: { redirect: to.fullPath } }
  }
  
  // 检查是否需要管理员权限
  if (to.meta.requiresAdmin) {
    if (!userStore.isLoggedIn) {
      toast.warning('请先登录')
      return { name: 'Login', query: { redirect: to.fullPath } }
    }
    if (!userStore.isAdmin) {
      logger.warn('Admin access denied', { userId: userStore.user?.id })
      toast.error('无管理员权限')
      return { name: 'Home' }
    }
  }
  
  // 已登录用户不能访问登录/注册页面
  if (to.meta.guest && userStore.isLoggedIn) {
    toast.info('您已登录')
    return { name: 'Home' }
  }
})

// 路由错误处理
router.onError((error) => {
  logger.error('Router navigation error', { error: error.message })

  // 处理异步组件加载失败（chunk load errors）
  if (error.name === 'ChunkLoadError' || error.message?.includes('Loading chunk')) {
    toast.error('页面加载失败，请刷新重试')
    router.push({ name: 'Home' })
    return
  }

  // 处理导航失败
  toast.error('页面导航失败，请重试')
})

// 路由后置守卫
router.afterEach((to, from) => {
  logger.debug('Navigation completed', { to: to.path })

  // 检测导航是否失败（from与to相同表示导航被拦截或失败）
  if (from.name && to.path === from.path && to.hash !== from.hash) {
    logger.warn('Navigation aborted or failed', { from: from.fullPath, to: to.fullPath })
  }
})

export default router
