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
              <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="8.5" cy="7" r="4"/>
              <line x1="20" y1="8" x2="20" y2="14"/>
              <line x1="23" y1="11" x2="17" y2="11"/>
            </svg>
          </div>
          <h1>注册</h1>
          <p>加入校园博客社区</p>
        </div>
        
        <form class="auth-form" @submit.prevent="handleRegister">
          <div class="form-group">
            <label class="form-label">用户名</label>
            <input 
              v-model="form.username" 
              type="text" 
              class="form-input"
              placeholder="6位以上小写英文字母、数字"
              required
              pattern="[a-z0-9]{6,}"
              title="用户名需为6位以上的小写英文字母和数字"
            />
          </div>
          
          <div class="form-group">
            <label class="form-label">邮箱</label>
            <input 
              v-model="form.email" 
              type="email" 
              class="form-input"
              placeholder="请输入邮箱"
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
                placeholder="请输入密码（至少8位）"
                required
                minlength="8"
                @input="updateStrength"
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
            <div v-if="form.password" class="strength-meter">
              <div class="strength-bar">
                <div class="strength-fill" :style="{ width: strengthPercent + '%', background: strengthColor }"></div>
              </div>
              <span class="strength-text">强度：<span :style="{ color: strengthColor, fontWeight: 600 }">{{ strengthText }}</span></span>
            </div>
          </div>
          
          <div class="form-group">
            <label class="form-label">确认密码</label>
            <div class="password-input-wrapper">
              <input 
                v-model="form.confirmPassword" 
                :type="showConfirmPassword ? 'text' : 'password'" 
                class="form-input"
                :class="{ 'input-error': form.confirmPassword && form.confirmPassword !== form.password }"
                placeholder="请再次输入密码"
                required
              />
              <button type="button" class="password-toggle" @click="showConfirmPassword = !showConfirmPassword"
                      :aria-label="showConfirmPassword ? '隐藏密码' : '显示密码'">
                <svg v-if="!showConfirmPassword" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                  <circle cx="12" cy="12" r="3"/>
                </svg>
                <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                  <line x1="1" y1="1" x2="23" y2="23"/>
                </svg>
              </button>
            </div>
            <span v-if="form.confirmPassword && form.confirmPassword !== form.password" class="error-hint">
              两次输入的密码不一致
            </span>
          </div>
          
          <div class="form-group">
            <label class="form-label">邮箱验证码</label>
            <div class="code-input-group">
              <input 
                v-model="form.code" 
                type="text" 
                class="form-input"
                placeholder="请输入验证码"
                required
                maxlength="6"
              />
              <button 
                type="button" 
                class="btn btn-secondary btn-code"
                @click="sendCode"
                :disabled="sendingCode || codeSent"
              >
                <span v-if="sendingCode" class="btn-spinner"></span>
                <span v-else>{{ codeSent ? `${countdown}s` : '获取验证码' }}</span>
              </button>
            </div>
          </div>
          
          <div class="form-group">
            <label class="form-label">昵称（可选）</label>
            <input 
              v-model="form.nickname" 
              type="text" 
              class="form-input"
              placeholder="请输入昵称"
            />
          </div>
          
          <div class="form-group">
            <label class="agreement">
              <input type="checkbox" v-model="form.agreed" required />
              <span class="checkmark"></span>
              <span>我已阅读并同意 <a href="/agreement" target="_blank">用户协议</a> 和 <a href="/privacy" target="_blank">隐私政策</a></span>
            </label>
          </div>
          
          <button type="submit" class="btn btn-primary btn-block" :disabled="loading || !form.agreed">
            <span v-if="loading" class="loading-spinner"></span>
            <span v-else>注册</span>
          </button>
        </form>
        
        <div class="auth-footer">
          <p>已有账号？<router-link to="/login">立即登录</router-link></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { userApi } from '../../api/user'
import { useLogger } from '../../utils/logger'
import { toast } from '../../utils/toast'
import { checkPasswordStrength } from '../../utils'

const router = useRouter()
const userStore = useUserStore()
const logger = useLogger('Register')

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
})

const loading = ref(false)
const sendingCode = ref(false)
const codeSent = ref(false)
const countdown = ref(60)
let countdownTimer: ReturnType<typeof setInterval> | undefined = undefined
const strengthResult = ref({ level: 'weak', text: '弱', color: '#F44336' })
const showPassword = ref(false)
const showConfirmPassword = ref(false)

const strengthPercent = computed(() => {
  const map: Record<string, number> = { weak: 33, medium: 66, strong: 100 }
  return map[strengthResult.value.level] || 0
})
const strengthColor = computed(() => strengthResult.value.color)
const strengthText = computed(() => strengthResult.value.text)

function updateStrength() {
  strengthResult.value = checkPasswordStrength(form.password)
}

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  code: '',
  agreed: false
})

const sendCode = async () => {
  const emailTrimmed = form.email.trim()
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailTrimmed)) {
    toast.error('请输入有效的邮箱地址')
    return
  }

  sendingCode.value = true
  try {
    const usernameTrimmed = form.username.trim()
    await userApi.sendRegisterCode(emailTrimmed, usernameTrimmed)
    codeSent.value = true
    countdown.value = 60
    countdownTimer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(countdownTimer)
        codeSent.value = false
      }
    }, 1000)
    toast.success('验证码已发送')
  } catch (error: any) {
    logger.error('Failed to send code', { error: error.message })
    toast.error(error.response?.data?.message || '发送验证码失败')
  } finally {
    sendingCode.value = false
  }
}

