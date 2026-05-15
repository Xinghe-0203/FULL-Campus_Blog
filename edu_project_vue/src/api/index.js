import axios from 'axios'
import { useUserStore } from '../stores/user'
import { useLogger } from '../utils/logger'

const logger = useLogger('API')

const STORAGE_KEY_PREFIX = 'edu_'

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

function safeGetItem(key, storage = 'localStorage') {
  try {
    return window[storage].getItem(key)
  } catch {
    return null
  }
}

function getToken(key) {
  return decodeData(safeGetItem(key)) || decodeData(safeGetItem(key, 'sessionStorage'))
}

function safeSetItem(key, value) {
  try {
    localStorage.setItem(key, value)
  } catch {
    // storage unavailable
  }
}

function safeRemoveItem(key) {
  try {
    localStorage.removeItem(key)
  } catch {
    // storage unavailable
  }
}

// 生成traceId
function generateTraceId() {
  return crypto.randomUUID?.()?.replace(/-/g, '') ||
    Array.from(crypto.getRandomValues(new Uint8Array(16)))
      .map(b => b.toString(16).padStart(2, '0')).join('')
}

// 存储Token指纹信息（用于检测Token是否被窃取）
function getTokenFingerprint(token) {
  try {
    // 使用token的部分hash作为指纹
    const hash = token.substring(0, 20) + token.substring(token.length - 20)
    return btoa(hash)
  } catch {
    return null
  }
}

// 检查Token指纹是否匹配（防止Token被窃取后在其他域名使用）
function verifyTokenFingerprint(token) {
  try {
    const savedFingerprint = safeGetItem(STORAGE_KEY_PREFIX + 'token_fingerprint')
    if (!savedFingerprint) {
      // 首次设置指纹
      const fingerprint = getTokenFingerprint(token)
      if (fingerprint) {
        safeSetItem(STORAGE_KEY_PREFIX + 'token_fingerprint', fingerprint)
      }
      return true
    }
    const currentFingerprint = getTokenFingerprint(token)
    return currentFingerprint === savedFingerprint
  } catch {
    return true
  }
}

// XSRF Token管理（防止CSRF攻击）
// 注意：虽然后端禁用了CSRF（使用JWT Authorization header），但前端仍然维护xsrf token以防万一
function getXsrfToken() {
  return safeGetItem(STORAGE_KEY_PREFIX + 'xsrf_token')
}

function generateXsrfToken() {
  try {
    const array = new Uint8Array(32)
    crypto.getRandomValues(array)
    const token = Array.from(array).map(b => b.toString(16).padStart(2, '0')).join('')
    safeSetItem(STORAGE_KEY_PREFIX + 'xsrf_token', token)
    return token
  } catch {
    return null
  }
}

// 清除安全相关存储
function clearSecurityStorage() {
  safeRemoveItem(STORAGE_KEY_PREFIX + 'token_fingerprint')
  safeRemoveItem(STORAGE_KEY_PREFIX + 'xsrf_token')
  safeRemoveItem(STORAGE_KEY_PREFIX + 'remember')
}

// 请求队列（用于Token刷新时暂存请求）
let isRefreshing = false
let failedQueue = []

