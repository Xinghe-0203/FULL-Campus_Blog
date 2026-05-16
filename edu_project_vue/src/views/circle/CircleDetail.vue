<template>
  <div class="circle-detail-page">
    <div class="detail-container">
      <button class="back-btn" @click="router.back()">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
        返回
      </button>
      <div v-if="loading" class="loading-skeleton">
        <div class="skeleton-card card">
          <div class="skeleton-header">
            <div class="skeleton-avatar"></div>
            <div class="skeleton-info">
              <div class="skeleton-name"></div>
              <div class="skeleton-time"></div>
            </div>
          </div>
          <div class="skeleton-text"></div>
          <div class="skeleton-text short"></div>
        </div>
      </div>

      <div v-else-if="error" class="error-state">
        <svg width="60" height="60" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
        <h3>{{ error }}</h3>
        <button class="btn btn-primary" @click="fetchPost">重试</button>
      </div>

      <div v-else-if="post" class="detail-content">
        <div class="feed-card card">
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

          <div class="feed-content">
            <p class="feed-text">{{ post.content }}</p>

            <div v-if="post.topicNames && post.topicNames.length" class="topic-tags">
              <router-link v-for="tn in post.topicNames" :key="tn" :to="`/search?keyword=${'#' + tn}`" class="topic-tag-link">#{{ tn }}</router-link>
            </div>

            <div v-if="post.images && post.images.length" class="feed-images" :class="`grid-${Math.min(post.images.length, 9)}`">
              <div v-for="(image, idx) in post.images" :key="idx" class="img-wrap" :class="{ 'is-video': isVideo(image) }" @click="openImagePreview(post.images, idx)">
                <img v-if="!isVideo(image)" :src="image" alt="" class="feed-image" loading="lazy" />
                <video v-else :src="image" class="feed-image" muted></video>
                <span v-if="isVideo(image)" class="play-icon">▶</span>
              </div>
            </div>

            <div v-if="post.videos && post.videos.length" class="feed-videos">
              <video v-for="(video, idx) in post.videos" :key="'v-'+idx" :src="video" class="feed-video" controls preload="metadata"></video>
            </div>

            <div v-if="post.repostPost" class="repost-card" @click="router.push(`/circle/${post.repostPost.id}`)">
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

          <div class="feed-stats">
            <span>{{ formatDate(post.createTime) }}</span>
            <span v-if="post.likeCount || post.commentCount || post.repostCount">
              {{ post.likeCount || 0 }} 赞 · {{ post.commentCount || 0 }} 评论 · {{ post.repostCount || 0 }} 转发
            </span>
          </div>

          <div class="feed-actions">
            <button class="action-btn" :class="{ liked: isLiked }" @click="toggleLike">
              <svg class="like-icon" :class="{ 'animate-pop': likeAnim }" width="20" height="20" viewBox="0 0 24 24" :fill="isLiked ? '#ef4444' : 'none'" stroke="currentColor" stroke-width="2">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
              </svg>
              <span>{{ post.likeCount || 0 }}</span>
            </button>
            <button class="action-btn active-comment">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              </svg>
              <span>{{ post.commentCount || 0 }}</span>
            </button>
            <button class="action-btn" @click="openRepostModal">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/>
                <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/>
              </svg>
              <span>{{ post.repostCount || 0 }}</span>
            </button>
          </div>
        </div>

        <div class="comment-section card">
          <h3 class="section-title">评论 ({{ post.commentCount ?? comments.length }})</h3>

          <div v-if="userStore.isLoggedIn" class="comment-form">
            <img :src="userStore.avatar || '/default-avatar.png'" class="comment-form-avatar" />
            <div class="comment-form-body">
              <div v-if="replyTarget" class="reply-indicator">
                <span>回复 @{{ replyTarget.replyToUsername }}</span>
                <button class="cancel-reply" @click="cancelReply">✕</button>
              </div>
              <textarea v-model="commentContent" :placeholder="replyTarget ? `回复 @${replyTarget.replyToUsername}...` : '写下你的评论...'" rows="2" @input="autoResizeComment" maxlength="500"></textarea>
              <div class="comment-form-bottom">
                <span class="char-count">{{ commentContent.length }}/500</span>
                <button class="btn btn-primary btn-sm" @click="submitComment" :disabled="!commentContent.trim() || submitting">
                  {{ submitting ? '发表中...' : replyTarget ? '回复' : '发表' }}
                </button>
              </div>
            </div>
          </div>
          <div v-else class="login-hint">
            <router-link to="/login">登录</router-link>后参与评论
          </div>

          <div v-if="commentsLoading" class="comment-skeleton">
            <div v-for="i in 3" :key="i" class="comment-skeleton-item">
              <div class="cs-avatar"></div>
              <div class="cs-body">
                <div class="cs-name"></div>
                <div class="cs-text"></div>
              </div>
            </div>
          </div>

          <div v-else-if="comments.length === 0" class="no-comments">
            <p>暂无评论，快来抢沙发吧~</p>
          </div>

          <div v-else class="comment-list">
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <img :src="comment.user?.avatar || '/default-avatar.png'" :alt="comment.user?.username" class="comment-avatar" />
              <div class="comment-body">
                <div class="comment-header">
                  <router-link :to="`/user/${comment.user?.id}`" class="comment-author">{{ comment.user?.nickname || comment.user?.username }}</router-link>
                  <span class="comment-time">{{ formatRelativeTime(comment.createTime) }}</span>
                </div>
                <div class="comment-content">{{ comment.content }}</div>
                <div class="comment-actions">
                  <button class="comment-action-btn" @click="startReply(comment)">回复</button>
                  <button v-if="comment.user?.id === userStore.userId" class="comment-action-btn danger" @click="deleteComment(comment.id)">删除</button>
                </div>

                <div v-if="comment.replies && comment.replies.length" class="nested-replies">
                  <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                    <img :src="reply.user?.avatar || '/default-avatar.png'" :alt="reply.user?.username" class="reply-avatar" />
                    <div class="reply-body">
                      <div class="comment-header">
                        <router-link :to="`/user/${reply.user?.id}`" class="comment-author">{{ reply.user?.nickname || reply.user?.username }}</router-link>
                        <span class="comment-time">{{ formatRelativeTime(reply.createTime) }}</span>
                      </div>
                      <div class="comment-content">{{ reply.content }}</div>
                      <div class="comment-actions">
                        <button class="comment-action-btn" @click="startReply(reply)">回复</button>
                        <button v-if="reply.user?.id === userStore.userId" class="comment-action-btn danger" @click="deleteComment(reply.id)">删除</button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <teleport to="body">
      <transition name="modal">
        <div v-if="showRepostModal" class="modal-overlay" @click.self="closeRepostModal">
          <div class="modal-content card">
            <div class="modal-header">
              <h3>转发动态</h3>
              <button class="close-btn" @click="closeRepostModal">✕</button>
            </div>
            <div class="modal-body">
              <div v-if="post" class="repost-original-card">
                <div class="repost-original-header">
                  <img :src="post.userAvatar || '/default-avatar.png'" class="repost-original-avatar" />
                  <span class="repost-original-name">{{ post.userNickname || post.userUsername }}</span>
                </div>
                <p class="repost-original-text">{{ post.content }}</p>
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

    <ImagePreview :images="previewImages" :initial-index="previewIndex" :show="showPreview" @close="showPreview = false" />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ImagePreview from '../../components/common/ImagePreview.vue'
