<template>
  <div class="reading-progress" :style="{ width: readProgress + '%' }"></div>
  <div class="post-detail-page">
    <button class="back-btn" @click="router.back()">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
      返回
    </button>
    <div class="post-container">
      <!-- Loading State -->
      <div v-if="isLoading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="error && !post" class="error-state">
        <h2>文章不存在或已被删除</h2>
        <router-link to="/" class="btn btn-primary">返回首页</router-link>
      </div>

      <!-- 文章内容 -->
      <article class="post-article card" v-else-if="post">
        <!-- 封面图 -->
        <div v-if="post.coverImage" class="post-cover">
          <img :src="post.coverImage" :alt="post.title" />
        </div>
        
        <div class="post-content">
          <!-- 标题 -->
          <h1 class="post-title">{{ post.title }}</h1>
          
          <!-- 作者信息 -->
          <div class="post-author">
            <router-link :to="`/user/${post.userId}`" class="author-info">
              <img :src="post.avatar || '/default-avatar.png'" :alt="post.username" class="author-avatar" />
              <div class="author-meta">
                <span class="author-name">{{ post.nickname || post.username }}</span>
                <span class="post-time">{{ formatRelativeTime(post.createTime) }}</span>
              </div>
            </router-link>
            <button 
              v-if="userStore.isLoggedIn && userStore.userId !== post.userId"
              class="btn btn-sm"
              :class="isFollowing ? 'btn-secondary' : 'btn-primary'"
              @click="toggleFollow"
            >
              {{ isFollowing ? '已关注' : '关注' }}
            </button>
            <router-link
              v-if="userStore.isLoggedIn && userStore.userId !== post.userId"
              :to="`/messages?userId=${post.userId}`"
              class="btn btn-ghost btn-sm"
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 2L11 13"/><path d="M22 2l-7 20-4-9-9-4 20-7z"/></svg>
              私信
            </router-link>
            <router-link v-if="userStore.isLoggedIn && userStore.userId === post.userId" :to="`/post-edit/${post.id}`" class="btn btn-ghost btn-sm">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
              编辑
            </router-link>
          </div>
          
          <!-- 标签 -->
          <div v-if="post.tags && post.tags.length" class="post-tags">
            <span
              v-for="tag in post.tags"
              :key="tag.id"
              class="tag"
            >
              {{ tag.name }}
            </span>
          </div>
          
          <!-- 文章正文 -->
          <div class="markdown-body" v-html="renderedContent"></div>
          
          <!-- 文章操作 -->
          <div class="post-actions">
            <button 
              class="action-btn"
              :class="{ active: isLiked }"
              @click="toggleLike"
              :disabled="!userStore.isLoggedIn"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" :fill="isLiked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
              </svg>
              <span>{{ post.likeCount || 0 }}</span>
            </button>
            <button 
              class="action-btn"
              :class="{ active: isCollected }"
              @click="toggleCollect"
              :disabled="!userStore.isLoggedIn"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" :fill="isCollected ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
                <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/>
              </svg>
              <span>{{ post.collectCount || 0 }}</span>
            </button>
            <button class="action-btn" @click="sharePost">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="18" cy="5" r="3"/>
                <circle cx="6" cy="12" r="3"/>
                <circle cx="18" cy="19" r="3"/>
                <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/>
                <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/>
              </svg>
              <span>{{ post.shareCount || 0 }}</span>
            </button>
            <button class="action-btn report-btn" @click="openReport" title="举报">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/><circle cx="12" cy="16" r="1"/><circle cx="12" cy="12" r="1"/><circle cx="12" cy="8" r="1"/></svg>
            </button>
          </div>
        </div>
      </article>
      
      <!-- 评论区 -->
      <div class="comment-section card">
        <h3 class="section-title">评论 ({{ comments.length }})</h3>
        
        <!-- 评论输入框 -->
        <div v-if="userStore.isLoggedIn" class="comment-input">
          <div v-if="replyingTo" class="reply-indicator">
            回复 @{{ replyingTo.nickname || replyingTo.username }}
            <button @click="cancelReply" class="cancel-reply-btn">取消</button>
          </div>
          <textarea
            v-model="commentContent"
            :placeholder="replyingTo ? '写下你的回复...' : '写下你的评论...'"
            rows="3"
            maxlength="1000"
          ></textarea>
          <button
            class="btn btn-primary btn-sm"
            @click="submitComment"
            :disabled="!commentContent.trim()"
          >
            {{ replyingTo ? '发表回复' : '发表评论' }}
          </button>
        </div>
        <div v-else class="login-hint">
          <router-link to="/login">登录</router-link>后参与评论
        </div>
        
        <!-- 评论列表 -->
        <div class="comment-list">
          <div v-if="commentsError" class="empty-hint error-hint">
            评论加载失败，请稍后重试
          </div>
          <div v-else-if="comments.length === 0" class="empty-hint">
            暂无评论，快来抢沙发吧
          </div>
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <img :src="comment.userAvatar || '/default-avatar.png'" :alt="comment.username" class="comment-avatar" />
            <div class="comment-body">
              <div class="comment-header">
                <router-link :to="`/user/${comment.userId}`" class="comment-author">
                  {{ comment.nickname || comment.username }}
                </router-link>
                <span class="comment-time">{{ formatRelativeTime(comment.createTime) }}</span>
              </div>
              <div class="comment-content" v-html="sanitizeText(comment.content)"></div>
              <div class="comment-actions">
                <button 
                  v-if="userStore.isLoggedIn"
                  class="reply-btn"
                  @click="replyTo(comment)"
                >
                  回复
                </button>
                <button 
                  v-if="userStore.isLoggedIn && (userStore.userId === comment.userId || userStore.isAdmin)"
                  class="delete-btn"
                  @click="deleteComment(comment.id)"
                >
                  删除
                </button>
              </div>
              
              <!-- 子评论 -->
              <div v-if="comment.replies && comment.replies.length" class="reply-list">
                <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                  <img :src="reply.userAvatar || '/default-avatar.png'" :alt="reply.username" class="reply-avatar" />
                  <div class="reply-body">
                    <div class="reply-header">
                      <router-link :to="`/user/${reply.userId}`" class="reply-author">
                        {{ reply.nickname || reply.username }}
                      </router-link>
                      <span class="reply-time">{{ formatRelativeTime(reply.createTime) }}</span>
                    </div>
                    <div class="reply-content" v-html="sanitizeText(reply.content)"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 侧边栏 -->
    <aside class="post-sidebar">
      <!-- 目录 -->
      <div v-if="toc.length > 0" class="toc-card card">
        <h3 class="sidebar-title">目录</h3>
        <nav class="toc-nav">
          <a 
            v-for="(item, index) in toc" 
            :key="item.id"
            :href="`#${item.id}`"
            class="toc-link"
            :class="[`level-${item.level}`, { active: activeTocId === item.id }]"
            @click.prevent="scrollToHeading(index)"
          >
            {{ item.text }}
          </a>
        </nav>
      </div>
      
      <!-- 文章信息 -->
      <div class="info-card card">
        <h3 class="sidebar-title">文章信息</h3>
        <div class="info-list">
          <div class="info-item">
            <span class="info-label">阅读</span>
            <span class="info-value">{{ post?.viewCount || 0 }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">点赞</span>
            <span class="info-value">{{ post?.likeCount || 0 }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">收藏</span>
            <span class="info-value">{{ post?.collectCount || 0 }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">评论</span>
            <span class="info-value">{{ post?.commentCount || 0 }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">分享</span>
            <span class="info-value">{{ post?.shareCount || 0 }}</span>
          </div>
        </div>
      </div>
    </aside>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { postApi } from '../../api/post'
import { commentApi } from '../../api/comment'
import { likeApi } from '../../api/like'
import { collectApi } from '../../api/collect'
import { followApi } from '../../api/follow'
import { shareApi } from '../../api/share'
import { useUserStore } from '../../stores/user'
import { formatRelativeTime } from '../../utils'
import { useLogger } from '../../utils/logger'
import { toast } from '../../utils/toast'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const logger = useLogger('PostDetail')

const post = ref(null)
const comments = ref([])
const isLiked = ref(false)
const isCollected = ref(false)
const isFollowing = ref(false)
const readProgress = ref(0)
const commentContent = ref('')
const toc = ref([])
const activeTocId = ref('')
const isLoading = ref(false)
const error = ref(false)
const commentsError = ref(false)
const replyingTo = ref(null)

// 渲染Markdown内容
const renderedContent = computed(() => {
  if (!post.value?.content) return ''

  const html = marked.parse(post.value.content)
  return DOMPurify.sanitize(html)
})

// XSS防护：对用户生成内容进行净化
const sanitizeText = (text) => {
  if (!text) return ''
  return DOMPurify.sanitize(text, { ALLOWED_TAGS: [] })
}

// 从Markdown文本中提取目录（不依赖DOM，更可靠）
const extractTocFromMarkdown = (markdown) => {
  if (!markdown) return []
  const headingRegex = /^(#{1,4})\s+(.+)$/gm
  const items = []
  let match
  while ((match = headingRegex.exec(markdown)) !== null) {
    items.push({
      id: `heading-${items.length}`,
      text: match[2],
      level: match[1].length
    })
  }
  return items
}

// 给DOM中的标题添加ID（供滚动高亮定位）
const addHeadingIds = () => {
  const headings = document.querySelectorAll('.markdown-body h1, .markdown-body h2, .markdown-body h3, .markdown-body h4')
  headings.forEach((el, index) => {
    if (!el.id) el.id = `heading-${index}`
  })
}

// 点击目录项滚动到对应标题
const scrollToHeading = (index) => {
  const headings = document.querySelectorAll('.markdown-body h1, .markdown-body h2, .markdown-body h3, .markdown-body h4')
  const target = headings[index]
  if (target) {
    target.scrollIntoView({ behavior: 'smooth', block: 'start' })
    activeTocId.value = toc.value[index]?.id || ''
  }
}

// 获取文章详情
const fetchPost = async () => {
  isLoading.value = true
  error.value = false

  try {
    const response = await postApi.getPostById(route.params.id)

    // 检查返回数据是否有效
    if (!response.data) {
      error.value = true
      return
    }

    post.value = response.data

    // 获取点赞和收藏状态
    if (userStore.isLoggedIn) {
      try {
        await Promise.all([
          checkLikeStatus(),
          checkCollectStatus(),
          checkFollowStatus()
        ])
      } catch (err) {
        logger.warn('Failed to fetch initial status', { error: err.message })
      }
    }

    // 获取分享数
    try {
      const shareResponse = await shareApi.getShareCount(route.params.id)
      post.value.shareCount = shareResponse.data ?? 0
    } catch (err) {
      post.value.shareCount = 0
    }

    // 提取目录 — 由下方 watch 自动执行
  } catch (err) {
    logger.error('Failed to fetch post', { error: err.message })

    // 404 或其他错误都显示错误状态
    if (err.response?.status === 404 || err.response?.status === 403) {
      error.value = true
    } else {
      // 其他网络错误也显示错误状态
      error.value = true
    }
  } finally {
    isLoading.value = false
  }
}

// 文章内容变化后提取目录
watch(() => post.value?.content, async (content) => {
  toc.value = content ? extractTocFromMarkdown(content) : []
  if (content) {
    await nextTick()
    addHeadingIds()
  }
})

// 获取评论
const fetchComments = async () => {
  commentsError.value = false
  try {
    const response = await commentApi.getCommentsByPostId(route.params.id)
    comments.value = response.data || []
  } catch (err) {
    logger.error('Failed to fetch comments', { error: err.message })
    commentsError.value = true
  }
}

// 检查点赞状态
const checkLikeStatus = async () => {
  try {
    const response = await likeApi.checkLikeStatus(route.params.id)
    isLiked.value = response.data?.liked
  } catch (err) {
    logger.error('Failed to check like status', { error: err.message })
  }
}

// 检查收藏状态
const checkCollectStatus = async () => {
  try {
    const response = await collectApi.checkCollectStatus(route.params.id)
    isCollected.value = response.data?.collected
  } catch (err) {
    logger.error('Failed to check collect status', { error: err.message })
  }
}

// 检查关注状态
const checkFollowStatus = async () => {
  if (!post.value?.userId) return
  try {
    const response = await followApi.checkFollowStatus(post.value.userId)
    isFollowing.value = response.data?.following
  } catch (err) {
    logger.error('Failed to check follow status', { error: err.message })
  }
}

// 切换点赞
const toggleLike = async () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  
  try {
    const likeResult = await likeApi.toggleLike(route.params.id)
    isLiked.value = !isLiked.value
    post.value.likeCount = (post.value.likeCount || 0) + (isLiked.value ? 1 : -1)
  } catch (err) {
    logger.error('Failed to toggle like', { error: err.message })
    toast.error('操作失败')
  }
}

// 切换收藏
const toggleCollect = async () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  
  try {
    await collectApi.toggleCollect(route.params.id)
    isCollected.value = !isCollected.value
    post.value.collectCount = (post.value.collectCount || 0) + (isCollected.value ? 1 : -1)
  } catch (err) {
    logger.error('Failed to toggle collect', { error: err.message })
    toast.error('操作失败')
  }
}

const openReport = () => {
  router.push(`/report/post/${post.value.id}`)
}

// 切换关注
const toggleFollow = async () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  if (!post.value?.userId) return
  
  try {
    await followApi.toggleFollow(post.value.userId)
    isFollowing.value = !isFollowing.value
  } catch (err) {
    logger.error('Failed to toggle follow', { error: err.message })
    toast.error('操作失败')
  }
}

// 提交评论
const submitComment = async () => {
  if (!commentContent.value.trim()) return

  try {
    const data = {
      postId: route.params.id,
      content: commentContent.value
    }

    // 如果是回复评论，添加父评论ID
    if (replyingTo.value) {
      data.parentId = replyingTo.value.id
    }

    await commentApi.createComment(data)
    commentContent.value = ''
    replyingTo.value = null
    await fetchComments()
    toast.success('评论成功')
  } catch (err) {
    logger.error('Failed to submit comment', { error: err.message })
    toast.error('评论失败')
  }
}

// 删除评论
const deleteComment = async (commentId) => {
  if (!confirm('确定删除这条评论吗？')) return
  
  try {
    await commentApi.deleteComment(commentId)
    await fetchComments()
  } catch (err) {
    logger.error('Failed to delete comment', { error: err.message })
    toast.error('删除失败')
  }
}

// 回复评论
const replyTo = (comment) => {
  replyingTo.value = comment
  commentContent.value = `@${comment.nickname || comment.username} `
  // 滚动到评论输入框
  document.querySelector('.comment-input')?.scrollIntoView({ behavior: 'smooth' })
}

// 取消回复
const cancelReply = () => {
  replyingTo.value = null
}

// 分享文章
const sharePost = async () => {
  try {
    // 调用API记录分享
    await shareApi.recordShare(route.params.id, 'web')

    // 复制链接到剪贴板
    const url = window.location.href
    if (navigator.clipboard) {
      await navigator.clipboard.writeText(url)
      toast.success('链接已复制到剪贴板')
    } else {
      toast.success('分享成功')
    }
  } catch (err) {
    logger.error('Failed to share post', { error: err.message })
    toast.error('分享失败')
  }
}

// 监听滚动高亮目录 + 阅读进度
const handleScroll = () => {
  const scrollTop = window.scrollY
  const docHeight = document.documentElement.scrollHeight - window.innerHeight
  readProgress.value = docHeight > 0 ? Math.min(100, Math.round((scrollTop / docHeight) * 100)) : 0

  const headings = document.querySelectorAll('.markdown-body h1, .markdown-body h2, .markdown-body h3, .markdown-body h4')
  let currentId = ''
  headings.forEach(heading => {
    if (heading.getBoundingClientRect().top <= 100) {
      currentId = heading.id
    }
  })
  activeTocId.value = currentId
}

onMounted(() => {
  fetchPost()
  fetchComments()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

watch(() => route.params.id, () => {
  post.value = null
  comments.value = []
  toc.value = []
  error.value = false
  isLoading.value = false
  commentContent.value = ''
  replyingTo.value = null
  isLiked.value = false
  isCollected.value = false
  isFollowing.value = false
  commentsError.value = false
  fetchPost()
  fetchComments()
})
</script>

<style scoped>
.reading-progress {
  position: fixed; top: 0; left: 0; height: 3px;
  background: var(--primary, #1a73e8);
  z-index: 9999;
  transition: width 0.1s linear;
}

.report-btn { opacity: 0.4; }
.report-btn:hover { opacity: 0.8; color: #ef4444; }
.back-btn { display: flex; align-items: center; gap: 4px; padding: 8px 12px; background: transparent; border: 1px solid var(--border); border-radius: 8px; color: var(--text-secondary); cursor: pointer; font-size: 0.875rem; transition: all 0.2s; width: fit-content; margin-bottom: 16px; grid-column: 1 / -1; }
.back-btn:hover { background: var(--border); color: var(--text-primary); }

.post-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--spacing-lg);
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: var(--spacing-xl);
}

.post-article {
  overflow: hidden;
}

.post-cover {
  height: 300px;
  overflow: hidden;
}

.post-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.post-content {
  padding: var(--spacing-xl);
}

.post-title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-lg);
  line-height: 1.3;
}

.post-author {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-lg);
  padding-bottom: var(--spacing-lg);
  border-bottom: 1px solid var(--border);
}

.author-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  text-decoration: none;
}

