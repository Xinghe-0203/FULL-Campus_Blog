import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { userApi } from '../api/user'
import { notificationApi } from '../api/notification'
import { messageApi } from '../api/message'
import { useLogger } from '../utils/logger'

const logger = useLogger('UserStore')

const STORAGE_KEY_PREFIX = 'edu_'

/**
 * 用户认证状态管理 Store
 *
 * @typedef {Object} UserState
 * @property {import('vue').Ref<Object|null>} user - 当前用户信息
 * @property {import('vue').Ref<string|null>} token - JWT Token
 * @property {import('vue').Ref<string|null>} refreshToken - 刷新 Token
 * @property {import('vue').Ref<number>} unreadNotifications - 未读通知数
 * @property {import('vue').Ref<number>} unreadMessages - 未读私信数
 * @property {import('vue').Ref<boolean>} loading - 是否正在加载
 * @property {import('vue').Ref<boolean>} remember - 是否记住登录
 * @property {import('vue').ComputedRef<boolean>} isLoggedIn - 是否已登录
 * @property {import('vue').ComputedRef<boolean>} isAdmin - 是否为管理员
 * @property {import('vue').ComputedRef<number|undefined>} userId - 用户ID
 * @property {import('vue').ComputedRef<string|undefined>} username - 用户名
 * @property {import('vue').ComputedRef<string|undefined>} avatar - 头像 URL
 * @property {import('vue').ComputedRef<string>} nickname - 昵称
 * @property {import('vue').ComputedRef<number>} totalUnread - 总未读数
 */

