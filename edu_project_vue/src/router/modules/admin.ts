/**
 * 管理后台路由模块
 */
import type { RouteRecordRaw } from 'vue-router'
import { lazyLoad } from '../helpers'

const adminRoutes: RouteRecordRaw[] = [
  {
    path: '/admin',
    component: lazyLoad(() => import('@/views/admin/AdminLayout.vue')),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: lazyLoad(() => import('@/views/admin/Dashboard.vue')),
        meta: { title: '仪表盘' }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: lazyLoad(() => import('@/views/admin/Users.vue')),
        meta: { title: '用户管理' }
      },
      {
        path: 'posts',
        name: 'AdminPosts',
        component: lazyLoad(() => import('@/views/admin/Posts.vue')),
        meta: { title: '文章管理' }
      },
      {
        path: 'circle',
        name: 'AdminCircle',
        component: lazyLoad(() => import('@/views/admin/CircleManagement.vue')),
        meta: { title: '校友圈管理' }
      },
      {
        path: 'tags',
        name: 'AdminTags',
        component: lazyLoad(() => import('@/views/admin/TagsManagement.vue')),
        meta: { title: '标签管理' }
      },
      {
        path: 'topics',
        name: 'AdminTopics',
        component: lazyLoad(() => import('@/views/admin/TopicsManagement.vue')),
        meta: { title: '话题管理' }
      },
      {
        path: 'reports',
        name: 'AdminReports',
        component: lazyLoad(() => import('@/views/admin/Reports.vue')),
        meta: { title: '举报管理' }
      },
      {
        path: 'statistics',
        name: 'AdminStatistics',
        component: lazyLoad(() => import('@/views/admin/Statistics.vue')),
        meta: { title: '数据统计' }
      }
    ]
  }
]

export default adminRoutes