const handleRegister = async () => {
  const usernameTrimmed = form.username.trim()
  const emailTrimmed = form.email.trim()
  const passwordTrimmed = form.password.trim()
  const codeTrimmed = form.code.trim()

  if (!usernameTrimmed || !emailTrimmed || !passwordTrimmed) {
    toast.error('请填写所有必填项')
    return
  }

  if (!/^[a-z0-9]{6,20}$/.test(usernameTrimmed)) {
    toast.error('用户名需为6-20位小写英文字母和数字')
    return
  }

  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailTrimmed)) {
    toast.error('请输入有效的邮箱地址')
    return
  }

  if (passwordTrimmed.length < 8) {
    toast.error('密码长度至少8位')
    return
  }

  if (passwordTrimmed !== form.confirmPassword) {
    toast.error('两次输入的密码不一致')
    return
  }

  if (!codeTrimmed) {
    toast.error('请输入邮箱验证码')
    return
  }

  if (!form.agreed) {
    toast.error('请阅读并同意用户协议和隐私政策')
    return
  }

  loading.value = true
  
  try {
    const response = await userApi.verifyRegisterCode({
      username: usernameTrimmed,
      email: emailTrimmed,
      password: passwordTrimmed,
      nickname: form.nickname?.trim() || usernameTrimmed,
      code: codeTrimmed
    })
    
    if (response.code === 200) {
      logger.info('Registration successful')
      const res = await userStore.login({
        username: usernameTrimmed,
        password: passwordTrimmed
      })
      if (res.success) {
        toast.success('注册成功，欢迎加入！')
        router.push('/')
      } else {
        toast.success('注册成功，请登录')
        router.push('/login')
      }
    } else {
      logger.warn('Registration failed', { message: response.message })
      toast.error(response.message || '注册失败')
    }
  } catch (error: any) {
    logger.error('Registration error', { error: error.message })
    toast.error(error.response?.data?.message || '注册失败，请稍后重试')
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
  background: linear-gradient(135deg, var(--accent), var(--primary-end));
  top: -10%;
  right: -10%;
  animation-delay: 0s;
}

.orb-2 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, var(--primary-start), var(--info));
  bottom: -5%;
  left: -5%;
  animation-delay: -7s;
}

.orb-3 {
  width: 250px;
  height: 250px;
  background: linear-gradient(135deg, var(--success), var(--primary-start));
  top: 40%;
  left: 70%;
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
  max-width: 520px;
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
  background: linear-gradient(135deg, var(--accent), var(--primary-end));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 8px 24px rgba(236, 72, 153, 0.4);
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

.form-input.input-error {
  border-color: var(--error);
}

.form-input.input-error:focus {
  box-shadow: 0 0 0 3px var(--error-light), var(--glass-shadow);
}

.strength-meter {
  margin-top: var(--spacing-sm);
}

.strength-bar {
  height: 6px;
  background: var(--border);
  border-radius: var(--radius-full);
  overflow: hidden;
  margin-bottom: 4px;
}

.strength-fill {
  height: 100%;
  border-radius: var(--radius-full);
  transition: width 0.4s cubic-bezier(0.4, 0, 0.2, 1), background 0.4s ease;
}

.strength-text {
  display: block;
  font-size: 0.75rem;
  color: var(--text-muted);
}

.error-hint {
  display: block;
  font-size: 0.75rem;
  color: var(--error);
  margin-top: var(--spacing-xs);
}

.code-input-group {
  display: flex;
  gap: var(--spacing-sm);
}

.code-input-group .form-input {
  flex: 1;
}

.btn-code {
  white-space: nowrap;
  flex-shrink: 0;
  padding: 0.75rem 1rem;
  font-size: 0.8125rem;
  min-width: 110px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-xs);
}

.btn-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid var(--primary);
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.agreement {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-sm);
  font-size: 0.8125rem;
  color: var(--text-secondary);
  cursor: pointer;
  position: relative;
  line-height: 1.5;
}

.agreement input {
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
  margin-top: 1px;
}

.agreement input:checked + .checkmark {
  background: linear-gradient(135deg, var(--primary-start), var(--primary-end));
  border-color: var(--primary);
}

.agreement input:checked + .checkmark::after {
  content: '';
  width: 5px;
  height: 10px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
  margin-top: -2px;
}

.agreement a {
  color: var(--primary);
  text-decoration: none;
  font-weight: 500;
}

.agreement a:hover {
  text-decoration: underline;
}

.btn-block {
  width: 100%;
  padding: 0.875rem;
  font-size: 1rem;
  font-weight: 600;
  border-radius: var(--radius);
  background: linear-gradient(135deg, var(--accent), var(--primary-end));
  color: var(--text-inverse);
  border: none;
  cursor: pointer;
  transition: all var(--transition);
  box-shadow: var(--shadow-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-md);
}

.btn-block:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md), 0 0 20px rgba(236, 72, 153, 0.4);
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
  
  .code-input-group {
    flex-direction: column;
  }
  
  .btn-code {
    width: 100%;
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
