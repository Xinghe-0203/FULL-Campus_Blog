<template>
  <div class="circle-page">
    <div class="circle-container">
      <button class="back-btn" @click="router.back()">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
        返回
      </button>
      <div class="create-post card" v-if="userStore.isLoggedIn" @click="openCreateModal">
        <div class="create-header">
          <img :src="userStore.avatar || '/default-avatar.png'" :alt="userStore.nickname" class="user-avatar" />
          <div class="create-input">分享你的校园生活...</div>
        </div>
      </div>

      <div class="feed-tabs">
        <button class="tab-btn" :class="{ active: activeTab === 'recommend' }" @click="switchTab('recommend')">推荐</button>
        <button v-if="userStore.isLoggedIn" class="tab-btn" :class="{ active: activeTab === 'following' }" @click="switchTab('following')">关注</button>
      </div>

      <div class="feed-list" ref="feedListRef">
        <div v-if="loading && posts.length === 0" class="loading-container">
          <div v-for="i in 3" :key="i" class="feed-skeleton card">
            <div class="skeleton-header">
              <div class="skeleton-avatar"></div>
              <div class="skeleton-info">
                <div class="skeleton-name"></div>
                <div class="skeleton-time"></div>
              </div>
            </div>
            <div class="skeleton-content">
              <div class="skeleton-text"></div>
              <div class="skeleton-text short"></div>
              <div class="skeleton-images">
                <div class="skeleton-img"></div>
                <div class="skeleton-img"></div>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="error" class="error-state">
          <svg width="60" height="60" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          <h3>加载失败</h3>
          <p>{{ error }}</p>
          <button class="btn btn-primary" @click="retryLoad">重新加载</button>
        </div>

        <div v-else-if="posts.length === 0" class="empty-state">
          <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
          <h3 class="empty-state-title">{{ activeTab === 'following' ? '还没有关注的内容' : '还没有动态' }}</h3>
          <p class="empty-state-text">{{ activeTab === 'following' ? '去发现更多有趣的人吧' : '快来说点什么吧' }}</p>
        </div>

        <div v-else class="posts">
          <div v-for="(post, index) in posts" :key="post.id" class="feed-item card" :style="{ animationDelay: `${index * 0.05}s` }">
            <div class="feed-header">
              <router-link :to="`/user/${post.userId}`" class="feed-author">
                <img :src="post.userAvatar || '/default-avatar.png'" :alt="post.userUsername" class="author-avatar" />
                <div class="author-info">
                  <span class="author-name">{{ post.userNickname || post.userUsername }}</span>
                  <span class="feed-time">{{ formatRelativeTime(post.createTime) }}</span>
                </div>
              </router-link>
              <span class="visibility-badge" :title="visibilityLabel(post.visibility)">
                <span v-if="post.visibility === 0">🌏</span>
                <span v-else-if="post.visibility === 1">👥</span>
                <span v-else>🔒</span>
              </span>
            </div>

            <div class="feed-content" @click="router.push(`/circle/${post.id}`)">
              <p class="feed-text">{{ post.content }}</p>

              <div v-if="post.images && post.images.length" class="feed-images" :class="`grid-${Math.min(post.images.length, 9)}`">
                <div v-for="(image, idx) in post.images" :key="idx" class="img-wrap" :class="{ 'is-video': isVideo(image) }" @click.stop="openImagePreview(post.images, idx)">
                  <img v-if="!isVideo(image)" :src="image" alt="动态图片" class="feed-image" loading="lazy" />
                  <video v-else :src="image" class="feed-image" muted @click.stop.prevent="playVideo(image)"></video>
                  <span v-if="isVideo(image)" class="play-icon">▶</span>
                </div>
              </div>

              <div v-if="post.videos && post.videos.length" class="feed-videos">
                <video v-for="(video, idx) in post.videos" :key="'v-'+idx" :src="video" class="feed-video" controls muted preload="metadata"></video>
              </div>

              <div v-if="post.repostPost" class="repost-card" @click.stop="router.push(`/circle/${post.repostPost.id}`)">
                <div class="repost-header">
                  <img :src="post.repostPost.userAvatar || '/default-avatar.png'" class="repost-avatar" />
                  <span class="repost-author">{{ post.repostPost.userNickname || post.repostPost.userUsername }}</span>
                </div>
                <p class="repost-text">{{ post.repostPost.content }}</p>
                <div v-if="post.repostPost.images && post.repostPost.images.length" class="repost-images mini">
                  <img v-for="(img, idx) in post.repostPost.images.slice(0, 3)" :key="idx" :src="img" class="repost-img" />
                  <span v-if="post.repostPost.images.length > 3" class="repost-more">+{{ post.repostPost.images.length - 3 }}</span>
                </div>
              </div>
            </div>

            <div class="feed-actions">
              <button class="action-btn" :class="{ liked: post.isLiked }" @click="toggleLike(post)">
                <svg class="like-icon" :class="{ 'animate-pop': post.likeAnim }" width="18" height="18" viewBox="0 0 24 24" :fill="post.isLiked ? '#ef4444' : 'none'" stroke="currentColor" stroke-width="2">
                  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                </svg>
                <span>{{ formatNumber(post.likeCount) }}</span>
              </button>
              <button class="action-btn" @click="router.push(`/circle/${post.id}`)">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                </svg>
                <span>{{ formatNumber(post.commentCount) }}</span>
              </button>
              <button class="action-btn" @click="openRepostModal(post)">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/>
                  <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/>
                </svg>
                <span>{{ formatNumber(post.repostCount) }}</span>
              </button>
            </div>
          </div>

          <div ref="sentinelRef" class="scroll-sentinel">
            <div v-if="loadingMore" class="loading-spinner">
              <div class="spinner"></div>
              <span>加载更多...</span>
            </div>
            <div v-else-if="!hasMore" class="no-more">没有更多了</div>
          </div>
        </div>
      </div>
    </div>

    <teleport to="body">
      <transition name="modal">
        <div v-if="showCreateModal" class="modal-overlay" @click.self="showCreateModal = false">
          <div class="modal-content card">
            <div class="modal-header">
              <h3>发布动态</h3>
              <button class="close-btn" @click="showCreateModal = false">✕</button>
            </div>
            <div class="modal-body">
              <div class="create-top">
                <img :src="userStore.avatar || '/default-avatar.png'" class="modal-avatar" />
                <div class="create-info">
                  <span class="create-nickname">{{ userStore.nickname }}</span>
                  <button class="visibility-selector" @click="showVisibilityPicker = !showVisibilityPicker">
                    {{ visibilityOptions[newPost.visibility] }}
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>
                  </button>
                  <div v-if="showVisibilityPicker" class="visibility-dropdown">
                    <div v-for="(label, key) in visibilityOptions" :key="key" class="vis-item" :class="{ active: newPost.visibility === Number(key) }" @click="newPost.visibility = Number(key); showVisibilityPicker = false">
                      <span>{{ ['🌏', '👥', '🔒'][key] }}</span>
                      <div><div class="vis-label">{{ label }}</div><div class="vis-desc">{{ visibilityDescriptions[key] }}</div></div>
                    </div>
                  </div>
                </div>
              </div>
              <textarea v-model="newPost.content" class="post-textarea" placeholder="分享你的校园生活..." rows="4" @input="autoResize" ref="textareaRef" maxlength="2000"></textarea>
              <div class="char-count" :class="{ warn: newPost.content.length > 1800 }">{{ newPost.content.length }}/2000</div>

              <div v-if="newPost.images.length" class="uploaded-images">
                <div v-for="(img, idx) in newPost.images" :key="idx" class="image-item">
                  <img :src="img" alt="" />
                  <button class="remove-image" @click="newPost.images.splice(idx, 1)">✕</button>
                </div>
              </div>

              <div class="toolbar">
                <input type="file" accept="image/jpeg,image/png,image/gif,image/webp" multiple @change="handleImageUpload" ref="imageInput" hidden />
                <input type="file" accept="video/mp4,video/webm" @change="handleVideoUpload" ref="videoInput" hidden />
                <button class="tool-btn" title="图片" @click="$refs.imageInput.click()" :disabled="newPost.images.length >= 9">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
                  <span>图片</span>
                </button>
                <button class="tool-btn" title="视频" @click="$refs.videoInput.click()">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="23 7 16 12 23 17 23 7"/><rect x="1" y="5" width="15" height="14" rx="2"/></svg>
                  <span>视频</span>
                </button>
                <div class="tool-right">
                  <button class="btn btn-primary btn-sm" @click="publishPost" :disabled="!newPost.content.trim() || publishing">
                    {{ publishing ? '发布中...' : '发布' }}
                  </button>
                </div>
              </div>
              <div v-if="uploading" class="upload-progress-bar">
                <div class="progress-fill" :style="{ width: uploadPercent + '%' }"></div>
                <span>{{ uploadPercent }}%</span>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </teleport>

    <teleport to="body">
      <transition name="modal">
        <div v-if="showRepostModal" class="modal-overlay" @click.self="closeRepostModal">
          <div class="modal-content card">
            <div class="modal-header">
              <h3>转发动态</h3>
              <button class="close-btn" @click="closeRepostModal">✕</button>
            </div>
            <div class="modal-body">
              <div v-if="currentRepostPost" class="repost-original-card">
                <div class="repost-original-header">
                  <img :src="currentRepostPost.userAvatar || '/default-avatar.png'" class="repost-original-avatar" />
                  <span class="repost-original-name">{{ currentRepostPost.userNickname || currentRepostPost.userUsername }}</span>
                </div>
                <p class="repost-original-text">{{ currentRepostPost.content }}</p>
              </div>
              <textarea v-model="repostContent" class="post-textarea" placeholder="说说你的看法..." rows="4" maxlength="2000"></textarea>
              <div class="char-count" :class="{ warn: repostContent.length > 1800 }">{{ repostContent.length }}/2000</div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-ghost" @click="closeRepostModal">取消</button>
              <button class="btn btn-primary" @click="confirmRepost" :disabled="reposting">
                {{ reposting ? '转发中...' : '转发' }}
              </button>
            </div>
          </div>
        </div>
      </transition>
    </teleport>

    <teleport to="body">
      <transition name="modal">
        <div v-if="showPreview" class="modal-overlay preview-overlay" @click.self="showPreview = false">
          <button class="preview-nav prev" @click="previewPrev" v-if="previewImages.length > 1">‹</button>
          <img :src="previewImages[previewIndex]" class="preview-image" @click.stop />
          <button class="preview-nav next" @click="previewNext" v-if="previewImages.length > 1">›</button>
          <button class="preview-close" @click="showPreview = false">✕</button>
          <div class="preview-counter" v-if="previewImages.length > 1">{{ previewIndex + 1 }} / {{ previewImages.length }}</div>
        </div>
      </transition>
    </teleport>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, nextTick, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { circleApi } from '../../api/circle'
