<template>
  <div class="profile-edit-page">
    <div class="edit-card card">
      <div class="edit-header">
        <h2>编辑个人资料</h2>
        <router-link to="/profile" class="btn btn-ghost btn-sm">返回主页</router-link>
      </div>

      <form class="edit-form" @submit.prevent="handleSubmit">
        <div class="form-group">
          <label class="form-label">头像</label>
          <div class="avatar-upload" @click="triggerAvatarUpload">
            <div class="avatar-preview-wrapper">
              <img :src="getSafeImageUrl(form.avatar, '/default-avatar.png')" alt="头像" class="avatar-preview" />
              <div class="avatar-overlay">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/><circle cx="12" cy="13" r="4"/></svg>
                <span>{{ uploading ? '上传中...' : '更换头像' }}</span>
              </div>
            </div>
            <input type="file" accept="image/jpeg,image/png,image/gif,image/webp" @change="handleAvatarUpload" ref="avatarInput" hidden />
            <p class="upload-tip">支持 jpg、png、gif、webp，最大 10MB</p>
          </div>
        </div>

        <div class="form-group float-label-group">
          <input v-model="form.nickname" type="text" class="form-input" placeholder=" " maxlength="20" />
          <label class="float-label">昵称</label>
          <span class="char-count-top">{{ (form.nickname || '').length }}/20</span>
        </div>

        <div class="form-group float-label-group">
          <textarea v-model="form.bio" class="form-input" placeholder=" " rows="4" maxlength="200"></textarea>
          <label class="float-label">个人简介</label>
          <span class="char-count-bottom">{{ (form.bio || '').length }}/200</span>
        </div>

        <div class="form-group float-label-group">
          <input :value="userStore.user?.email" type="email" class="form-input" disabled placeholder=" " />
          <label class="float-label">邮箱</label>
          <span class="form-hint">邮箱不可修改</span>
        </div>

        <div class="form-actions">
          <button type="submit" class="btn btn-primary" :disabled="loading || uploading">
            <template v-if="loading">
              <span class="btn-spinner"></span>
              保存中...
            </template>
            <template v-else>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/><polyline points="17 21 17 13 7 13 7 21"/><polyline points="7 3 7 8 15 8"/></svg>
              保存修改
            </template>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { userApi } from '../../api/user'
import { mediaApi } from '../../api/media'
import { useLogger } from '../../utils/logger'
import { getSafeImageUrl } from '../../utils'
import { toast } from '../../utils/toast'

const router = useRouter()
const userStore = useUserStore()
const logger = useLogger('ProfileEdit')

const loading = ref(false)
const uploading = ref(false)
const avatarInput = ref(null)
const form = reactive({
  nickname: '',
  bio: '',
  avatar: ''
})

function triggerAvatarUpload() {
  if (!uploading.value) {
    avatarInput.value?.click()
  }
}

onMounted(() => {
  if (userStore.user) {
    form.nickname = userStore.user.nickname || ''
    form.bio = userStore.user.bio || ''
    form.avatar = userStore.user.avatar || ''
  }
})

async function handleAvatarUpload(event) {
  const file = event.target.files[0]
  if (!file) return

  uploading.value = true
  try {
    const response = await mediaApi.uploadFile(file, 'avatar')
    form.avatar = response.data.fileUrl
    toast.success('头像上传成功')
  } catch (error) {
    logger.error('Failed to upload avatar', { error: error.message })
    toast.error(error.response?.data?.message || '上传头像失败，请重试')
  } finally {
    uploading.value = false
  }
}

async function handleSubmit() {
  if (!form.nickname.trim()) {
    toast.warning('请输入昵称')
    return
  }

  loading.value = true
  try {
    await userStore.updateProfile({
      nickname: form.nickname,
      bio: form.bio
    })

    if (form.avatar && form.avatar !== (userStore.user?.avatar || '')) {
      await userApi.updateAvatar(form.avatar)
      userStore.updateAvatar(form.avatar)
    }

    toast.success('保存成功')
    router.push('/profile')
  } catch (error) {
    logger.error('Failed to update profile', { error: error.message })
    toast.error(error.response?.data?.message || '保存失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.profile-edit-page {
  max-width: 560px;
  margin: 0 auto;
  padding: 24px;
}

.edit-card {
  padding: 32px;
}

.edit-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
}

.edit-header h2 {
  font-size: 1.25rem;
  font-weight: 700;
}

.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.avatar-preview-wrapper {
  position: relative;
  width: 96px;
  height: 96px;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: var(--shadow-md);
  transition: transform 0.2s;
}

.avatar-preview-wrapper:hover {
  transform: scale(1.05);
}

.avatar-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: white;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.avatar-preview-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay svg {
  width: 22px;
  height: 22px;
}

.avatar-overlay span {
  font-size: 0.6875rem;
  font-weight: 500;
}

.upload-tip {
  font-size: 0.75rem;
  color: var(--text-muted);
  text-align: center;
  margin: 0;
}

.float-label-group {
  position: relative;
  margin-bottom: 20px;
}

.float-label-group .form-input {
  padding: 20px 14px 6px;
  border-radius: var(--radius);
}

.float-label-group textarea.form-input {
  padding: 22px 14px 10px;
  min-height: 100px;
}

.float-label {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 0.875rem;
  color: var(--text-muted);
  pointer-events: none;
  transition: all 0.2s ease;
  background: var(--surface);
  padding: 0 4px;
}

.float-label-group textarea ~ .float-label {
  top: 22px;
  transform: none;
}

.float-label-group .form-input:focus ~ .float-label,
.float-label-group .form-input:not(:placeholder-shown) ~ .float-label,
.float-label-group .form-input:disabled ~ .float-label {
  top: 6px;
  transform: none;
  font-size: 0.6875rem;
  color: var(--primary);
}

.float-label-group .form-input:disabled ~ .float-label {
  color: var(--text-muted);
}

.char-count-top {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 0.6875rem;
  color: var(--text-muted);
  pointer-events: none;
}

.char-count-bottom {
  display: block;
  text-align: right;
  font-size: 0.6875rem;
  color: var(--text-muted);
  margin-top: 4px;
}

.form-hint {
  display: block;
  font-size: 0.6875rem;
  color: var(--text-muted);
  margin-top: 4px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px solid var(--border);
}

.btn-spinner {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid transparent;
  border-top-color: currentColor;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .profile-edit-page {
    padding: 16px;
  }

  .edit-card {
    padding: 20px;
  }
}
</style>