import { circleApi } from '../../api/circle'
import { useUserStore } from '../../stores/user'
import { formatRelativeTime, formatDate } from '../../utils'
import { useLogger } from '../../utils/logger'
import { toast } from '../../utils/toast'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const logger = useLogger('CircleDetail')

const post = ref(null)
const comments = ref([])
const loading = ref(true)
const error = ref('')
const isLiked = ref(false)
const likeAnim = ref(false)
const commentContent = ref('')
const replyTarget = ref(null)
const submitting = ref(false)
const commentsLoading = ref(true)
const showPreview = ref(false)
const previewImages = ref([])
const previewIndex = ref(0)
const showRepostModal = ref(false)
const repostContent = ref('')
const reposting = ref(false)

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

const fetchPost = async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await circleApi.getPostById(route.params.id)
    post.value = res.data
  } catch (err) {
    logger.error('fetchPost error', { error: err.message })
    error.value = err.response?.data?.message || '动态不存在或已删除'
  } finally {
    loading.value = false
  }
  if (userStore.isLoggedIn) {
    try {
      const likeRes = await circleApi.checkLikeStatus(route.params.id)
      isLiked.value = likeRes.data ?? false
    } catch (err) {
      logger.warn('checkLikeStatus failed', { error: err.message })
    }
  }
}

