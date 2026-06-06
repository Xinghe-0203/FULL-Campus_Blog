/**
 * 发现路由模块（热搜、话题、标签、搜索）
 */
import type { RouteRecordRaw } from 'vue-router'
import { lazyLoad } from '../helpers'

const discoverRoutes: RouteRecordRaw[] = [
  {
    path: '/search',
    name: 'Search',
    component: lazyLoad(() => import('@/views/search/Search.vue')),
    meta: { title: '搜索' }
  },
  {
    path: '/trending',
    name: 'Trending',
    component: lazyLoad(() => import('@/views/trending/TrendingPage.vue')),
    meta: { title: '热搜' }
  },
  {
    path: '/topic/:id',
    name: 'TopicDetail',
    component: lazyLoad(() => import('@/views/topic/TopicDetail.vue')),
    meta: { title: '话题详情' }
  },
  {
    path: '/tag/:id',
    name: 'TagDetail',
    component: lazyLoad(() => import('@/views/tag/TagDetail.vue')),
    meta: { title: '标签详情' }
  },
  {
    path: '/report/:type/:id',
    name: 'Report',
    component: lazyLoad(() => import('@/views/common/Report.vue')),
    meta: { title: '举报', requiresAuth: true }
  }
]

export default discoverRoutes
