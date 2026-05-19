import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { notificationApi } from '../api/notification'
import { messageApi } from '../api/message'
import { userApi } from '../api/user'
import { useLogger } from '../utils/logger'

const logger = useLogger('UserStore')

const STORAGE_KEY_PREFIX = 'edu_'

// ============================================================
// 组合式函数 - 内部使用
// ============================================================

/**
 * 编码数据为 Base64 字符串
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
 */
function decodeData(encoded) {
  if (!encoded) return null
  try {
    return JSON.parse(decodeURIComponent(escape(atob(encoded))))
  } catch {
    return null
  }
}

// ============================================================
// Auth Store - 认证状态 (登录/登出/Token)
// ============================================================

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref(null)
  const refreshToken = ref(null)
  const user = ref(null)
  const remember = ref(false)
  const loading = ref(false)

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

  // 清除所有本地存储中的认证数据
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

  // 从本地存储恢复用户状态
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
        logger.info('Auth state restored from storage', { userId: user.value?.id })
      } else {
        logger.warn('Failed to decode stored auth data, clearing storage')
        clearStorage()
      }
    } catch (error) {
      logger.error('Failed to restore auth state', { error: error.message })
      clearStorage()
    }
  }

  // 重置所有状态并清除存储
  function resetState() {
    user.value = null
    token.value = null
    refreshToken.value = null
    clearStorage()
  }

  // 验证Token是否有效
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

  // 检查Token是否可能已被窃取
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

  // 用户登录
  async function login(credentials) {
    loading.value = true
    try {
      const response = await userApi.login(credentials)
      const loginData = response.data || response

      let { token: newToken, refreshToken: newRefreshToken, id, username, nickname, avatar, role } = loginData

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

      return { success: true }
    } catch (error) {
      logger.error('Login failed', { error: error.message })
      return { success: false, message: error.response?.data?.message || '登录失败' }
    } finally {
      loading.value = false
    }
  }

  // 用户注册
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

  // 用户登出
  async function logout() {
    const userId = user.value?.id
    try {
      await userApi.logout()
    } catch (e) {
      logger.warn('Backend logout failed', { error: e.message })
    }
    resetState()
    logger.info('User logged out', { userId })
  }

  // 更新Token和刷新Token
  function updateTokens(newToken, newRefreshToken) {
    token.value = newToken
    refreshToken.value = newRefreshToken
    saveToStorage()
  }

  // 更新用户信息
  function updateUserInfo(userData) {
    const allowedFields = ['id', 'username', 'nickname', 'avatar', 'email', 'role',
      'coverImage', 'bio', 'gender', 'birthday', 'phone', 'school', 'major', 'grade',
      'signature', 'status', 'createdAt']
    const filteredData = {}
    for (const key of allowedFields) {
      if (key in userData) {
        filteredData[key] = userData[key]
      }
    }
    user.value = { ...user.value, ...filteredData }
    saveToStorage()
    logger.info('User info updated', { userId: user.value?.id })
  }

  // 更新用户头像
  function updateAvatar(avatarUrl) {
    if (user.value) {
      user.value.avatar = avatarUrl
      saveToStorage()
    }
  }

  // 更新用户封面图
  function updateCoverImage(coverImageUrl) {
    if (user.value) {
      user.value.coverImage = coverImageUrl
      saveToStorage()
    }
  }

  /**
   * 更新个人资料
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

  // 通知未读计数 (Navbar 等直接通过 userStore 访问)
  const unreadNotifications = ref(0)
  const unreadMessages = ref(0)
  const totalUnread = computed(() => unreadNotifications.value + unreadMessages.value)

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

  function incrementNotifications(count = 1) { unreadNotifications.value += count }
  function incrementMessages(count = 1) { unreadMessages.value += count }

  return {
    token,
    refreshToken,
    user,
    remember,
    loading,

    isLoggedIn,
    isAdmin,
    userId,
    username,
    avatar,
    nickname,

    unreadNotifications,
    unreadMessages,
    totalUnread,

    saveToStorage,
    clearStorage,
    restoreFromStorage,
    resetState,
    validateToken,
    detectTokenTheft,
    login,
    register,
    logout,
    updateTokens,
    updateUserInfo,
    updateAvatar,
    updateCoverImage,
    updateProfile,
    changePassword,
    fetchUnreadCounts,
    incrementNotifications,
    incrementMessages
  }
})

// ============================================================
// User Store - 兼容别名 (与 useAuthStore 相同)
// ============================================================

export const useUserStore = useAuthStore

// ============================================================
// Notification Store - 通知状态 (未读计数)
// ============================================================

export const useNotificationStore = defineStore('notification', () => {
  // 状态
  const unreadNotifications = ref(0)
  const unreadMessages = ref(0)

  // 计算属性
  const totalUnread = computed(() => unreadNotifications.value + unreadMessages.value)

  // 获取未读通知和私信数量
  async function fetchUnreadCounts() {
    const authStore = useAuthStore()
    if (!authStore.isLoggedIn) return

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

  // 增加未读通知数
  function incrementNotifications(count = 1) {
    unreadNotifications.value += count
  }

  // 增加未读消息数
  function incrementMessages(count = 1) {
    unreadMessages.value += count
  }

  // 重置未读计数
  function reset() {
    unreadNotifications.value = 0
    unreadMessages.value = 0
  }

  return {
    // 状态
    unreadNotifications,
    unreadMessages,

    // 计算属性
    totalUnread,

    // 方法
    fetchUnreadCounts,
    incrementNotifications,
    incrementMessages,
    reset
  }
})