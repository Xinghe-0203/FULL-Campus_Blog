import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const STORAGE_KEY_PREFIX = 'edu_'

export const useThemeStore = defineStore('theme', () => {
  // 状态
  const isDarkMode = ref(false)
  const theme = ref('light') // 'light' | 'dark' | 'system'
  let mediaQuery = null
  let mediaHandler = null

  // 计算属性
  const currentTheme = computed(() => {
    if (theme.value === 'system') {
      return (mediaQuery || window.matchMedia('(prefers-color-scheme: dark)')).matches ? 'dark' : 'light'
    }
    return theme.value
  })

  // 初始化主题
  function initTheme() {
    // 从本地存储读取主题设置
    let savedTheme = 'light'
    try {
      savedTheme = localStorage.getItem(STORAGE_KEY_PREFIX + 'theme') || 'light'
    } catch {}
    theme.value = savedTheme
    
    // 应用主题
    applyTheme()
    
    // 清理旧监听器
    destroyTheme()
    
    // 监听系统主题变化
    mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    mediaHandler = () => {
      if (theme.value === 'system') {
        applyTheme()
      }
    }
    mediaQuery.addEventListener('change', mediaHandler)
  }

  // 清理监听器
  function destroyTheme() {
    if (mediaQuery && mediaHandler) {
      mediaQuery.removeEventListener('change', mediaHandler)
      mediaQuery = null
      mediaHandler = null
    }
  }

  // 应用主题
  function applyTheme() {
    const effectiveTheme = currentTheme.value
    isDarkMode.value = effectiveTheme === 'dark'
    
    if (isDarkMode.value) {
      document.documentElement.classList.add('dark-mode')
    } else {
      document.documentElement.classList.remove('dark-mode')
    }
  }

  // 切换主题
  function toggleTheme() {
    if (theme.value === 'light') {
      theme.value = 'dark'
    } else if (theme.value === 'dark') {
      theme.value = 'system'
    } else {
      theme.value = 'light'
    }
    
    try {
      localStorage.setItem(STORAGE_KEY_PREFIX + 'theme', theme.value)
    } catch {}
    applyTheme()
  }

  // 设置主题
  function setTheme(newTheme) {
    theme.value = newTheme
    try {
      localStorage.setItem(STORAGE_KEY_PREFIX + 'theme', theme.value)
    } catch {}
    applyTheme()
  }

  return {
    isDarkMode,
    theme,
    currentTheme,
    initTheme,
    destroyTheme,
    toggleTheme,
    setTheme
  }
})
