import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { marked } from 'marked'
import { useLogger } from './utils/logger'

// 导入样式
import './styles/main.css'

// 全局配置Markdown渲染（breaks:true 确保单换行被转换为<br>）
marked.setOptions({
  gfm: true,
  breaks: true
})

const app = createApp(App)
const logger = useLogger('App')

// 使用Pinia状态管理
const pinia = createPinia()
app.use(pinia)

// 在路由初始化前恢复用户认证状态，确保路由守卫能正确判断登录态
import { useUserStore } from './stores/user'
const userStore = useUserStore(pinia)
userStore.restoreFromStorage()

// 使用路由
app.use(router)

// 全局错误处理
app.config.errorHandler = (err, instance, info) => {
  logger.error('Vue error', { error: err.message, info })
  console.error('[Vue Error]', err)
}

// 全局警告处理（仅开发环境）
if (import.meta.env.DEV) {
  app.config.warnHandler = (msg, instance, info) => {
    logger.warn('Vue warning', { message: msg, info })
  }
}

// 挂载应用
app.mount('#app')

logger.info('Application initialized', {
  version: '2.0.0',
  mode: import.meta.env.MODE
})
