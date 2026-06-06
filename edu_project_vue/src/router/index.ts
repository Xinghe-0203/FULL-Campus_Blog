/**
 * 路由配置 - 模块化入口
 */
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { setupGuards } from './guards'

import authRoutes from './modules/auth'
import homeRoutes from './modules/home'
import postRoutes from './modules/post'
import circleRoutes from './modules/circle'
import userRoutes from './modules/user'
import discoverRoutes from './modules/discover'
import adminRoutes from './modules/admin'

export { lazyLoad } from './helpers'

const routes: RouteRecordRaw[] = [
  ...homeRoutes,
  ...authRoutes,
  ...postRoutes,
  ...circleRoutes,
  ...userRoutes,
  ...discoverRoutes,
  ...adminRoutes,
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/common/NotFound.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, _from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    if (to.hash) {
      return { el: to.hash, behavior: 'smooth' }
    }
    return { top: 0 }
  }
})

setupGuards(router)

export default router
