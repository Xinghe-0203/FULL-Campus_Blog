<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-card card">
        <div class="auth-header">
          <h1>找回密码</h1>
          <p>通过邮箱重置密码</p>
        </div>
        
        <form class="auth-form" @submit.prevent="handleSubmit">
          <!-- 步骤1：输入邮箱 -->
          <template v-if="step === 1">
            <div class="form-group">
              <label class="form-label">注册邮箱</label>
              <input 
                v-model="form.email" 
                type="email" 
                class="form-input"
                placeholder="请输入注册时使用的邮箱"
                required
              />
            </div>
            
            <button type="submit" class="btn btn-primary btn-block" :disabled="loading">
              <span v-if="loading" class="loading-spinner"></span>
              <span v-else>发送验证码</span>
            </button>
          </template>
          
          <!-- 步骤2：输入验证码和新密码 -->
          <template v-if="step === 2">
            <div class="form-group">
              <label class="form-label">验证码</label>
              <div class="code-input-group">
                <input 
                  v-model="form.code" 
                  type="text" 
                  class="form-input"
                  placeholder="请输入邮箱收到的验证码"
                  required
                />
                <button 
                  type="button" 
                  class="btn btn-secondary btn-code"
                  @click="resendCode"
                  :disabled="sendingCode || codeSent"
                >
                  {{ codeSent ? `${countdown}s` : (sendingCode ? '发送中...' : '重新发送') }}
                </button>
              </div>
            </div>
            
            <div class="form-group">
              <label class="form-label">新密码</label>
              <input 
                v-model="form.newPassword" 
                type="password" 
                class="form-input"
                placeholder="请输入新密码（至少8位）"
                required
                minlength="8"
                @input="updateStrength"
              />
              <div v-if="form.newPassword" class="strength-bar">
                <div class="strength-fill" :style="{ width: strengthPercent + '%', background: strengthColor }"></div>
              </div>
              <span class="form-hint">强度：<span :style="{ color: strengthColor, fontWeight: 600 }">{{ strengthText }}</span></span>
            </div>
            
            <div class="form-group">
              <label class="form-label">确认新密码</label>
              <input 
                v-model="form.confirmPassword" 
                type="password" 
                class="form-input"
                placeholder="请再次输入新密码"
                required
              />
            </div>
            
            <button type="submit" class="btn btn-primary btn-block" :disabled="loading">
              <span v-if="loading" class="loading-spinner"></span>
              <span v-else>重置密码</span>
            </button>
          </template>
          
          <!-- 步骤3：成功 -->
          <template v-if="step === 3">
            <div class="success-message">
              <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                <polyline points="22 4 12 14.01 9 11.01"/>
              </svg>
              <h3>密码重置成功</h3>
              <p>请使用新密码登录</p>
            </div>
            
            <router-link to="/login" class="btn btn-primary btn-block">
              立即登录
            </router-link>
          </template>
        </form>
        
        <div class="auth-footer">
          <p>想起密码了？<router-link to="/login">返回登录</router-link></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onUnmounted } from 'vue'
import { userApi } from '../../api/user'
import { useLogger } from '../../utils/logger'
import { toast } from '../../utils/toast'
import { checkPasswordStrength } from '../../utils'

const logger = useLogger('PasswordReset')

const step = ref(1)
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
  strengthResult.value = checkPasswordStrength(form.newPassword)
}

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
})

const form = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const resendCode = async () => {
  const emailTrimmed = form.email.trim()
  if (!emailTrimmed) {
    toast.error('邮箱为空')
    return
  }
  if (countdownTimer) clearInterval(countdownTimer)
  sendingCode.value = true
  try {
    await userApi.sendCode(emailTrimmed)
    codeSent.value = true
    countdown.value = 60
    countdownTimer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(countdownTimer)
        codeSent.value = false
      }
    }, 1000)
    toast.success('验证码已重新发送')
  } catch (error) {
    toast.error('发送验证码失败')
  } finally {
    sendingCode.value = false
  }
}

const handleSubmit = async () => {
  if (step.value === 1) {
    // 发送验证码
    const emailTrimmed = form.email.trim()
    if (!emailTrimmed) {
      toast.error('请输入邮箱')
      return
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailTrimmed)) {
      toast.error('请输入有效的邮箱地址')
      return
    }

    loading.value = true
    try {
      await userApi.sendCode(emailTrimmed)
      logger.info('Verification code sent', { email: emailTrimmed })
      toast.success('验证码已发送至邮箱')
      codeSent.value = true
      countdown.value = 60
      countdownTimer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(countdownTimer)
          codeSent.value = false
        }
      }, 1000)
      step.value = 2
    } catch (error) {
      logger.error('Failed to send code', { error: error.message })
      toast.error(error.response?.data?.message || '发送验证码失败')
    } finally {
      loading.value = false
    }
  } else if (step.value === 2) {
    // 重置密码
    const codeTrimmed = form.code.trim()
    const newPasswordTrimmed = form.newPassword.trim()
    const confirmPasswordTrimmed = form.confirmPassword.trim()

    if (!codeTrimmed) {
      toast.error('请输入验证码')
      return
    }

    if (!newPasswordTrimmed) {
      toast.error('请输入新密码')
      return
    }

    if (newPasswordTrimmed.length < 8) {
      toast.error('密码长度至少8位')
      return
    }

    if (newPasswordTrimmed !== confirmPasswordTrimmed) {
      toast.error('两次输入的密码不一致')
      return
    }

    loading.value = true
    try {
      await userApi.resetPassword({
        email: form.email.trim(),
        code: codeTrimmed,
        password: newPasswordTrimmed
      })
      logger.info('Password reset successful')
      toast.success('密码重置成功')
      step.value = 3
    } catch (error) {
      logger.error('Failed to reset password', { error: error.message })
      toast.error(error.response?.data?.message || '重置密码失败')
    } finally {
      loading.value = false
    }
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

.btn-block {
  width: 100%;
  padding: 0.75rem;
  font-size: 1rem;
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

.success-message {
  text-align: center;
  padding: var(--spacing-xl) 0;
}

.success-message svg {
  color: var(--success);
  margin-bottom: var(--spacing-md);
}

.success-message h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-sm);
}

.success-message p {
  font-size: 0.875rem;
  color: var(--text-secondary);
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