.author-avatar {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-full);
  object-fit: cover;
}

.author-meta {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-weight: 600;
  color: var(--text-primary);
}

.post-time {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-lg);
}

.tag {
  padding: 0.25rem 0.75rem;
  font-size: 0.75rem;
  background: var(--primary-light);
  color: var(--primary);
  border-radius: var(--radius-full);
  text-decoration: none;
}

.post-actions {
  display: flex;
  gap: var(--spacing-md);
  padding-top: var(--spacing-lg);
  border-top: 1px solid var(--border);
  margin-top: var(--spacing-xl);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: 0.5rem 1rem;
  background: none;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition);
}

.action-btn:hover:not(:disabled) {
  border-color: var(--primary);
  color: var(--primary);
}

.action-btn.active {
  background: var(--primary-light);
  border-color: var(--primary);
  color: var(--primary);
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 评论区 */
.comment-section {
  padding: var(--spacing-xl);
  margin-top: var(--spacing-lg);
}

.section-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-lg);
}

.comment-input {
  margin-bottom: var(--spacing-xl);
}

.comment-input textarea {
  width: 100%;
  padding: var(--spacing-md);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  resize: vertical;
  min-height: 100px;
  font-size: 0.875rem;
  margin-bottom: var(--spacing-sm);
}