import { mediaApi } from '../../api/media'
import { useUserStore } from '../../stores/user'
import { formatRelativeTime, formatNumber } from '../../utils'
import { useLogger } from '../../utils/logger'
import { toast } from '../../utils/toast'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const logger = useLogger('Circle')

const loading = ref(false)
const loadingMore = ref(false)
const posts = ref([])
const activeTab = ref('recommend')
const currentPage = ref(1)
const hasMore = ref(true)
const error = ref('')

const showCreateModal = ref(false)
const publishing = ref(false)
const uploading = ref(false)
const uploadPercent = ref(0)
const showVisibilityPicker = ref(false)

const showRepostModal = ref(false)
const repostContent = ref('')
const currentRepostPost = ref(null)
const reposting = ref(false)

const showPreview = ref(false)
const previewImages = ref([])
const previewIndex = ref(0)

const sentinelRef = ref(null)
const feedListRef = ref(null)
const textareaRef = ref(null)
const imageInput = ref(null)
const videoInput = ref(null)

const visibilityOptions = { 0: '公开', 1: '关注者可见', 2: '仅自己' }
const visibilityDescriptions = { 0: '所有人可见', 1: '仅关注的粉丝可见', 2: '只有自己可见' }

const newPost = reactive({
  content: '',
  images: [],
  videos: [],
  visibility: 0,
  tags: []
})