const fetchComments = async () => {
  commentsLoading.value = true
  try {
    const res = await circleApi.getComments(route.params.id)
    comments.value = Array.isArray(res.data) ? res.data : []
  } catch (err) {
    logger.error('fetchComments error', { error: err.message })
  } finally {
    commentsLoading.value = false
  }
}

const toggleLike = async () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  const prev = isLiked.value
  isLiked.value = !isLiked.value
  post.value.likeCount += isLiked.value ? 1 : -1
  likeAnim.value = true
  setTimeout(() => likeAnim.value = false, 400)
  try {
    await circleApi.toggleLike(route.params.id)
  } catch (err) {
    isLiked.value = prev
    post.value.likeCount += isLiked.value ? 1 : -1
    logger.error('toggleLike error', { error: err.message })
  }
}

const submitComment = async () => {
  if (!commentContent.value.trim()) return
  if (commentContent.value.length > 500) {
    toast.error('评论内容不能超过500个字符')
    return
  }
  submitting.value = true
  try {
    await circleApi.createComment({
      postId: Number(route.params.id),
      content: commentContent.value,
      parentId: replyTarget.value?.parentId || null,
      replyToUserId: replyTarget.value?.replyToUserId || null
    })
    commentContent.value = ''
    replyTarget.value = null
    await fetchComments()
    if (post.value) post.value.commentCount++
  } catch (err) {
    logger.error('submitComment error', { error: err.message })
    toast.error('评论失败')
  } finally {
    submitting.value = false
  }
}

const startReply = (comment) => {
  replyTarget.value = {
    parentId: comment.parentId || comment.id,
    replyToUserId: comment.user?.id,
    replyToUsername: comment.user?.nickname || comment.user?.username
  }
}

const cancelReply = () => {
  replyTarget.value = null
}

const deleteComment = async (commentId) => {
  try {
    await circleApi.deleteComment(commentId)
    toast.success('评论已删除')
    await fetchComments()
    if (post.value) post.value.commentCount = Math.max(0, post.value.commentCount - 1)
  } catch (err) {
    logger.error('deleteComment error', { error: err.message })
    toast.error('删除失败')
  }
}

const openRepostModal = () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  showRepostModal.value = true
  repostContent.value = ''
}

const closeRepostModal = () => {
  showRepostModal.value = false
  repostContent.value = ''
}

const confirmRepost = async () => {
  if (!userStore.isLoggedIn) { toast.warning('请先登录'); return }
  if (!post.value) return
  reposting.value = true
  try {
    await circleApi.repost(post.value.id, repostContent.value)
    closeRepostModal()
    post.value.repostCount = (post.value.repostCount || 0) + 1
    toast.success('转发成功')
  } catch (err) {
    logger.error('repost error', { error: err.message })
    toast.error('转发失败')
  } finally {
    reposting.value = false
  }
}

const openImagePreview = (images, idx) => {
  previewImages.value = images
  previewIndex.value = idx
  showPreview.value = true
}

const autoResizeComment = (e) => {
  const el = e.target
  el.style.height = 'auto'
  el.style.height = el.scrollHeight + 'px'
}

onMounted(() => {
  fetchPost()
  fetchComments()
})

watch(() => route.params.id, () => {
  if (route.params.id) {
    fetchPost()
    fetchComments()
  }
})
</script>

<style scoped>
.back-btn { display: flex; align-items: center; gap: 4px; padding: 8px 12px; background: transparent; border: 1px solid var(--border); border-radius: 8px; color: var(--text-secondary); cursor: pointer; font-size: 0.875rem; transition: all 0.2s; width: fit-content; margin-bottom: 16px; }
.back-btn:hover { background: var(--border); color: var(--text-primary); }
.circle-detail-page {
  max-width: 600px;
  margin: 0 auto;
  padding: 16px;
}

