<template>
  <div class="auth-page">
    <div class="auth-bg-orb orb-1"></div>
    <div class="auth-bg-orb orb-2"></div>
    <div class="auth-bg-orb orb-3"></div>
    <div class="auth-container">
      <div class="auth-card">
        <div class="auth-header">
          <div class="auth-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
          </div>
          <h1>登录</h1>
          <p>欢迎回到校园博客</p>
        </div>
        
        <form class="auth-form" @submit.prevent="handleLogin">
          <div class="form-group">
            <label class="form-label">用户名 / 邮箱</label>
            <input 
              v-model="form.account" 
              type="text" 
              class="form-input"
              placeholder="请输入用户名或邮箱"
              required
            />
          </div>
          
          <div class="form-group">
            <label class="form-label">密码</label>
            <div class="password-input-wrapper">
              <input 
                v-model="form.password" 
                :type="showPassword ? 'text' : 'password'" 
                class="form-input"
                placeholder="请输入密码"
                required
                minlength="8"
              />
              <button type="button" class="password-toggle" @click="showPassword = !showPassword"
                      :aria-label="showPassword ? '隐藏密码' : '显示密码'">
                <svg v-if="!showPassword" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                  <circle cx="12" cy="12" r="3"/>
                </svg>
                <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                  <line x1="1" y1="1" x2="23" y2="23"/>
                </svg>
              </button>
            </div>
          </div>
          
          <div class="form-options">
            <label class="remember-me">
              <input type="checkbox" v-model="form.remember" />
              <span class="checkmark"></span>
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

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { useLogger } from '../../utils/logger'
import { toast } from '../../utils/toast'
import type { LoginRequest } from '../../types/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const logger = useLogger('Login')

const loading = ref(false)
const showPassword = ref(false)
const form = reactive({
  account: '',
  password: '',
  remember: false
})

// 判断输入是邮箱还是用户名
const isEmail = (str: string) => {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(str)
}

const handleLogin = async () => {
  if (!form.account || !form.password) {
    toast.error('请输入用户名/邮箱和密码')
    return
  }

  const accountTrimmed = form.account.trim()
  const accountIsEmail = isEmail(accountTrimmed)

  // 用户名格式校验（如果不是邮箱格式）
  if (!accountIsEmail && (accountTrimmed.length < 3 || accountTrimmed.length > 20)) {
    toast.error('用户名长度应为3-20个字符')
    return
  }

  if (form.password.length < 8) {
    toast.error('密码长度至少8位')
    return
  }

  loading.value = true
  
  try {
    const loginData: LoginRequest = {
      username: '',
      password: form.password,
      remember: form.remember
    }

    // 根据输入类型选择登录字段
    if (accountIsEmail) {
      loginData.email = accountTrimmed
    } else {
      loginData.username = accountTrimmed
    }

    const result = await userStore.login(loginData)
    
    if (result.success) {
      logger.info('Login successful')
      const redirect = (route.query.redirect as string) || '/'
      const safeRedirect = redirect.startsWith('/') && !redirect.startsWith('//') ? redirect : '/'
      router.push(safeRedirect)
    } else {
      logger.warn('Login failed', { message: result.message })
      toast.error(result.message || '登录失败')
    }
  } catch (error: any) {
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
  background: var(--background-gradient);
  position: relative;
  overflow: hidden;
}

.auth-bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
  animation: orb-float 20s ease-in-out infinite;
}

.orb-1 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, var(--primary-start), var(--primary-end));
  top: -10%;
  left: -10%;
  animation-delay: 0s;
}

.orb-2 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, var(--accent), var(--primary-end));
  bottom: -5%;
  right: -5%;
  animation-delay: -7s;
}

.orb-3 {
  width: 250px;
  height: 250px;
  background: linear-gradient(135deg, var(--info), var(--primary-start));
  top: 50%;
  left: 60%;
  animation-delay: -14s;
}

@keyframes orb-float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(30px, -30px) scale(1.1); }
  50% { transform: translate(-20px, 20px) scale(0.95); }
  75% { transform: translate(20px, 10px) scale(1.05); }
}

.auth-container {
  width: 100%;
  max-width: 480px;
  position: relative;
  z-index: 1;
}

.auth-card {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border-wet);
  border-radius: var(--radius-xl);
  box-shadow: var(--glass-shadow-wet);
  padding: var(--spacing-2xl);
  transition: all var(--transition-slow);
  position: relative;
  overflow: hidden;
}