const isVideo = (url) => {
  if (!url) return false
  const exts = ['.mp4', '.webm', '.mov', '.avi', '.mkv']
  const lower = url.toLowerCase()
  return exts.some(e => lower.includes(e)) || lower.includes('video')
}

const visibilityLabel = (v) => {
  if (v === 0) return '公开'
  if (v === 1) return '关注者可见'
  return '仅自己可见'
}

const switchTab = (tab) => {
  activeTab.value = tab
}

const fetchPosts = async (reset = false) => {
  if (loading.value && !reset) return
  if (reset) {
    currentPage.value = 1
    hasMore.value = true
    error.value = ''
  }
  loading.value = true
  try {
    const fn = activeTab.value === 'following' ? circleApi.getFollowingFeed : circleApi.getRecommendFeed
    const res = await fn({ pageNum: currentPage.value, pageSize: 20 })
    const list = Array.isArray(res.data) ? res.data : (res.data?.records || [])
    const mapped = list.map(p => ({ ...p, isLiked: p.isLiked || false, likeAnim: false }))
    posts.value = reset ? mapped : [...posts.value, ...mapped]
    hasMore.value = list.length >= 20
  } catch (err) {
    logger.error('fetchPosts error', { error: err.message })
    error.value = err.response?.data?.message || err.message || '加载失败'
    if (reset) posts.value = []
  } finally {
    loading.value = false
  }
}

