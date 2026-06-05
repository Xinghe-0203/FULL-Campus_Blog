/**
 * 首页路由模块
 */
import type { RouteRecordRaw } from 'vue-router'
import { lazyLoad } from '../helpers'

const homeRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: lazyLoad(() => import('@/views/Home.vue')),
    meta: { title: '首页' }
  }
]

export default homeRoutes
