import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { userApi } from '../api/user'
import { notificationApi } from '../api/notification'
import { messageApi } from '../api/message'
import { useLogger } from '../utils/logger'

const logger = useLogger('UserStore')

const STORAGE_KEY_PREFIX = 'edu_'

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
  
  function encodeData(data) {
    if (!data) return null
    try {
      return btoa(unescape(encodeURIComponent(JSON.stringify(data))))
    } catch {
      return null
    }
  }
  
  function decodeData(encoded) {
    if (!encoded) return null
    try {
      return JSON.parse(decodeURIComponent(escape(atob(encoded))))
    } catch {
      return null
    }
  }

  // 从本地存储恢复状态
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

  // 重置所有状态
  function resetState() {
    user.value = null
    token.value = null
    refreshToken.value = null
    unreadNotifications.value = 0
    unreadMessages.value = 0
    clearStorage()
  }

  // 清除本地存储
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

  // 验证Token是否有效（检查JWT过期时间，避免每次验证都刷新Token）
  // 注意：不在此处调用 resetState()，让 api/index.js 的响应拦截器处理刷新
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

  // 检查Token是否可能已被窃取（异常检测）
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

  // 登录
  async function login(credentials) {
    loading.value = true
    try {
      const response = await userApi.login(credentials)
      // Handle both flat and nested response structures
      const loginData = response.data || response

      let { token: newToken, refreshToken: newRefreshToken, id, username, nickname, avatar } = loginData

      // Fallback to nested data if backend returns { data: { user, token, refreshToken } }
      if (!id && loginData.data) {
        const nestedData = loginData.data
        newToken = nestedData.token
        newRefreshToken = nestedData.refreshToken
        id = nestedData.id
        username = nestedData.username
        nickname = nestedData.nickname
        avatar = nestedData.avatar
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
        email: loginData.email || loginData.data?.email
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

  // 注册
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

  // 登出
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

  // 更新Token
  function updateTokens(newToken, newRefreshToken) {
    token.value = newToken
    refreshToken.value = newRefreshToken
    saveToStorage()
  }

  // 更新用户信息
  function updateUserInfo(userData) {
    user.value = { ...user.value, ...userData }
    saveToStorage()
    logger.info('User info updated', { userId: user.value?.id })
  }

  // 更新头像
  function updateAvatar(avatarUrl) {
    if (user.value) {
      user.value.avatar = avatarUrl
      saveToStorage()
    }
  }

  // 更新封面图
  function updateCoverImage(coverImageUrl) {
    if (user.value) {
      user.value.coverImage = coverImageUrl
      saveToStorage()
    }
  }

  // 获取未读消息数
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

  // 更新个人资料
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

  // 修改密码
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