const retryLoad = () => fetchPosts(true)

const loadMore = async () => {
  if (loadingMore.value || loading.value || !hasMore.value) return
  loadingMore.value = true
  currentPage.value++
  try {
    await fetchPosts()
  } catch {
    currentPage.value--
    hasMore.value = true
  } finally {
    loadingMore.value = false
  }
}

watch(activeTab, () => fetchPosts(true))

let observer = null
const setupObserver = () => {
  if (!sentinelRef.value) return
  observer = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting) loadMore()
  }, { rootMargin: '200px' })
  observer.observe(sentinelRef.value)
}

watch(() => posts.value.length, () => {
  nextTick(() => {
    if (observer) observer.disconnect()
    setupObserver()
  })
})

const toggleLike = async (post) => {
  if (!userStore.isLoggedIn) { toast.warning('请先登录'); return }
  const prev = post.isLiked
  post.isLiked = !post.isLiked
  post.likeCount += post.isLiked ? 1 : -1
  post.likeAnim = true
  setTimeout(() => post.likeAnim = false, 400)
  try {
    await circleApi.toggleLike(post.id)
  } catch (err) {
    post.isLiked = prev
    post.likeCount += post.isLiked ? 1 : -1
    logger.error('toggleLike error', { error: err.message })
  }
}

const openRepostModal = (post) => {
  if (!userStore.isLoggedIn) { toast.warning('请先登录'); return }
  currentRepostPost.value = post
  repostContent.value = ''
  showRepostModal.value = true
}

const closeRepostModal = () => {
  showRepostModal.value = false
  currentRepostPost.value = null
  repostContent.value = ''
}

const confirmRepost = async () => {
  if (!currentRepostPost.value) return
  reposting.value = true
  try {
    await circleApi.repost(currentRepostPost.value.id, repostContent.value)
    closeRepostModal()
    toast.success('转发成功')
    fetchPosts(true)
  } catch (err) {
    logger.error('repost error', { error: err.message })
    toast.error(err.response?.data?.message || '转发失败')
  } finally {
    reposting.value = false
  }
}

const openImagePreview = (images, idx) => {
  previewImages.value = images
  previewIndex.value = idx
  showPreview.value = true
}

const previewPrev = () => {
  previewIndex.value = (previewIndex.value - 1 + previewImages.value.length) % previewImages.value.length
}

const previewNext = () => {
  previewIndex.value = (previewIndex.value + 1) % previewImages.value.length
}

const playVideo = (url) => {
  window.open(url, '_blank')
}

const MAX_IMAGE_SIZE = 10 * 1024 * 1024
const MAX_VIDEO_SIZE = 100 * 1024 * 1024