.feed-card {
  padding: 18px;
  margin-bottom: 14px;
  border-radius: 16px;
}

.feed-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.feed-author {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}

.author-avatar {
  width: 44px;
  height: 44px;
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
  color: var(--text-primary);
}

.feed-time {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 2px;
}

.visibility-badge {
  font-size: 16px;
  cursor: default;
}

.feed-text {
  font-size: 15px;
  color: var(--text-primary);
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.feed-images {
  display: grid;
  gap: 4px;
  margin-top: 14px;
  border-radius: 12px;
  overflow: hidden;
}

.grid-1 { grid-template-columns: 1fr; max-width: 400px; }
.grid-2 { grid-template-columns: 1fr 1fr; }
.grid-3 { grid-template-columns: 1fr 1fr 1fr; }
.grid-4 { grid-template-columns: 1fr 1fr; }
.grid-5, .grid-6, .grid-7, .grid-8, .grid-9 { grid-template-columns: 1fr 1fr 1fr; }

.img-wrap {
  position: relative;
  aspect-ratio: 1;
  overflow: hidden;
  border-radius: 8px;
  background: var(--bg-secondary);
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
  width: 44px;
  height: 44px;
  background: rgba(0,0,0,0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
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
  margin-top: 14px;
  padding: 14px;
  background: var(--bg-secondary);
  border-radius: 12px;
  border: 1px solid var(--border);
  cursor: pointer;
  transition: background 0.2s;
}

.repost-card:hover {
  background: var(--border);
}

.repost-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.repost-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
}

.repost-author {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
}

.repost-text {
  font-size: 14px;
  color: var(--text-primary);
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
  background: var(--bg-secondary);
  border-radius: 6px;
  font-size: 13px;
  color: var(--text-secondary);
}

.feed-stats {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  font-size: 12px;
  color: var(--text-muted);
  border-bottom: 1px solid var(--border);
  margin-bottom: 10px;
}

.feed-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  background: var(--bg-secondary);
  border: none;
  border-radius: 20px;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s ease;
  flex: 1;
  justify-content: center;
}

.action-btn:hover {
  background: var(--border);
}

.action-btn.liked {
  color: var(--error);
  background: rgba(239, 68, 68, 0.1);
}

