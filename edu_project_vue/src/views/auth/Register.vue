<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-card card">
        <div class="auth-header">
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
              placeholder="请输入用户名"
              required
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
            <input 
              v-model="form.password" 
              type="password" 
              class="form-input"
              placeholder="请输入密码（至少8位）"
              required
              minlength="8"
              @input="updateStrength"
            />
            <div v-if="form.password" class="strength-bar">
              <div class="strength-fill" :style="{ width: strengthPercent + '%', background: strengthColor }"></div>
            </div>
            <span class="form-hint">强度：<span :style="{ color: strengthColor, fontWeight: 600 }">{{ strengthText }}</span></span>
          </div>
          
          <div class="form-group">
            <label class="form-label">确认密码</label>
            <input 
              v-model="form.confirmPassword" 
              type="password" 
              class="form-input"
              placeholder="请再次输入密码"
              required
            />
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
                {{ codeSent ? `${countdown}s` : (sendingCode ? '发送中...' : '获取验证码') }}
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

<script setup>
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
let countdownTimer = null
const strengthResult = ref({ level: 'weak', text: '弱', color: '#F44336' })

const strengthPercent = computed(() => {
  const map = { weak: 33, medium: 66, strong: 100 }
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
  } catch (error) {
    logger.error('Failed to send code', { error: error.message })
    toast.error('发送验证码失败')
  } finally {
    sendingCode.value = false
  }
}

const handleRegister = async () => {
  // Validation
  const usernameTrimmed = form.username.trim()
  const emailTrimmed = form.email.trim()
  const passwordTrimmed = form.password.trim()
  const codeTrimmed = form.code.trim()

  if (!usernameTrimmed || !emailTrimmed || !passwordTrimmed) {
    toast.error('请填写所有必填项')
    return
  }

  if (usernameTrimmed.length < 3 || usernameTrimmed.length > 20) {
    toast.error('用户名长度应为3-20个字符')
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
      toast.success('注册成功，请登录')
      router.push('/login')
    } else {
      logger.warn('Registration failed', { message: response.message })
      toast.error(response.message || '注册失败')
    }
  } catch (error) {
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

.agreement {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-sm);
  font-size: 0.8rem;
  color: var(--text-secondary);
  cursor: pointer;
}

.agreement input {
  width: 16px;
  height: 16px;
  margin-top: 2px;
  accent-color: var(--primary);
}

.agreement a {
  color: var(--primary);
  text-decoration: none;
}

.agreement a:hover {
  text-decoration: underline;
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
}

.strength-bar {
  height: 4px;
  background: var(--border);
  border-radius: 2px;
  margin-top: 6px;
  overflow: hidden;
}
.strength-fill {
  height: 100%;
  border-radius: 2px;
  transition: width 0.3s ease, background 0.3s ease;
}
.form-hint {
  display: block;
  font-size: 0.6875rem;
  color: var(--text-muted);
  margin-top: 4px;
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