const handleImageUpload = async (e) => {
  const files = Array.from(e.target.files)
  uploading.value = true
  uploadPercent.value = 0
  let uploadedCount = 0
  try {
    for (const file of files) {
      if (file.size > MAX_IMAGE_SIZE) {
        toast.warning(`图片 ${file.name} 超过10MB限制`)
        continue
      }
      if (newPost.images.length >= 9) break
      const res = await mediaApi.uploadFile(file, 'circle', (pe) => {
        if (pe.total) uploadPercent.value = Math.round((pe.loaded / pe.total) * 100)
      })
      newPost.images.push(res.data.fileUrl)
      uploadedCount++
    }
    if (uploadedCount > 0) toast.success('上传完成')
  } catch (err) {
    logger.error('upload image error', { error: err.message })
    toast.error(err.response?.data?.message || '图片上传失败')
  } finally {
    uploading.value = false
    uploadPercent.value = 0
    if (imageInput.value) imageInput.value.value = ''
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
    newPost.videos.push(res.data.fileUrl)
    toast.success('视频上传成功')
  } catch (err) {
    logger.error('upload video error', { error: err.message })
    toast.error(err.response?.data?.message || '视频上传失败')
  } finally {
    uploading.value = false
    uploadPercent.value = 0
    if (videoInput.value) videoInput.value.value = ''
  }
}

const autoResize = () => {
  const el = textareaRef.value
  if (el) {
    el.style.height = 'auto'
    el.style.height = el.scrollHeight + 'px'
  }
}

const publishPost = async () => {
  if (!newPost.content.trim()) return
  publishing.value = true
  try {
    await circleApi.createPost({
      content: newPost.content,
      images: newPost.images,
      videos: newPost.videos,
      visibility: newPost.visibility,
      tags: newPost.tags,
      allowComment: 1,
      allowRepost: 1
    })
    showCreateModal.value = false
    newPost.content = ''
    newPost.images = []
    newPost.videos = []
    newPost.visibility = 0
    newPost.tags = []
    toast.success('发布成功')
    await fetchPosts(true)
  } catch (err) {
    logger.error('publish error', { error: err.message })
    toast.error(err.response?.data?.message || '发布失败')
  } finally {
    publishing.value = false
  }
}

const openCreateModal = () => {
  if (!userStore.isLoggedIn) {
    toast.warning('请先登录')
    router.push('/login')
    return
  }
  showCreateModal.value = true
  nextTick(() => autoResize())
}

onMounted(() => {
  fetchPosts(true)
  if (route.query.create === 'true') {
    nextTick(() => {
      openCreateModal()
      router.replace({ query: {} })
    })
  }
})

onBeforeUnmount(() => {
  if (observer) observer.disconnect()
})
</script>

<style scoped>
.back-btn { display: flex; align-items: center; gap: 4px; padding: 8px 12px; background: transparent; border: 1px solid var(--border); border-radius: 8px; color: var(--text-secondary); cursor: pointer; font-size: 0.875rem; transition: all 0.2s; width: fit-content; margin-bottom: 16px; }
.back-btn:hover { background: var(--border); color: var(--text-primary); }
.circle-page {
  max-width: 600px;
  margin: 0 auto;
  padding: 16px;
}

.create-post {
  padding: 14px 18px;
  margin-bottom: 14px;
  border-radius: 16px;
  cursor: pointer;
  transition: box-shadow 0.3s ease, transform 0.2s ease;
}

.create-post:hover {
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  transform: translateY(-1px);
}

.create-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.create-input {
  flex: 1;
  padding: 10px 16px;
  background: #f5f5f5;
  border-radius: 24px;
  color: #999;
  font-size: 14px;
  transition: background 0.3s;
}

.create-input:hover {
  background: #eee;
}

.feed-tabs {
  display: flex;
  gap: 4px;
  margin-bottom: 14px;
  background: #f5f5f5;
  border-radius: 12px;
  padding: 3px;
}

