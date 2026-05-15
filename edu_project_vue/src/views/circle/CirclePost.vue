<template>
  <div class="circle-post-page">
    <div class="post-container">
      <div class="post-card card">
        <div class="card-header">
          <h2>发布动态</h2>
        </div>
        <div class="card-body">
          <div class="create-top">
            <img :src="userStore.avatar || '/default-avatar.png'" class="user-avatar" />
            <div class="create-info">
              <span class="create-nickname">{{ userStore.nickname }}</span>
              <button class="visibility-selector" @click="showPicker = !showPicker">
                {{ visibilityOptions[form.visibility] }}
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>
              </button>
              <div v-if="showPicker" class="visibility-dropdown">
                <div v-for="(label, key) in visibilityOptions" :key="key" class="vis-item" :class="{ active: form.visibility === Number(key) }" @click="form.visibility = Number(key); showPicker = false">
                  <span>{{ ['🌏', '👥', '🔒'][key] }}</span>
                  <div>
                    <div class="vis-label">{{ label }}</div>
                    <div class="vis-desc">{{ ['所有人可见', '仅关注的粉丝可见', '只有自己可见'][key] }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <textarea v-model="form.content" class="post-textarea" placeholder="分享你的校园生活..." rows="6" maxlength="2000"></textarea>
          <div class="char-count" :class="{ warn: form.content.length > 1800 }">{{ form.content.length }}/2000</div>

          <div v-if="form.images.length" class="uploaded-images">
            <div v-for="(img, idx) in form.images" :key="idx" class="image-item">
              <img :src="img" alt="" />
              <button class="remove-image" @click="form.images.splice(idx, 1)">✕</button>
            </div>
          </div>

          <div v-if="form.videos.length" class="uploaded-videos">
            <div v-for="(video, idx) in form.videos" :key="idx" class="video-item">
              <video :src="video" class="video-preview" muted preload="metadata"></video>
              <button class="remove-video" @click="form.videos.splice(idx, 1)">✕</button>
            </div>
          </div>

          <div v-if="uploading" class="upload-progress-bar">
            <div class="progress-fill" :style="{ width: uploadPercent + '%' }"></div>
          </div>

          <div class="toolbar">
            <input type="file" accept="image/jpeg,image/png,image/gif,image/webp" multiple @change="handleUpload" ref="fileInput" hidden />
            <input type="file" accept="video/mp4,video/webm" @change="handleVideoUpload" ref="videoInput" hidden />
            <button class="tool-btn" @click="$refs.fileInput.click()" :disabled="form.images.length >= 9">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
              图片
            </button>
            <button class="tool-btn" @click="$refs.videoInput.click()">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="23 7 16 12 23 17 23 7"/><rect x="1" y="5" width="15" height="14" rx="2"/></svg>
              视频
            </button>
          </div>
        </div>
        <div class="card-footer">
          <button class="btn btn-ghost" @click="goBack">取消</button>
          <button class="btn btn-primary" @click="publishPost" :disabled="!form.content.trim() || publishing">
            {{ publishing ? '发布中...' : '发布' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { circleApi } from '../../api/circle'
import { mediaApi } from '../../api/media'
import { useUserStore } from '../../stores/user'
import { useLogger } from '../../utils/logger'
import { toast } from '../../utils/toast'

const router = useRouter()
const userStore = useUserStore()
const logger = useLogger('CirclePost')

const MAX_IMAGE_SIZE = 10 * 1024 * 1024
const MAX_VIDEO_SIZE = 100 * 1024 * 1024

const publishing = ref(false)
const uploading = ref(false)
const uploadPercent = ref(0)
const showPicker = ref(false)
const fileInput = ref(null)
const videoInput = ref(null)

const visibilityOptions = { 0: '公开', 1: '关注者可见', 2: '仅自己' }

const form = reactive({
  content: '',
  images: [],
  videos: [],
  visibility: 0,
  tags: []
})

const handleUpload = async (e) => {
  const files = Array.from(e.target.files)
  uploading.value = true
  uploadPercent.value = 0
  try {
    for (const file of files) {
      if (file.size > MAX_IMAGE_SIZE) {
        toast.warning(`图片 ${file.name} 超过10MB限制`)
        continue
      }
      if (form.images.length >= 9) break
      const res = await mediaApi.uploadFile(file, 'circle', (pe) => {
        if (pe.total) uploadPercent.value = Math.round((pe.loaded / pe.total) * 100)
      })
      form.images.push(res.data.fileUrl)
    }
    toast.success('上传完成')
  } catch (err) {
    logger.error('upload error', { error: err.message })
    toast.error('上传失败')
  } finally {
    uploading.value = false
    uploadPercent.value = 0
    if (fileInput.value) fileInput.value.value = ''
  }
}

const handleVideoUpload = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  if (file.size > MAX_VIDEO_SIZE) {
    toast.warning('视频文件不能超过100MB')
    return
  }
  uploading.value = true
  uploadPercent.value = 0
  try {
    const res = await mediaApi.uploadFile(file, 'circle', (pe) => {
      if (pe.total) uploadPercent.value = Math.round((pe.loaded / pe.total) * 100)
    })
    form.videos.push(res.data.fileUrl)
    toast.success('视频上传成功')
  } catch (err) {
    logger.error('upload video error', { error: err.message })
    toast.error('视频上传失败')
  } finally {
    uploading.value = false
    uploadPercent.value = 0
    if (videoInput.value) videoInput.value.value = ''
  }
}

const publishPost = async () => {
  if (!form.content.trim()) return
  publishing.value = true
  try {
    await circleApi.createPost({
      content: form.content,
      images: form.images,
      videos: form.videos,
      visibility: form.visibility,
      tags: form.tags,
      allowComment: 1,
      allowRepost: 1
    })
    toast.success('发布成功')
    router.push('/circle')
  } catch (err) {
    logger.error('publish error', { error: err.message })
    toast.error(err.response?.data?.message || '发布失败')
  } finally {
    publishing.value = false
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  document.title = '发布动态 - 校友圈'
})
</script>

<style scoped>
.circle-post-page {
  max-width: 600px;
  margin: 0 auto;
  padding: 16px;
}

.post-card {
  border-radius: 16px;
  overflow: hidden;
}

.card-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.card-header h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.card-body {
  padding: 20px;
}

.create-top {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.create-info {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.create-nickname {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
}

.visibility-selector {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  background: #f5f5f5;
  border: none;
  border-radius: 20px;
  font-size: 12px;
  color: #666;
  cursor: pointer;
  transition: background 0.2s;
}

.visibility-selector:hover {
  background: #e8e8e8;
}

.visibility-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  margin-top: 6px;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
  padding: 6px;
  z-index: 100;
  min-width: 200px;
}

.vis-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.vis-item:hover,
.vis-item.active {
  background: #f5f5ff;
}

.vis-label { font-size: 13px; font-weight: 500; }
.vis-desc { font-size: 11px; color: #999; margin-top: 2px; }

.post-textarea {
  width: 100%;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  padding: 12px;
  font-size: 15px;
  font-family: inherit;
  resize: vertical;
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.2s;
}

.post-textarea:focus {
  border-color: #4f46e5;
}

.char-count {
  text-align: right;
  font-size: 12px;
  color: #bbb;
  margin-top: 8px;
}

.char-count.warn {
  color: #f59e0b;
}

.uploaded-images {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin: 12px 0;
}

.image-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 10px;
  overflow: hidden;
  background: #f5f5f5;
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-image {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: rgba(0,0,0,0.5);
  color: #fff;
  border: none;
  font-size: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.remove-image:hover {
  background: rgba(239,68,68,0.8);
}

.uploaded-videos {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin: 12px 0;
}

.video-item {
  position: relative;
  border-radius: 10px;
  overflow: hidden;
  background: #000;
  max-width: 100%;
}

.video-preview {
  width: 100%;
  max-height: 200px;
  object-fit: contain;
  display: block;
}

.remove-video {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: rgba(0,0,0,0.5);
  color: #fff;
  border: none;
  font-size: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.remove-video:hover {
  background: rgba(239,68,68,0.8);
}

.upload-progress-bar {
  height: 4px;
  background: #eee;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 12px;
}

.progress-fill {
  height: 100%;
  background: #4f46e5;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.toolbar {
  display: flex;
  gap: 8px;
}

.tool-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  background: #f5f5f5;
  border: none;
  border-radius: 20px;
  color: #666;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
}

.tool-btn:hover {
  background: #e8e8ff;
  color: #4f46e5;
}

.tool-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 14px 20px;
  border-top: 1px solid #f0f0f0;
}

.btn-primary {
  padding: 8px 24px;
  background: #4f46e5;
  color: #fff;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary:hover {
  background: #4338ca;
  transform: translateY(-1px);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.btn-ghost {
  padding: 8px 20px;
  background: transparent;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  color: #666;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-ghost:hover {
  background: #f5f5f5;
}

@media (max-width: 600px) {
  .circle-post-page { padding: 12px; }
}
</style>