.comment-input textarea:focus {
  outline: none;
  border-color: var(--primary);
}

.login-hint {
  text-align: center;
  padding: var(--spacing-lg);
  background: var(--background);
  border-radius: var(--radius);
  margin-bottom: var(--spacing-xl);
}

.login-hint a {
  color: var(--primary);
  font-weight: 500;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.comment-item {
  display: flex;
  gap: var(--spacing-md);
}

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  object-fit: cover;
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-xs);
}

.comment-author {
  font-weight: 600;
  color: var(--text-primary);
  text-decoration: none;
}

.comment-time {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.comment-content {
  font-size: 0.875rem;
  color: var(--text-primary);
  line-height: 1.6;
}

.comment-actions {
  display: flex;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-sm);
}

.reply-btn,
.delete-btn {
  font-size: 0.75rem;
  color: var(--text-muted);
  background: none;
  border: none;
  cursor: pointer;
}

.reply-btn:hover {
  color: var(--primary);
}

.delete-btn:hover {
  color: var(--error);
}

.reply-list {
  margin-top: var(--spacing-md);
  padding-left: var(--spacing-md);
  border-left: 2px solid var(--border);
}

.reply-item {
  display: flex;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) 0;
}

.reply-avatar {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  object-fit: cover;
}

.reply-body {
  flex: 1;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: 2px;
}

