<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-card card">
        <div class="auth-header">
          <h1>登录</h1>
          <p>欢迎回到校园博客</p>
        </div>
        
        <form class="auth-form" @submit.prevent="handleLogin">
          <div class="form-group">
            <label class="form-label">用户名</label>
            <input 
              v-model="form.username" 
              type="text" 
              class="form-input"
              placeholder="请输入用户名"
              required
            />
          </div>
          
          <div class="form-group">
            <label class="form-label">密码</label>
            <input 
              v-model="form.password" 
              type="password" 
              class="form-input"
              placeholder="请输入密码"
              required
              minlength="8"
            />
          </div>
          
          <div class="form-options">
            <label class="remember-me">
              <input type="checkbox" v-model="form.remember" />
              <span>记住我</span>
            </label>
            <router-link to="/password-reset" class="forgot-link">
              忘记密码？
            </router-link>
          </div>
          
          <button type="submit" class="btn btn-primary btn-block" :disabled="loading">
            <span v-if="loading" class="loading-spinner"></span>
            <span v-else>登录</span>
          </button>
        </form>
        
        <div class="auth-footer">
          <p>还没有账号？<router-link to="/register">立即注册</router-link></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { useLogger } from '../../utils/logger'
import { toast } from '../../utils/toast'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const logger = useLogger('Login')

const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
  remember: false
})

const handleLogin = async () => {
  if (!form.username || !form.password) {
    toast.error('请输入用户名和密码')
    return
  }

  // Basic format validation
  const usernameTrimmed = form.username.trim()
  if (usernameTrimmed.length < 3 || usernameTrimmed.length > 20) {
    toast.error('用户名长度应为3-20个字符')
    return
  }

  if (form.password.length < 8) {
    toast.error('密码长度至少8位')
    return
  }

  loading.value = true
  
  try {
    const result = await userStore.login({
      username: usernameTrimmed,
      password: form.password,
      remember: form.remember
    })
    
    if (result.success) {
      logger.info('Login successful')
      
      // 防止开放重定向攻击：只允许站内路径
      const redirect = route.query.redirect || '/'
      const safeRedirect = redirect.startsWith('/') && !redirect.startsWith('//') ? redirect : '/'
      router.push(safeRedirect)
    } else {
      logger.warn('Login failed', { message: result.message })
      toast.error(result.message || '登录失败')
    }
  } catch (error) {
    logger.error('Login error', { error: error.message })
    toast.error('登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-lg);
  background: linear-gradient(135deg, var(--primary-light) 0%, var(--background) 100%);
}

.auth-container {
  width: 100%;
  max-width: 400px;
}

.auth-card {
  padding: var(--spacing-xl);
}

.auth-header {
  text-align: center;
  margin-bottom: var(--spacing-xl);
}

.auth-header h1 {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-sm);
}

.auth-header p {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.auth-form {
  margin-bottom: var(--spacing-lg);
}

.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-lg);
}

.remember-me {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: 0.875rem;
  color: var(--text-secondary);
  cursor: pointer;
}

.remember-me input {
  width: 16px;
  height: 16px;
  accent-color: var(--primary);
}

.forgot-link {
  font-size: 0.875rem;
  color: var(--primary);
  text-decoration: none;
}

.forgot-link:hover {
  text-decoration: underline;
}

.btn-block {
  width: 100%;
  padding: 0.75rem;
  font-size: 1rem;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid transparent;
  border-top-color: currentColor;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.auth-footer {
  text-align: center;
  padding-top: var(--spacing-lg);
  border-top: 1px solid var(--border);
}

.auth-footer p {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.auth-footer a {
  color: var(--primary);
  text-decoration: none;
  font-weight: 500;
}

.auth-footer a:hover {
  text-decoration: underline;
}
</style>
