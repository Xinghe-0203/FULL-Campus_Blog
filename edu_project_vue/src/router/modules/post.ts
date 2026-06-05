/**
 * 文章路由模块
 */
import type { RouteRecordRaw } from 'vue-router'
import { lazyLoad } from '../helpers'

const postRoutes: RouteRecordRaw[] = [
  {
    path: '/post/:id',
    name: 'PostDetail',
    component: lazyLoad(() => import('@/views/post/PostDetail.vue')),
    meta: { title: '文章详情' }
  },
  {
    path: '/post-edit',
    name: 'PostEdit',
    component: lazyLoad(() => import('@/views/post/PostEdit.vue')),
    meta: { title: '写文章', requiresAuth: true }
  },
  {
    path: '/post-edit/:id',
    name: 'PostEditId',
    component: lazyLoad(() => import('@/views/post/PostEdit.vue')),
    meta: { title: '编辑文章', requiresAuth: true }
  },
  {
    path: '/post-search',
    name: 'PostSearch',
    component: lazyLoad(() => import('@/views/post/PostSearch.vue')),
    meta: { title: '文章搜索' }
  }
]

export default postRoutes