export const useUserStore = defineStore('user', () => {
  // 状态
  const user = ref(null)
  const token = ref(null)
  const refreshToken = ref(null)
  const unreadNotifications = ref(0)
  const unreadMessages = ref(0)
  const loading = ref(false)
  const remember = ref(false)
  try {
    remember.value = localStorage.getItem(STORAGE_KEY_PREFIX + 'remember') === 'true'
  } catch {}

  // 计算属性
  const isLoggedIn = computed(() => !!token.value && !!user.value)
  const isAdmin = computed(() => user.value?.role === 'admin')
  const userId = computed(() => user.value?.id)
  const username = computed(() => user.value?.username)
  const avatar = computed(() => user.value?.avatar)
  const nickname = computed(() => user.value?.nickname || user.value?.username)
  const totalUnread = computed(() => unreadNotifications.value + unreadMessages.value)
  
  /**
   * 编码数据为 Base64 字符串
   * @param {Object} data - 要编码的数据
   * @returns {string|null}
   */
  function encodeData(data) {
    if (!data) return null
    try {
      return btoa(unescape(encodeURIComponent(JSON.stringify(data))))
    } catch {
      return null
    }
  }
  
  /**
   * 解码 Base64 字符串为对象
   * @param {string|null} encoded - 编码后的字符串
   * @returns {Object|null}
   */
  function decodeData(encoded) {
    if (!encoded) return null
    try {
      return JSON.parse(decodeURIComponent(escape(atob(encoded))))
    } catch {
      return null
    }
  }

  /**
   * 从本地存储恢复用户状态
   * @returns {void}
   */
  function restoreFromStorage() {
    try {
      const savedToken = localStorage.getItem(STORAGE_KEY_PREFIX + 'token') || sessionStorage.getItem(STORAGE_KEY_PREFIX + 'token')
      const savedRefreshToken = localStorage.getItem(STORAGE_KEY_PREFIX + 'refreshToken') || sessionStorage.getItem(STORAGE_KEY_PREFIX + 'refreshToken')
      const savedUser = localStorage.getItem(STORAGE_KEY_PREFIX + 'user') || sessionStorage.getItem(STORAGE_KEY_PREFIX + 'user')
      remember.value = localStorage.getItem(STORAGE_KEY_PREFIX + 'remember') === 'true'

      const decodedToken = decodeData(savedToken)
      const decodedUser = decodeData(savedUser)

      if (decodedToken && decodedUser) {
        token.value = decodedToken
        refreshToken.value = decodeData(savedRefreshToken)
        user.value = decodedUser
        logger.info('User state restored from storage', { userId: user.value?.id })

        fetchUnreadCounts()
      } else {
        // Token或User解码失败，清除存储
        logger.warn('Failed to decode stored auth data, clearing storage')
        clearStorage()
      }
    } catch (error) {
      logger.error('Failed to restore user state', { error: error.message })
      clearStorage()
    }
  }

  // 保存到本地存储
  function saveToStorage() {
    try {
      const storage = remember.value ? localStorage : sessionStorage
      if (token.value) {
        storage.setItem(STORAGE_KEY_PREFIX + 'token', encodeData(token.value))
      }
      if (refreshToken.value) {
        storage.setItem(STORAGE_KEY_PREFIX + 'refreshToken', encodeData(refreshToken.value))
      }
      if (user.value) {
        storage.setItem(STORAGE_KEY_PREFIX + 'user', encodeData(user.value))
      }
    } catch (error) {
      logger.warn('Failed to save to storage', { error: error.message })
    }
  }

  /**
   * 重置所有状态并清除存储
   * @returns {void}
   */
  function resetState() {
    user.value = null
    token.value = null
    refreshToken.value = null
    unreadNotifications.value = 0
    unreadMessages.value = 0
    clearStorage()
  }

  /**
   * 清除所有本地存储中的认证数据
   * @returns {void}
   */
  function clearStorage() {
    try {
      localStorage.removeItem(STORAGE_KEY_PREFIX + 'token')
      localStorage.removeItem(STORAGE_KEY_PREFIX + 'refreshToken')
      localStorage.removeItem(STORAGE_KEY_PREFIX + 'user')
      localStorage.removeItem(STORAGE_KEY_PREFIX + 'remember')
      localStorage.removeItem(STORAGE_KEY_PREFIX + 'token_fingerprint')
      localStorage.removeItem(STORAGE_KEY_PREFIX + 'xsrf_token')
      sessionStorage.removeItem(STORAGE_KEY_PREFIX + 'token')
      sessionStorage.removeItem(STORAGE_KEY_PREFIX + 'refreshToken')
      sessionStorage.removeItem(STORAGE_KEY_PREFIX + 'user')
    } catch (error) {
      logger.warn('Failed to clear storage', { error: error.message })
    }
  }

  /**
   * 验证Token是否有效（检查JWT过期时间，避免每次验证都刷新Token）
   * 注意：不在此处调用 resetState()，让 api/index.js 的响应拦截器处理刷新
   * @returns {Promise<boolean>}
   */
  async function validateToken() {
    if (!token.value) {
      return false
    }

    try {
      const parts = token.value.split('.')
      if (parts.length === 3) {
        const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')))
        if (payload.exp && payload.exp * 1000 < Date.now()) {
          logger.warn('Token expired', { exp: new Date(payload.exp * 1000).toISOString() })
          return false
        }
        // 检查token是否即将过期（5分钟内过期）
        if (payload.exp && payload.exp * 1000 - Date.now() < 5 * 60 * 1000) {
          logger.info('Token expiring soon, will refresh on next request')
        }
        return true
      }
      return true
    } catch (error) {
      logger.warn('Token validation failed', { error: error.message })
      return false
    }
  }

  /**
   * 检查Token是否可能已被窃取（异常检测）
   * @returns {boolean}
   */
  function detectTokenTheft() {
    try {
      const savedToken = localStorage.getItem(STORAGE_KEY_PREFIX + 'token')
      const currentToken = token.value
      if (savedToken && currentToken && savedToken !== encodeData(currentToken)) {
        logger.error('Token mismatch detected - possible theft attempt', {
          savedLength: savedToken.length,
          currentLength: encodeData(currentToken)?.length
        })
        return true
      }
      return false
    } catch {
      return false
    }
  }

  /**
   * 用户登录
   * @param {{username: string, password: string, remember?: boolean}} credentials - 登录凭证
   * @returns {Promise<{success: boolean, message?: string}>}
   */
  async function login(credentials) {
    loading.value = true
    try {
      const response = await userApi.login(credentials)
      // Handle both flat and nested response structures
      const loginData = response.data || response

      let { token: newToken, refreshToken: newRefreshToken, id, username, nickname, avatar, role } = loginData

      // Fallback to nested data if backend returns { data: { user, token, refreshToken } }
      if (!id && loginData.data) {
        const nestedData = loginData.data
        newToken = nestedData.token
        newRefreshToken = nestedData.refreshToken
        id = nestedData.id
        username = nestedData.username
        nickname = nestedData.nickname
        avatar = nestedData.avatar
        role = nestedData.role
      }

      // Validate required fields
      if (!newToken || !id) {
        logger.error('Invalid login response structure', { loginData })
        return { success: false, message: '登录失败，服务器响应异常' }
      }

      token.value = newToken
      refreshToken.value = newRefreshToken
    user.value = {
        id,
        username: username || loginData.username || loginData.data?.username,
        nickname: nickname || loginData.nickname || loginData.data?.nickname,
        avatar: avatar || loginData.avatar || loginData.data?.avatar,
        email: loginData.email || loginData.data?.email,
        role: role || loginData.role || loginData.data?.role
    }

      if (credentials.remember) {
        try {
          localStorage.setItem(STORAGE_KEY_PREFIX + 'remember', 'true')
        } catch (e) { logger.warn('Failed to save remember preference', { error: e.message }) }
        remember.value = true
      } else {
        try {
          localStorage.removeItem(STORAGE_KEY_PREFIX + 'remember')
        } catch (e) { logger.warn('Failed to remove remember preference', { error: e.message }) }
        remember.value = false
      }

      saveToStorage()

      logger.info('Login successful', { userId: id, username: user.value.username })

      // 获取未读消息数
      await fetchUnreadCounts()

      return { success: true }
    } catch (error) {
      logger.error('Login failed', { error: error.message })
      return { success: false, message: error.response?.data?.message || '登录失败' }
    } finally {
      loading.value = false
    }
  }

  /**
   * 用户注册
   * @param {{username: string, password: string, email?: string}} data - 注册信息
   * @returns {Promise<{success: boolean, message?: string}>}
   */
  async function register(data) {
    loading.value = true
    try {
      await userApi.register(data)
      logger.info('Registration successful', { username: data.username })
      return { success: true }
    } catch (error) {
      logger.error('Registration failed', { error: error.message })
      return { success: false, message: error.response?.data?.message || '注册失败' }
    } finally {
      loading.value = false
    }
  }

  /**
   * 用户登出
   * @returns {Promise<void>}
   */
  async function logout() {
    const userId = user.value?.id
    try {
      await userApi.logout()
    } catch (e) {
      logger.warn('Backend logout failed', { error: e.message })
    }
    user.value = null
    token.value = null
    refreshToken.value = null
    unreadNotifications.value = 0
    unreadMessages.value = 0
    
    clearStorage()
    logger.info('User logged out', { userId })
  }

  /**
   * 更新Token和刷新Token
   * @param {string} newToken - 新的JWT Token
   * @param {string} newRefreshToken - 新的刷新Token
   * @returns {void}
   */
  function updateTokens(newToken, newRefreshToken) {
    token.value = newToken
    refreshToken.value = newRefreshToken
    saveToStorage()
  }

  /**
   * 更新用户信息
   * @param {Object} userData - 用户信息更新数据
   * @returns {void}
   */
  function updateUserInfo(userData) {
    user.value = { ...user.value, ...userData }
    saveToStorage()
    logger.info('User info updated', { userId: user.value?.id })
  }

  /**
   * 更新用户头像
   * @param {string} avatarUrl - 新头像URL
   * @returns {void}
   */
  function updateAvatar(avatarUrl) {
    if (user.value) {
      user.value.avatar = avatarUrl
      saveToStorage()
    }
  }

  /**
   * 更新用户封面图
   * @param {string} coverImageUrl - 新封面图URL
   * @returns {void}
   */
  function updateCoverImage(coverImageUrl) {
    if (user.value) {
      user.value.coverImage = coverImageUrl
      saveToStorage()
    }
  }

  /**
   * 获取未读通知和私信数量
   * @returns {Promise<void>}
   */
  async function fetchUnreadCounts() {
    if (!isLoggedIn.value) return
    
    try {
      const [notifResult, msgResult] = await Promise.allSettled([
        notificationApi.getUnreadCount(),
        messageApi.getUnreadCount()
      ])
      
      unreadNotifications.value = (notifResult.status === 'fulfilled' ? notifResult.value.data : 0) || 0
      unreadMessages.value = (msgResult.status === 'fulfilled' ? msgResult.value.data : 0) || 0
    } catch (error) {
      logger.warn('Failed to fetch unread counts', { error: error.message })
    }
  }

  /**
   * 更新个人资料
   * @param {Object} profileData - 个人资料更新数据
   * @returns {Promise<{success: boolean, message?: string}>}
   */
  async function updateProfile(profileData) {
    loading.value = true
    try {
      await userApi.updateProfile(profileData)
      updateUserInfo(profileData)
      return { success: true }
    } catch (error) {
      logger.error('Failed to update profile', { error: error.message })
      return { success: false, message: error.response?.data?.message || '更新失败' }
    } finally {
      loading.value = false
    }
  }

  /**
   * 修改密码
   * @param {{oldPassword: string, newPassword: string}} passwordData - 密码数据
   * @returns {Promise<{success: boolean, message?: string}>}
   */
  async function changePassword(passwordData) {
    loading.value = true
    try {
      await userApi.changePassword(passwordData)
      return { success: true }
    } catch (error) {
      logger.error('Failed to change password', { error: error.message })
      return { success: false, message: error.response?.data?.message || '修改密码失败' }
    } finally {
      loading.value = false
    }
  }

  return {
    // 状态
    user,
    token,
    refreshToken,
    unreadNotifications,
    unreadMessages,
    loading,

    // 计算属性
    isLoggedIn,
    isAdmin,
    userId,
    username,
    avatar,
    nickname,
    totalUnread,
    remember,

    // 方法
    restoreFromStorage,
    validateToken,
    login,
    register,
    logout,
    updateTokens,
    updateUserInfo,
    updateAvatar,
    updateCoverImage,
    fetchUnreadCounts,
    updateProfile,
    changePassword
  }
})