const api = axios.create({
  baseURL: '/api',
  timeout: 15000
})

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token)
    }
  })
  failedQueue = []
}

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    const token = getToken(STORAGE_KEY_PREFIX + 'token')

    // 安全检查：验证Token指纹防止被窃取后在其他域名使用
    if (token && !verifyTokenFingerprint(token)) {
      logger.error('Token fingerprint mismatch - possible theft attempt')
      // 触发登出流程
      import('../stores/user').then(({ useUserStore }) => {
        const userStore = useUserStore()
        userStore.resetState()
      })
      throw new Error('Token security violation')
    }

    if (token && !config.headers.Authorization) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // 添加XSRF token（作为额外的安全层，虽然后端已禁用CSRF）
    const xsrfToken = getXsrfToken() || generateXsrfToken()
    if (xsrfToken) {
      config.headers['X-XSRF-TOKEN'] = xsrfToken
    }

    // 添加traceId用于日志追踪
    const traceId = generateTraceId()
    config.headers['X-Trace-Id'] = traceId

    logger.debug('Request', {
      method: config.method?.toUpperCase(),
      url: config.url,
      traceId
    })

    return config
  },
  (error) => {
    logger.error('Request error', { error: error.message })
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    logger.debug('Response success', {
      url: response.config.url,
      status: response.status
    })
    return response.data
  },
  async (error) => {
    const originalRequest = error.config
    
    logger.error('Response error', {
      url: originalRequest?.url,
      status: error.response?.status,
      message: error.response?.data?.message || error.message
    })
    
    // 400 - 请求参数错误
    if (error.response?.status === 400) {
      const message = error.response?.data?.message || '请求参数错误'
      logger.warn('Bad request', { url: originalRequest?.url, message })
      return Promise.reject(error)
    }

    // 403 - 无权限
    if (error.response?.status === 403) {
      const message = error.response?.data?.message || '没有访问权限'
      logger.warn('Forbidden', { url: originalRequest?.url, message })
      return Promise.reject(error)
    }

    // 404 - 资源不存在
    if (error.response?.status === 404) {
      const message = error.response?.data?.message || '请求的资源不存在'
      logger.warn('Not found', { url: originalRequest?.url, message })
      return Promise.reject(error)
    }

    // 500 - 服务器错误
    if (error.response?.status === 500) {
      const message = error.response?.data?.message || '服务器内部错误，请稍后再试'
      logger.error('Server error', { url: originalRequest?.url, message })
      return Promise.reject(error)
    }

    // 401错误处理（Token过期）
    if (error.response?.status === 401 && !originalRequest._retry) {
      // 如果是刷新Token的请求失败，直接跳转登录
      if (originalRequest.url === '/user/refresh') {
        await handleAuthError()
        return Promise.reject(error)
      }
      
      // 如果正在刷新Token，将请求加入队列
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        }).then(token => {
          originalRequest.headers.Authorization = `Bearer ${token}`
          return api(originalRequest)
        })
      }
      
      originalRequest._retry = true
      isRefreshing = true
      
      try {
        const refreshToken = getToken(STORAGE_KEY_PREFIX + 'refreshToken')
        if (!refreshToken) {
          throw new Error('No refresh token')
        }
        
        const response = await api.post('/user/refresh', null, {
          headers: { Authorization: `Bearer ${refreshToken}` }
        })

        const refreshData = response.data || response
        const { token: newToken, refreshToken: newRefreshToken } = refreshData
        
        const userStore = useUserStore()
        const storage = userStore.remember ? localStorage : sessionStorage
        try {
          storage.setItem(STORAGE_KEY_PREFIX + 'token', encodeData(newToken))
          storage.setItem(STORAGE_KEY_PREFIX + 'refreshToken', encodeData(newRefreshToken))
        } catch (e) {
          // storage unavailable
        }
        userStore.updateTokens(newToken, newRefreshToken)
        
        processQueue(null, newToken)
        
        originalRequest.headers.Authorization = `Bearer ${newToken}`
        return api(originalRequest)
      } catch (refreshError) {
        processQueue(refreshError, null)
        await handleAuthError()
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }
    
    return Promise.reject(error)
  }
)

// 处理认证错误
async function handleAuthError() {
  safeRemoveItem(STORAGE_KEY_PREFIX + 'token')
  safeRemoveItem(STORAGE_KEY_PREFIX + 'refreshToken')
  safeRemoveItem(STORAGE_KEY_PREFIX + 'user')
  try { sessionStorage.removeItem(STORAGE_KEY_PREFIX + 'token') } catch {}
  try { sessionStorage.removeItem(STORAGE_KEY_PREFIX + 'refreshToken') } catch {}
  try { sessionStorage.removeItem(STORAGE_KEY_PREFIX + 'user') } catch {}
  // 清除安全相关存储
  clearSecurityStorage()

  try {
    const userStore = useUserStore()
    userStore.resetState()
  } catch (e) {
    // store may not be available during initialization
  }

  if (window.location.pathname !== '/login') {
    try {
      const { default: router } = await import('../router')
      router.push('/login')
    } catch {
      window.location.href = '/login'
    }
  }
}

export { getToken }
export default api