.auth-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent);
}

.auth-card:hover {
  box-shadow: var(--shadow-lg), var(--glass-shadow-wet);
}

.auth-header {
  text-align: center;
  margin-bottom: var(--spacing-xl);
}

.auth-icon {
  width: 72px;
  height: 72px;
  margin: 0 auto var(--spacing-md);
  background: linear-gradient(135deg, var(--primary-start), var(--primary-end));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 8px 24px var(--primary-glow);
}

.auth-header h1 {
  font-size: 1.875rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
  letter-spacing: -0.02em;
}

.auth-header p {
  font-size: 0.9375rem;
  color: var(--text-secondary);
}

.auth-form {
  margin-bottom: var(--spacing-lg);
}

.form-group {
  margin-bottom: var(--spacing-md);
}

.form-label {
  display: block;
  margin-bottom: var(--spacing-xs);
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-primary);
}

.form-input {
  width: 100%;
  padding: 0.75rem 1rem;
  font-size: 0.9375rem;
  font-family: inherit;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius);
  color: var(--text-primary);
  transition: all var(--transition);
  box-shadow: var(--glass-shadow);
}

.form-input:hover:not(:disabled):not(:focus) {
  border-color: var(--text-muted);
}

.form-input:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-light), var(--glass-shadow);
  background: var(--glass-hover);
}

.form-input::placeholder {
  color: var(--text-muted);
}

.password-input-wrapper {
  position: relative;
}

.password-input-wrapper .form-input {
  padding-right: 40px;
}

.password-toggle {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
}

.password-toggle:hover {
  color: var(--primary);
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
  position: relative;
}

.remember-me input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
}

.checkmark {
  width: 18px;
  height: 18px;
  border: 2px solid var(--glass-border);
  border-radius: var(--radius-xs);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
  background: var(--glass-bg);
  flex-shrink: 0;
}

.remember-me input:checked + .checkmark {
  background: linear-gradient(135deg, var(--primary-start), var(--primary-end));
  border-color: var(--primary);
}

.remember-me input:checked + .checkmark::after {
  content: '';
  width: 5px;
  height: 10px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
  margin-top: -2px;
}

.forgot-link {
  font-size: 0.875rem;
  color: var(--primary);
  text-decoration: none;
  font-weight: 500;
  transition: all var(--transition-fast);
}

.forgot-link:hover {
  color: var(--primary-hover);
  text-decoration: underline;
}

.btn-block {
  width: 100%;
  padding: 0.875rem;
  font-size: 1rem;
  font-weight: 600;
  border-radius: var(--radius);
  background: linear-gradient(135deg, var(--primary-start), var(--primary-end));
  color: var(--text-inverse);
  border: none;
  cursor: pointer;
  transition: all var(--transition);
  box-shadow: var(--shadow-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
}

.btn-block:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md), var(--shadow-glow-primary);
}

.btn-block:active:not(:disabled) {
  transform: translateY(0);
}

.btn-block:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.auth-footer {
  text-align: center;
  padding-top: var(--spacing-lg);
  border-top: 1px solid var(--glass-border);
}

.auth-footer p {
  font-size: 0.9375rem;
  color: var(--text-secondary);
}

.auth-footer a {
  color: var(--primary);
  text-decoration: none;
  font-weight: 600;
  transition: all var(--transition-fast);
}

.auth-footer a:hover {
  color: var(--primary-hover);
  text-decoration: underline;
}

@media (max-width: 640px) {
  .auth-page {
    padding: var(--spacing-md);
  }
  
  .auth-card {
    padding: var(--spacing-xl);
    border-radius: var(--radius-lg);
  }
  
  .auth-header h1 {
    font-size: 1.5rem;
  }
  
  .auth-icon {
    width: 60px;
    height: 60px;
  }
  
  .auth-icon svg {
    width: 32px;
    height: 32px;
  }
  
  .form-options {
    flex-direction: column;
    gap: var(--spacing-sm);
    align-items: flex-start;
  }
  
  .orb-1, .orb-2, .orb-3 {
    filter: blur(60px);
    opacity: 0.3;
  }
  
  .orb-1 { width: 250px; height: 250px; }
  .orb-2 { width: 200px; height: 200px; }
  .orb-3 { width: 180px; height: 180px; }
}
</style>