.action-btn.active-comment {
  color: var(--primary);
  background: var(--primary-light);
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

.comment-section {
  padding: 18px;
  border-radius: 16px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 16px;
  color: var(--text-primary);
}

.comment-form {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.comment-form-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.comment-form-body {
  flex: 1;
}

.reply-indicator {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 12px;
  background: var(--primary-light);
  border-radius: 8px 8px 0 0;
  font-size: 13px;
  color: var(--primary);
}

.cancel-reply {
  background: none;
  border: none;
  font-size: 14px;
  color: var(--text-muted);
  cursor: pointer;
  padding: 0 4px;
}

.cancel-reply:hover {
  color: var(--error);
}

.comment-form-body textarea {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid var(--border);
  border-radius: 12px;
  font-size: 14px;
  font-family: inherit;
  resize: none;
  outline: none;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.comment-form-body textarea:focus {
  border-color: var(--primary);
}

.comment-form-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}

.comment-form-bottom .char-count {
  font-size: 12px;
  color: var(--text-muted);
}

.btn-primary {
  padding: 8px 20px;
  background: var(--primary);
  color: #fff;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary:hover {
  background: var(--primary-hover);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-primary.btn-sm {
  padding: 6px 16px;
  font-size: 13px;
}

.login-hint {
  text-align: center;
  padding: 14px;
  background: var(--bg-secondary);
  border-radius: 12px;
  margin-bottom: 20px;
  font-size: 14px;
  color: var(--text-muted);
}

.login-hint a {
  color: var(--primary);
  font-weight: 500;
  text-decoration: none;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.comment-item {
  display: flex;
  gap: 10px;
}

.comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.comment-author {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  text-decoration: none;
}

.comment-author:hover {
  color: var(--primary);
}

.comment-time {
  font-size: 11px;
  color: var(--text-muted);
}

.comment-content {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.5;
  word-break: break-word;
}

.comment-actions {
  display: flex;
  gap: 10px;
  margin-top: 4px;
}

.comment-action-btn {
  background: none;
  border: none;
  color: var(--text-muted);
  font-size: 12px;
  cursor: pointer;
  padding: 2px 0;
}

.comment-action-btn:hover {
  color: var(--primary);
}

.comment-action-btn.danger:hover {
  color: var(--error);
}

.nested-replies {
  margin-top: 10px;
  padding: 10px 0 0 14px;
  border-left: 2px solid var(--border);
}

.reply-item {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.reply-item:last-child {
  margin-bottom: 0;
}

.reply-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.reply-body {
  flex: 1;
  min-width: 0;
}

.no-comments {
  text-align: center;
  padding: 30px;
  color: var(--text-muted);
  font-size: 14px;
}

.loading-skeleton {
  padding: 18px;
}

.skeleton-card {
  padding: 18px;
  border-radius: 16px;
}

.skeleton-header {
  display: flex;
  gap: 12px;
  margin-bottom: 14px;
}

.skeleton-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(90deg, var(--skeleton-base) 25%, var(--skeleton-highlight) 50%, var(--skeleton-base) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-info { flex: 1; }
.skeleton-name { width: 100px; height: 14px; background: var(--skeleton-base); border-radius: 4px; margin-bottom: 6px; animation: shimmer 1.5s infinite; background-size: 200% 100%; }
.skeleton-time { width: 60px; height: 12px; background: var(--skeleton-base); border-radius: 4px; animation: shimmer 1.5s infinite; background-size: 200% 100%; }
.skeleton-text { height: 14px; background: var(--skeleton-base); border-radius: 4px; margin-bottom: 8px; animation: shimmer 1.5s infinite; background-size: 200% 100%; }
.skeleton-text.short { width: 60%; }

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.comment-skeleton {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 10px 0;
}

.comment-skeleton-item {
  display: flex;
  gap: 10px;
}

.cs-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--skeleton-base);
  flex-shrink: 0;
  animation: shimmer 1.5s infinite;
  background-size: 200% 100%;
}

.cs-body { flex: 1; }
.cs-name { width: 80px; height: 12px; background: var(--skeleton-base); border-radius: 4px; margin-bottom: 8px; animation: shimmer 1.5s infinite; background-size: 200% 100%; }
.cs-text { width: 100%; height: 12px; background: var(--skeleton-base); border-radius: 4px; animation: shimmer 1.5s infinite; background-size: 200% 100%; }

.error-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-secondary);
}

.error-state h3 {
  margin: 12px 0 16px;
  font-size: 16px;
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
  border-bottom: 1px solid var(--border);
}

.modal-header h3 {
  font-size: 16px;
  font-weight: 600;
}

.close-btn {
  background: var(--bg-secondary);
  border: none;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.close-btn:hover {
  background: var(--border);
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

.modal-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  padding: 14px 20px;
  border-top: 1px solid var(--border);
}

.post-textarea {
  width: 100%;
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 12px;
  font-size: 15px;
  font-family: inherit;
  resize: vertical;
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.2s;
  min-height: 80px;
}

.post-textarea:focus {
  border-color: var(--primary);
}

.char-count {
  text-align: right;
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 8px;
}

.char-count.warn {
  color: var(--warning);
}

.repost-original-card {
  padding: 14px;
  background: var(--bg-secondary);
  border-radius: 12px;
  margin-bottom: 14px;
  border: 1px solid var(--border);
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
  color: var(--text-secondary);
}

.repost-original-text {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}

.btn-ghost {
  padding: 8px 20px;
  background: transparent;
  border: 1px solid var(--border);
  border-radius: 20px;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-ghost:hover {
  background: var(--bg-secondary);
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

.topic-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}

.topic-tag-link {
  display: inline-block;
  padding: 3px 10px;
  background: var(--primary-light);
  color: var(--primary);
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  text-decoration: none;
  transition: background 0.2s;
}

.topic-tag-link:hover {
  background: var(--primary-light);
  color: var(--primary-hover);
}

@media (max-width: 600px) {
  .circle-detail-page { padding: 12px; }
  .feed-card, .comment-section { padding: 14px; }
}
</style>