.tab-btn {
  flex: 1;
  padding: 8px 0;
  font-size: 14px;
  font-weight: 500;
  background: transparent;
  border: none;
  border-radius: 10px;
  color: #666;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tab-btn.active {
  background: #fff;
  color: #1a1a1a;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}

.tab-btn:hover:not(.active) {
  color: #333;
}

.feed-item {
  padding: 18px;
  margin-bottom: 14px;
  border-radius: 16px;
  transition: box-shadow 0.3s ease, transform 0.2s ease;
  animation: fadeUp 0.4s ease both;
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.feed-item:hover {
  box-shadow: 0 4px 24px rgba(0,0,0,0.08);
  transform: translateY(-1px);
}

.feed-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.feed-author {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}

.author-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  object-fit: cover;
}

.author-info {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-weight: 600;
  font-size: 15px;
  color: #1a1a1a;
}

.feed-time {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.visibility-badge {
  font-size: 16px;
  cursor: default;
}

.feed-content {
  margin-bottom: 12px;
  cursor: pointer;
}

.feed-text {
  font-size: 15px;
  color: #1a1a1a;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.feed-images {
  display: grid;
  gap: 4px;
  margin-top: 12px;
  border-radius: 12px;
  overflow: hidden;
}

.grid-1 { grid-template-columns: 1fr; max-width: 400px; }
.grid-2 { grid-template-columns: 1fr 1fr; }
.grid-3 { grid-template-columns: 1fr 1fr 1fr; }
.grid-4 { grid-template-columns: 1fr 1fr; }
.grid-5 { grid-template-columns: 1fr 1fr 1fr; }
.grid-6 { grid-template-columns: 1fr 1fr 1fr; }
.grid-7 { grid-template-columns: 1fr 1fr 1fr; }
.grid-8 { grid-template-columns: 1fr 1fr 1fr; }
.grid-9 { grid-template-columns: 1fr 1fr 1fr; }

.grid-4 .img-wrap:nth-child(1),
.grid-4 .img-wrap:nth-child(2) {
  grid-column: span 1;
}

.img-wrap {
  position: relative;
  aspect-ratio: 1;
  overflow: hidden;
  border-radius: 8px;
  background: #f0f0f0;
}

.img-wrap.is-video {
  cursor: pointer;
}

.feed-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.img-wrap:hover .feed-image {
  transform: scale(1.03);
}

.play-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 40px;
  height: 40px;
  background: rgba(0,0,0,0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  pointer-events: none;
}

.feed-videos {
  margin-top: 12px;
  border-radius: 12px;
  overflow: hidden;
}

.feed-video {
  width: 100%;
  max-height: 400px;
  object-fit: contain;
  background: #000;
  border-radius: 12px;
}

.repost-card {
  margin-top: 12px;
  padding: 14px;
  background: #f8f9fa;
  border-radius: 12px;
  border: 1px solid #eee;
  cursor: pointer;
  transition: background 0.2s;
}

.repost-card:hover {
  background: #f0f1f3;
}

.repost-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.repost-avatar {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  object-fit: cover;
}

.repost-author {
  font-size: 13px;
  font-weight: 500;
  color: #666;
}

.repost-text {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}

.repost-images.mini {
  display: flex;
  gap: 4px;
  margin-top: 8px;
}

.repost-img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
}

.repost-more {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eee;
  border-radius: 6px;
  font-size: 13px;
  color: #666;
}

.feed-actions {
  display: flex;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: transparent;
  border: none;
  border-radius: 20px;
  color: #666;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s ease;
}

.action-btn:hover {
  background: #f5f5f5;
  color: #ef4444;
}

.action-btn.liked {
  color: #ef4444;
}

.like-icon.animate-pop {
  animation: likePop 0.4s ease;
}

@keyframes likePop {
  0% { transform: scale(1); }
  25% { transform: scale(1.3); }
  50% { transform: scale(0.9); }
  100% { transform: scale(1); }
}

.skeleton-header {
  display: flex;
  gap: 12px;
  margin-bottom: 14px;
}

.skeleton-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: linear-gradient(90deg, #eee 25%, #f5f5f5 50%, #eee 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-info { flex: 1; }
.skeleton-name { width: 100px; height: 14px; background: #eee; border-radius: 4px; margin-bottom: 6px; animation: shimmer 1.5s infinite; background-size: 200% 100%; }
.skeleton-time { width: 60px; height: 12px; background: #eee; border-radius: 4px; animation: shimmer 1.5s infinite; background-size: 200% 100%; }

.skeleton-content { }
.skeleton-text { height: 14px; background: #eee; border-radius: 4px; margin-bottom: 8px; animation: shimmer 1.5s infinite; background-size: 200% 100%; }
.skeleton-text.short { width: 60%; }
.skeleton-images { display: flex; gap: 4px; margin-top: 12px; }
.skeleton-img { flex: 1; aspect-ratio: 1; background: #eee; border-radius: 8px; animation: shimmer 1.5s infinite; background-size: 200% 100%; }

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.error-state {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.error-state h3 { margin: 12px 0 6px; font-size: 16px; }
.error-state p { font-size: 13px; color: #999; margin-bottom: 16px; }

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #ccc;
}

.empty-state-title {
  margin: 16px 0 6px;
  font-size: 16px;
  color: #999;
}

.empty-state-text {
  font-size: 13px;
  color: #bbb;
}

.scroll-sentinel {
  text-align: center;
  padding: 20px 0;
}

.loading-spinner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #999;
  font-size: 13px;
}

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid #eee;
  border-top-color: #4f46e5;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.no-more {
  font-size: 13px;
  color: #ccc;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 20px;
}

.modal-content {
  width: 100%;
  max-width: 520px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  border-radius: 20px;
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h3 {
  font-size: 16px;
  font-weight: 600;
}

.close-btn {
  background: #f5f5f5;
  border: none;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.close-btn:hover {
  background: #e0e0e0;
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

.create-top {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.modal-avatar {
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
  border: none;
  font-size: 15px;
  line-height: 1.6;
  resize: none;
  color: #1a1a1a;
  background: transparent;
  font-family: inherit;
  min-height: 100px;
}

.post-textarea:focus {
  outline: none;
}

.post-textarea::placeholder {
  color: #bbb;
}

.char-count {
  text-align: right;
  font-size: 12px;
  color: #bbb;
  margin-bottom: 10px;
}

.char-count.warn {
  color: #f59e0b;
}

.uploaded-images {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 12px;
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
  transition: background 0.2s;
}

.remove-image:hover {
  background: rgba(239,68,68,0.8);
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tool-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
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

.tool-right {
  margin-left: auto;
}

.btn-primary {
  padding: 8px 20px;
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

.btn-primary.btn-sm {
  padding: 6px 16px;
  font-size: 13px;
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

.upload-progress-bar {
  margin-top: 10px;
  height: 4px;
  background: #eee;
  border-radius: 4px;
  position: relative;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #4f46e5;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.upload-progress-bar span {
  display: none;
}

.modal-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  padding: 14px 20px;
  border-top: 1px solid #f0f0f0;
}

.repost-original-card {
  padding: 14px;
  background: #f8f9fa;
  border-radius: 12px;
  margin-bottom: 14px;
  border: 1px solid #eee;
}

.repost-original-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.repost-original-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
}

.repost-original-name {
  font-size: 13px;
  font-weight: 500;
  color: #666;
}

.repost-original-text {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}

.preview-overlay {
  background: rgba(0,0,0,0.85);
  z-index: 3000;
  cursor: zoom-out;
}

.preview-image {
  max-width: 90vw;
  max-height: 85vh;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 8px 40px rgba(0,0,0,0.4);
}

.preview-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(255,255,255,0.15);
  border: none;
  color: #fff;
  font-size: 24px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.preview-nav:hover {
  background: rgba(255,255,255,0.3);
}

.preview-nav.prev { left: 20px; }
.preview-nav.next { right: 20px; }

.preview-close {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255,255,255,0.15);
  border: none;
  color: #fff;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.preview-close:hover {
  background: rgba(255,255,255,0.3);
}

.preview-counter {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  color: #fff;
  font-size: 14px;
  background: rgba(0,0,0,0.5);
  padding: 6px 16px;
  border-radius: 20px;
}

.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-active .modal-content,
.modal-leave-active .modal-content {
  transition: transform 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-content,
.modal-leave-to .modal-content {
  transform: scale(0.95);
}

@media (max-width: 600px) {
  .circle-page {
    padding: 12px;
  }
  .feed-item { padding: 14px; }
  .modal-content { max-height: 90vh; margin: 10px; border-radius: 16px; }
}
</style>
