/**
 * 认证路由模块
 */
import type { RouteRecordRaw } from 'vue-router'
import { lazyLoad } from '../helpers'

const authRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: lazyLoad(() => import('@/views/auth/Login.vue')),
    meta: { title: '登录', guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: lazyLoad(() => import('@/views/auth/Register.vue')),
    meta: { title: '注册', guest: true }
  },
  {
    path: '/password-reset',
    name: 'PasswordReset',
    component: lazyLoad(() => import('@/views/auth/PasswordReset.vue')),
    meta: { title: '找回密码', guest: true }
  }
]

export default authRoutes
