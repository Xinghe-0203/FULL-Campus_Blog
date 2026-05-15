/**
 * 前端日志系统
 * 提供统一的日志记录方法，支持发送到后端
 */

// 日志级别
const LOG_LEVELS = {
  DEBUG: 0,
  INFO: 1,
  WARN: 2,
  ERROR: 3
}

// 当前日志级别（可通过环境变量配置）
const currentLevel = import.meta.env.DEV ? LOG_LEVELS.DEBUG : LOG_LEVELS.INFO

let isInitialized = false

// 日志队列（用于批量发送到后端）
let logQueue = []
let flushTimer = null
let isDestroyed = false

/**
 * 生成唯一ID
 */
function generateId() {
  return Math.random().toString(36).substring(2, 15)
}

/**
 * 格式化日志数据
 */
function formatLogData(level, module, message, data) {
  return {
    id: generateId(),
    timestamp: new Date().toISOString(),
    level,
    module,
    message,
    data,
    url: window.location.href,
    userAgent: navigator.userAgent
  }
}

/**
 * 发送日志到后端（批量）
 */
async function sendToBackend(logEntries) {
  try {
    await fetch('/api/log', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ logs: logEntries })
    })
  } catch (error) {
    // 静默失败，不影响用户体验
  }
}

/**
 * 批量发送日志
 */
function flushLogs() {
  if (isDestroyed || logQueue.length === 0) return

  const logsToSend = [...logQueue]
  logQueue = []

  sendToBackend(logsToSend)
}

/**
 * 记录日志
 */
function log(level, module, message, data = null) {
  if (isDestroyed || LOG_LEVELS[level] < currentLevel) return

  const logEntry = formatLogData(level, module, message, data)

  // 控制台输出（开发环境）
  if (import.meta.env.DEV) {
    const style = getConsoleStyle(level)
    console.log(
      `%c[${level}]%c ${module}%c ${message}`,
      style,
      'color: #888;',
      'color: inherit;',
      data || ''
    )
    return
  }

  // 添加到队列（仅生产环境）
  logQueue.push(logEntry)

  // 错误日志立即发送
  if (level === 'ERROR') {
    flushLogs()
  } else if (logQueue.length >= 10) {
    flushLogs()
  }
}

/**
 * 获取控制台样式
 */
function getConsoleStyle(level) {
  switch (level) {
    case 'DEBUG':
      return 'color: #888; font-weight: normal;'
    case 'INFO':
      return 'color: #2196F3; font-weight: bold;'
    case 'WARN':
      return 'color: #FF9800; font-weight: bold;'
    case 'ERROR':
      return 'color: #F44336; font-weight: bold;'
    default:
      return 'color: inherit;'
  }
}

/**
 * 创建日志器实例
 */
export function createLogger(module) {
  return {
    debug: (message, data) => log('DEBUG', module, message, data),
    info: (message, data) => log('INFO', module, message, data),
    warn: (message, data) => log('WARN', module, message, data),
    error: (message, data) => log('ERROR', module, message, data)
  }
}

/**
 * Hook for Vue组件使用
 */
export function useLogger(module) {
  return createLogger(module)
}

/**
 * 性能日志
 */
export function logPerformance(name, duration) {
  const level = duration > 3000 ? 'WARN' : duration > 1000 ? 'INFO' : 'DEBUG'
  log(level, 'Performance', `${name}: ${duration}ms`)
}

/**
 * 用户行为日志
 */
export function logUserAction(action, data) {
  log('INFO', 'UserAction', action, data)
}

/**
 * API调用日志
 */
export function logApiCall(method, url, status, duration) {
  const level = status >= 400 ? 'ERROR' : 'INFO'
  log(level, 'API', `${method} ${url} - ${status}`, { duration })
}

function handleBeforeUnload() {
  if (logQueue.length > 0) {
    const logsToSend = [...logQueue]
    logQueue = []
    try {
      navigator.sendBeacon('/api/log', JSON.stringify({ logs: logsToSend }))
    } catch (e) {
      // sendBeacon 失败时静默处理
    }
  }
}

/**
 * 初始化日志系统
 */
export function initLogger() {
  if (isInitialized) return
  isInitialized = true

  // 仅生产环境启动定时刷新和卸载前上报
  if (!import.meta.env.DEV) {
    flushTimer = setInterval(flushLogs, 30000)
    window.addEventListener('beforeunload', handleBeforeUnload)
  }

  // 记录页面加载（开发环境仅控制台输出）
  log('INFO', 'Logger', 'Logger initialized', {
    mode: import.meta.env.MODE,
    level: currentLevel
  })
}

/**
 * 销毁日志系统，清理定时器和事件监听
 */
export function destroyLogger() {
  if (flushTimer) {
    clearInterval(flushTimer)
    flushTimer = null
  }
  window.removeEventListener('beforeunload', handleBeforeUnload)
  flushLogs()
  isDestroyed = true
}

// 默认导出
// 自动初始化
if (typeof window !== 'undefined') {
  initLogger()
}

export default {
  createLogger,
  useLogger,
  logPerformance,
  logUserAction,
  logApiCall,
  initLogger,
  destroyLogger
}