.reply-author {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-primary);
  text-decoration: none;
}

.reply-time {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.reply-content {
  font-size: 0.875rem;
  color: var(--text-primary);
}

.empty-hint {
  text-align: center;
  padding: var(--spacing-xl);
  color: var(--text-muted);
}

/* 侧边栏 */
.post-sidebar {
  position: sticky;
  top: 80px;
  height: fit-content;
}

.toc-card,
.info-card {
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-md);
}

.sidebar-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
  padding-bottom: var(--spacing-sm);
  border-bottom: 1px solid var(--border);
}

.toc-nav {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.toc-link {
  display: block;
  padding: var(--spacing-xs) var(--spacing-sm);
  font-size: 0.875rem;
  color: var(--text-secondary);
  text-decoration: none;
  border-radius: var(--radius);
  transition: all var(--transition);
}

.toc-link:hover {
  background: var(--background);
  color: var(--primary);
}

.toc-link.active {
  background: var(--primary-light);
  color: var(--primary);
}

.toc-link.level-2 { padding-left: var(--spacing-md); }
.toc-link.level-3 { padding-left: var(--spacing-lg); }
.toc-link.level-4 { padding-left: var(--spacing-xl); }

.info-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.info-item {
  display: flex;
  justify-content: space-between;
  font-size: 0.875rem;
}

.info-label {
  color: var(--text-muted);
}

.info-value {
  font-weight: 600;
  color: var(--text-primary);
}

/* Loading & Error States */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-xl);
  color: var(--text-muted);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--border);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: var(--spacing-md);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-state {
  text-align: center;
  padding: var(--spacing-xl);
}

.error-state h2 {
  margin-bottom: var(--spacing-lg);
  color: var(--text-primary);
}

/* Reply Indicator */
.reply-indicator {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--primary-light);
  border-radius: var(--radius) var(--radius) 0 0;
  font-size: 0.875rem;
  color: var(--primary);
  margin-bottom: -1px;
}

.cancel-reply-btn {
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  font-size: 0.75rem;
}

.cancel-reply-btn:hover {
  color: var(--error);
}

@media (max-width: 992px) {
  .post-detail-page {
    grid-template-columns: 1fr;
  }
  
  .post-sidebar {
    position: static;
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: var(--spacing-md);
  }
}

@media (max-width: 768px) {
  .post-sidebar {
    grid-template-columns: 1fr;
  }
  
  .post-title {
    font-size: 1.5rem;
  }
  
  .post-actions {
    flex-wrap: wrap;
  }
}
</style>
