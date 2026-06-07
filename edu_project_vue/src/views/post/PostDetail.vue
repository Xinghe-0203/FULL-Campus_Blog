<template>
  <div class="reading-progress-bar" :style="{ width: readProgress + '%' }"></div>
  <div class="post-detail-page">
    <button class="back-btn glass" @click="router.back()">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
      返回
    </button>
    <div class="post-main">
    <div class="post-container">
      <!-- Loading State -->
      <div v-if="isLoading" class="loading-state glass">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="error && !post" class="error-state glass">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
        <h2>{{ error }}</h2>
        <div class="error-actions">
          <button @click="fetchPost" class="btn btn-primary">重新加载</button>
          <router-link to="/" class="btn btn-outline">返回首页</router-link>
        </div>
      </div>

      <!-- 文章内容 -->
      <article class="post-article card" v-else-if="post">
        <!-- 封面图 -->
        <div v-if="post.coverImage" class="post-cover">
          <img :src="post.coverImage" :alt="post.title" />
          <div class="cover-overlay"></div>
        </div>
        
        <div class="post-content">
          <!-- 标题 -->
          <h1 class="post-title">{{ post.title }}</h1>
          
          <!-- 作者信息卡片 -->
          <div class="author-card glass">
            <router-link :to="`/user/${post.userId}`" class="author-info">
              <img :src="post.avatar || '/default-avatar.png'" :alt="post.username" class="author-avatar" />
              <div class="author-meta">
                <span class="author-name">{{ post.nickname || post.username }}</span>
                <span class="post-time">{{ formatRelativeTime(post.createTime) }}</span>
              </div>
            </router-link>
            <div class="author-actions">
              <button
                v-if="userStore.isLoggedIn && userStore.userId !== post.userId"
                class="btn btn-sm"
                :class="isFollowing ? 'btn-secondary' : 'btn-primary'"
                @click="toggleFollow"
                :disabled="isTogglingFollow"
              >
                <svg v-if="!isFollowing" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/></svg>
                <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><polyline points="17 11 19 13 23 9"/></svg>
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
          </div>

          <!-- 标签 -->
          <div v-if="post.tags && post.tags.length" class="post-tags">
            <span
              v-for="tag in post.tags"
              :key="tag.id"
              class="tag badge badge-ghost"
            >
              {{ tag.name }}
            </span>
          </div>
          
          <!-- 文章正文 -->
          <div class="markdown-body" v-html="renderedContent" ref="contentRef"></div>
          
          <!-- 文章操作 -->
          <div class="post-actions">
            <button
              class="action-btn glass"
              :class="{ active: isLiked }"
              @click="toggleLike"
              :disabled="!userStore.isLoggedIn || isTogglingLike"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" :fill="isLiked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
              </svg>
              <span>{{ post.likeCount || 0 }}</span>
            </button>
            <button
              class="action-btn glass"
              :class="{ active: isCollected }"
              @click="toggleCollect"
              :disabled="!userStore.isLoggedIn || isTogglingCollect"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" :fill="isCollected ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
                <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/>
              </svg>
              <span>{{ post.collectCount || 0 }}</span>
            </button>
            <button class="action-btn glass" @click="sharePost">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="18" cy="5" r="3"/>
                <circle cx="6" cy="12" r="3"/>
                <circle cx="18" cy="19" r="3"/>
                <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/>
                <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/>
              </svg>
              <span>{{ post.shareCount || 0 }}</span>
            </button>
            <button class="action-btn glass report-btn" @click="openReport" title="举报">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"/><line x1="4" y1="22" x2="4" y2="15"/></svg>
            </button>
          </div>
        </div>
      </article>
      
      <!-- 评论区 -->
      <div v-if="post" class="comment-section glass">
        <h3 class="section-title">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
          评论 ({{ comments.length }})
        </h3>
        
        <!-- 评论输入框 -->
        <div v-if="userStore.isLoggedIn" class="comment-input glass">
          <div v-if="replyingTo" class="reply-indicator">
            <span>回复 <strong>@{{ replyingTo.nickname || replyingTo.username }}</strong></span>
            <button @click="cancelReply" class="cancel-reply-btn">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              取消
            </button>
          </div>
          <textarea
            v-model="commentContent"
            :placeholder="replyingTo ? '写下你的回复...' : '写下你的评论...'"
            rows="3"
            maxlength="1000"
            class="form-input"
          ></textarea>
          <div class="comment-input-actions">
            <span class="char-count">{{ commentContent.length }}/1000</span>
            <button
              class="btn btn-primary btn-sm"
              @click="submitComment"
              :disabled="!commentContent.trim()"
            >
              {{ replyingTo ? '发表回复' : '发表评论' }}
            </button>
          </div>
        </div>
        <div v-else class="login-hint glass">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/><polyline points="10 17 15 12 10 7"/><line x1="15" y1="12" x2="3" y2="12"/></svg>
          <router-link to="/login" class="btn btn-primary">登录</router-link>
          <span>后参与评论</span>
        </div>
        
        <!-- 评论列表 -->
        <div class="comment-list">
          <div v-if="commentsError" class="empty-hint error-hint">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            评论加载失败，请稍后重试
            <button @click="fetchComments" class="btn btn-sm btn-outline">点击重试</button>
          </div>
          <div v-else-if="comments.length === 0" class="empty-hint">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
            暂无评论，快来抢沙发吧
          </div>
          <div v-for="comment in comments" :key="comment.id" class="comment-item glass">
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
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 17 4 12 9 7"/><path d="M20 18v-2a4 4 0 0 0-4-4H4"/></svg>
                  回复
                </button>
                <button 
                  v-if="userStore.isLoggedIn && (userStore.userId === comment.userId || userStore.isAdmin)"
                  class="delete-btn"
                  @click="deleteComment(comment.id)"
                >
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                  删除
                </button>
              </div>
              
              <!-- 子评论 -->
              <div v-if="comment.replies && comment.replies.length" class="reply-list">
                <div v-for="reply in comment.replies" :key="reply.id" class="reply-item glass">
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
    <aside v-if="post" class="post-sidebar">
      <!-- 目录 -->
      <div v-if="toc.length > 0" class="toc-card glass">
        <h3 class="sidebar-title">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg>
          目录
        </h3>
        <nav class="toc-nav">
          <a 
            v-for="(item, index) in toc" 
            :key="item.id"
            :href="`#${item.id}`"
            class="toc-link"
            :class="[`level-${item.level}`, { active: activeTocId === item.id }]"
            @click.prevent="scrollToHeading(index)"
          >
            <span class="toc-indicator"></span>
            {{ item.text }}
          </a>
        </nav>
      </div>
      
      <!-- 文章信息 -->
      <div class="info-card glass">
        <h3 class="sidebar-title">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>
          文章信息
        </h3>
        <div class="info-list">
          <div class="info-item">
            <span class="info-label">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
              阅读
            </span>
            <span class="info-value">{{ post?.viewCount || 0 }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
              点赞
            </span>
            <span class="info-value">{{ post?.likeCount || 0 }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/></svg>
              收藏
            </span>
            <span class="info-value">{{ post?.collectCount || 0 }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
              评论
            </span>
            <span class="info-value">{{ post?.commentCount || 0 }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/><line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/></svg>
              分享
            </span>
            <span class="info-value">{{ post?.shareCount || 0 }}</span>
          </div>
        </div>
      </div>
    </aside>
    </div>
    <ConfirmDialog />
  </div>
</template>

<script setup lang="ts">
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
import { formatRelativeTime, copyToClipboard } from '../../utils'
import { useLogger } from '../../utils/logger'
import { toast } from '../../utils/toast'
import { useConfirm } from '../../composables/useConfirm'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const logger = useLogger('PostDetail')
const { confirm, ConfirmDialog } = useConfirm()

const post = ref<any>(null)
const comments = ref<any[]>([])
const isLiked = ref(false)
const isCollected = ref(false)
const isFollowing = ref(false)
const readProgress = ref(0)
const commentContent = ref('')
const toc = ref<any[]>([])
const activeTocId = ref('')
const isLoading = ref(false)
const error = ref<string | boolean>(false)
const commentsError = ref(false)
const replyingTo = ref<any>(null)
// contentRef removed - unused

// 渲染Markdown内容
const renderedContent = computed(() => {
  if (!post.value?.content) return ''
  const rawHtml = marked.parse(post.value.content) as string
  const sanitized = DOMPurify.sanitize(rawHtml, {
    USE_PROFILES: { html: true },
    ADD_ATTR: ['target', 'referrerpolicy'],
    FORBID_TAGS: ['style', 'script', 'iframe', 'form'],
    FORBID_ATTR: ['onerror', 'onload', 'onclick']
  })
  // 为所有链接添加安全属性
  return sanitized.replace(/<a\s/g, '<a target="_blank" rel="noopener noreferrer" referrerpolicy="no-referrer" ')
    .replace(/<img\s/g, '<img referrerpolicy="no-referrer" ')
})

// XSS防护：对用户生成内容进行净化
const sanitizeText = (text: string) => {
  if (!text) return ''
  return DOMPurify.sanitize(text, { ALLOWED_TAGS: [] })
}

// 从markdown文本中提取目录（不依赖DOM，更可靠）
const extractTocFromMarkdown = (markdown: string) => {
  if (!markdown) return []
  const headingRegex = /^(#{1,4})\s+(.+)$/gm
  const items = []
  let match
  while ((match = headingRegex.exec(markdown)) !== null) {
    items.push({
      id: `heading-${items.length}`,
      text: match[2] || '',
      level: (match[1] || '').length
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
const scrollToHeading = (index: number) => {
  const headings = document.querySelectorAll('.markdown-body h1, .markdown-body h2, .markdown-body h3, .markdown-body h4')
  const target = headings[index]
  if (target) {
    target.scrollIntoView({ behavior: 'smooth', block: 'start' })
    activeTocId.value = toc.value[index]?.id || ''
  }
}

// 代码块复制按钮的事件委托处理
const handleCodeCopyClick = async (e: Event) => {
  const target = e.target as HTMLElement
  const btn = target.closest('.copy-btn')
  if (!btn) return
  const block = btn.closest('pre')
  if (!block) return
  const code = block.querySelector('code')?.textContent || block.textContent
  try {
    await navigator.clipboard.writeText(code)
    btn.innerHTML = '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>已复制'
    btn.classList.add('copied')
    setTimeout(() => {
      btn.innerHTML = '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>复制'
      btn.classList.remove('copied')
    }, 2000)
  } catch {
    toast.error('复制失败')
  }
}

// 为代码块添加复制按钮
const addCodeCopyButtons = () => {
  const markdownBody = document.querySelector('.markdown-body')
  if (!markdownBody || (markdownBody as any)._copyDelegate) return
  ;(markdownBody as any)._copyDelegate = true
  markdownBody.addEventListener('click', handleCodeCopyClick)
  const codeBlocks = markdownBody.querySelectorAll('pre')
  codeBlocks.forEach((block) => {
    if (block.querySelector('.copy-btn')) return
    const btn = document.createElement('button')
    btn.className = 'copy-btn'
    btn.innerHTML = '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>复制'
    block.appendChild(btn)
  })
}

// 获取文章详情
const fetchPost = async () => {
  isLoading.value = true
  error.value = false

  try {
    const response = await postApi.getPostById(String(route.params.id))

    if (!response.data) {
      error.value = true
      return
    }

    post.value = response.data

    if (userStore.isLoggedIn) {
      try {
        await Promise.all([
          checkLikeStatus(),
          checkCollectStatus(),
          checkFollowStatus()
        ])
      } catch (err: any) {
        logger.warn('Failed to fetch initial status', { error: err.message })
      }
    }

    try {
      const shareResponse = await shareApi.getShareCount(String(route.params.id))
      post.value.shareCount = shareResponse.data ?? 0
    } catch (err: any) {
      post.value.shareCount = 0
    }
  } catch (err: any) {
    logger.error('Failed to fetch post', { error: err.message })
    if (err.response?.status === 404) {
      error.value = '文章不存在或已被删除'
    } else if (err.response?.status === 403) {
      error.value = '抱歉，您无权查看此文章'
    } else {
      error.value = '加载失败，请稍后重试'
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
    addCodeCopyButtons()
  }
})

// 获取评论
const fetchComments = async () => {
  commentsError.value = false
  try {
    const response = await commentApi.getCommentsByPostId(String(route.params.id), {} as any)
    comments.value = response.data?.records || response.data || []
  } catch (err: any) {
    logger.error('Failed to fetch comments', { error: err.message })
    commentsError.value = true
  }
}

// 检查点赞状态
const checkLikeStatus = async () => {
  try {
    const response = await likeApi.checkLikeStatus(String(route.params.id))
    isLiked.value = !!(response.data as any)?.liked
  } catch (err: any) {
    logger.error('Failed to check like status', { error: err.message })
  }
}

// 检查收藏状态
const checkCollectStatus = async () => {
  try {
    const response = await collectApi.checkCollectStatus(String(route.params.id))
    isCollected.value = !!(response.data as any)?.collected
  } catch (err: any) {
    logger.error('Failed to check collect status', { error: err.message })
  }
}

// 检查关注状态
const checkFollowStatus = async () => {
  if (!post.value?.userId) return
  try {
    const response = await followApi.checkFollowStatus(post.value.userId)
    isFollowing.value = !!(response.data as any)?.following
  } catch (err: any) {
    logger.error('Failed to check follow status', { error: err.message })
  }
}

// 防抖状态
const isTogglingLike = ref(false)
const isTogglingCollect = ref(false)
const isTogglingFollow = ref(false)

// 切换点赞
const toggleLike = async () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  if (isTogglingLike.value) return
  isTogglingLike.value = true

  // 乐观更新
  const prevLiked = isLiked.value
  const prevCount = post.value.likeCount
  isLiked.value = !isLiked.value
  post.value.likeCount = (post.value.likeCount || 0) + (isLiked.value ? 1 : -1)

  try {
    const res = await likeApi.toggleLike(String(route.params.id))
    const data = res.data as any
    // 使用后端返回的实际状态
    if (data?.action) {
      isLiked.value = data.action === 'like'
    }
    if (data?.likeCount !== undefined) {
      post.value.likeCount = data.likeCount
    }
  } catch (err: any) {
    // 回滚
    isLiked.value = prevLiked
    post.value.likeCount = prevCount
    logger.error('Failed to toggle like', { error: err.message })
    toast.error('操作失败')
  } finally {
    isTogglingLike.value = false
  }
}

// 切换收藏
const toggleCollect = async () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  if (isTogglingCollect.value) return
  isTogglingCollect.value = true

  // 乐观更新
  const prevCollected = isCollected.value
  const prevCount = post.value.collectCount
  isCollected.value = !isCollected.value
  post.value.collectCount = (post.value.collectCount || 0) + (isCollected.value ? 1 : -1)

  try {
    const res = await collectApi.toggleCollect(String(route.params.id))
    const data = res.data as any
    if (data?.action) {
      isCollected.value = data.action === 'collect'
    }
    if (data?.collectCount !== undefined) {
      post.value.collectCount = data.collectCount
    }
  } catch (err: any) {
    // 回滚
    isCollected.value = prevCollected
    post.value.collectCount = prevCount
    logger.error('Failed to toggle collect', { error: err.message })
    toast.error('操作失败')
  } finally {
    isTogglingCollect.value = false
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
  if (isTogglingFollow.value) return
  isTogglingFollow.value = true

  // 乐观更新
  const prevFollowing = isFollowing.value
  isFollowing.value = !isFollowing.value

  try {
    const res = await followApi.toggleFollow(post.value.userId)
    const data = res.data as any
    if (data?.action) {
      isFollowing.value = data.action === 'follow'
    }
  } catch (err: any) {
    // 回滚
    isFollowing.value = prevFollowing
    logger.error('Failed to toggle follow', { error: err.message })
    toast.error('操作失败')
  } finally {
    isTogglingFollow.value = false
  }
}

// 提交评论
const submitComment = async () => {
  if (!commentContent.value.trim()) return

  try {
    const data: any = {
      postId: String(route.params.id),
      content: commentContent.value
    }

    if (replyingTo.value) {
      data.parentId = replyingTo.value.id
    }

    await commentApi.createComment(data)
    commentContent.value = ''
    replyingTo.value = null
    await fetchComments()
    toast.success('评论成功')
  } catch (err: any) {
    logger.error('Failed to submit comment', { error: err.message })
    toast.error('评论失败')
  }
}

// 删除评论
const deleteComment = async (commentId: number | string) => {
  const ok = await confirm('确定删除这条评论吗？')
  if (!ok) return
  
  try {
    await commentApi.deleteComment(commentId)
    await fetchComments()
  } catch (err: any) {
    logger.error('Failed to delete comment', { error: err.message })
    toast.error('删除失败')
  }
}

// 回复评论
const replyTo = (comment: any) => {
  replyingTo.value = comment
  commentContent.value = `@${comment.nickname || comment.username} `
  document.querySelector('.comment-input')?.scrollIntoView({ behavior: 'smooth' })
}

// 取消回复
const cancelReply = () => {
  replyingTo.value = null
}

// 分享文章
const sharePost = async () => {
  const url = window.location.href
  const success = await copyToClipboard(url)
  if (success) {
    toast.success('链接已复制到剪贴板')
    try {
      await shareApi.recordShare(String(route.params.id), 'web')
      post.value.shareCount = (post.value.shareCount || 0) + 1
    } catch (err: any) {
      logger.warn('Failed to record share', { error: err.message })
    }
  } else {
    toast.error('复制失败，请手动复制链接')
  }
}

// 监听滚动高亮目录 + 阅读进度
const handleScroll = () => {
  const scrollTop = window.scrollY
  const docHeight = document.documentElement.scrollHeight - window.innerHeight
  readProgress.value = docHeight > 0 ? Math.min(100, Math.round((scrollTop / docHeight) * 100)) : 0

  const headings = document.querySelectorAll('.markdown-body h1, .markdown-body h2, .markdown-body h3, .markdown-body h4')
  let currentId = ''
  headings.forEach((heading: Element) => {
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
  const markdownBody = document.querySelector('.markdown-body')
  if (markdownBody && (markdownBody as any)._copyDelegate) {
    markdownBody.removeEventListener('click', handleCodeCopyClick)
    delete (markdownBody as any)._copyDelegate
  }
})

watch(() => route.params.id, () => {
  post.value = null
  comments.value = []
  toc.value = []
  error.value = false
  isLoading.value = true
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
/* 阅读进度条 - 渐变 */
.reading-progress-bar {
  position: fixed;
  top: 0;
  left: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--primary-start), var(--primary-end), var(--accent));
  z-index: 9999;
  transition: width 0.15s ease-out;
  box-shadow: 0 0 8px var(--primary-glow);
}

/* 返回按钮 */
.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius);
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 0.875rem;
  transition: all var(--transition);
  width: fit-content;
  margin-bottom: var(--spacing-lg);
}

.back-btn:hover {
  background: var(--glass-hover);
  color: var(--primary);
  border-color: var(--primary);
  transform: translateX(-2px);
}

/* 主页面布局 */
.post-detail-page {
  max-width: var(--container-xl);
  margin: 0 auto;
  padding: var(--spacing-lg);
}

.post-main {
  display: flex;
  gap: var(--spacing-xl);
  align-items: flex-start;
}

.post-container {
  flex: 1;
  min-width: 0;
}

/* 文章卡片 */
.post-article {
  overflow: hidden;
  transition: all var(--transition-slow);
}

/* 封面图 */
.post-cover {
  position: relative;
  height: 320px;
  overflow: hidden;
}

.post-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}

.post-article:hover .post-cover img {
  transform: scale(1.02);
}

.cover-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 50%;
  background: linear-gradient(to top, var(--glass-bg), transparent);
  pointer-events: none;
}

/* 文章内容区 */
.post-content {
  padding: var(--spacing-xl);
}

.post-title {
  font-size: 2.25rem;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: var(--spacing-lg);
  line-height: 1.25;
  overflow-wrap: break-word;
  word-break: break-word;
  background: linear-gradient(135deg, var(--text-primary), var(--text-secondary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 作者卡片 */
.author-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md) var(--spacing-lg);
  margin-bottom: var(--spacing-lg);
  border-radius: var(--radius-md);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  transition: all var(--transition);
}

.author-card:hover {
  background: var(--glass-hover);
  box-shadow: var(--shadow-md);
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
  border: 2px solid var(--glass-border);
  transition: all var(--transition);
}

.author-info:hover .author-avatar {
  border-color: var(--primary);
  transform: scale(1.05);
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

.author-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

/* 标签 */
.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-lg);
}

/* 文章操作按钮 */
.post-actions {
  display: flex;
  gap: var(--spacing-md);
  padding-top: var(--spacing-lg);
  border-top: 1px solid var(--glass-border);
  margin-top: var(--spacing-xl);
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: 0.625rem 1rem;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition);
  font-size: 0.875rem;
}

.action-btn:hover:not(:disabled) {
  background: var(--glass-hover);
  border-color: var(--primary);
  color: var(--primary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.action-btn.active {
  background: var(--primary-light);
  border-color: var(--primary);
  color: var(--primary);
}

.action-btn.active:hover {
  background: var(--primary);
  color: white;
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.report-btn:hover:not(:disabled) {
  border-color: var(--error);
  color: var(--error);
  background: var(--error-light);
}

/* 评论区 */
.comment-section {
  padding: var(--spacing-xl);
  margin-top: var(--spacing-lg);
  border-radius: var(--radius-lg);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border-wet);
  box-shadow: var(--glass-shadow-wet);
}

.section-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-lg);
  padding-bottom: var(--spacing-sm);
  border-bottom: 1px solid var(--glass-border);
}

/* 评论输入框 */
.comment-input {
  padding: var(--spacing-md);
  margin-bottom: var(--spacing-xl);
  border-radius: var(--radius-md);
  background: var(--surface);
  border: 1px solid var(--border);
}

.comment-input textarea {
  width: 100%;
  padding: var(--spacing-md);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius);
  resize: vertical;
  min-height: 100px;
  font-size: 0.875rem;
  margin-bottom: var(--spacing-sm);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  color: var(--text-primary);
  transition: all var(--transition);
}

.comment-input textarea:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-light);
}

.comment-input-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.char-count {
  font-size: 0.75rem;
  color: var(--text-muted);
}

/* 登录提示 */
.login-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-md);
  text-align: center;
  padding: var(--spacing-xl);
  margin-bottom: var(--spacing-xl);
  border-radius: var(--radius-md);
  background: var(--surface);
  border: 1px solid var(--border);
  color: var(--text-muted);
}

.login-hint a {
  color: var(--primary);
  font-weight: 500;
}

/* 评论列表 */
.comment-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.comment-item {
  display: flex;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
  border-radius: var(--radius-md);
  background: var(--surface);
  border: 1px solid var(--border);
  transition: all var(--transition);
}

.comment-item:hover {
  background: var(--glass-hover);
  box-shadow: var(--shadow-sm);
}

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  object-fit: cover;
  flex-shrink: 0;
  border: 2px solid var(--glass-border);
}

.comment-body {
  flex: 1;
  min-width: 0;
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

.comment-author:hover {
  color: var(--primary);
}

.comment-time {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.comment-content {
  font-size: 0.875rem;
  color: var(--text-primary);
  line-height: 1.6;
  word-break: break-word;
}

.comment-actions {
  display: flex;
  gap: var(--spacing-md);
  margin-top: var(--spacing-sm);
}

.reply-btn,
.delete-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 0.75rem;
  color: var(--text-muted);
  background: none;
  border: none;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: var(--radius-xs);
  transition: all var(--transition);
}

.reply-btn:hover {
  color: var(--primary);
  background: var(--primary-light);
}

.delete-btn:hover {
  color: var(--error);
  background: var(--error-light);
}

/* 回复列表 */
.reply-list {
  margin-top: var(--spacing-md);
  padding-left: var(--spacing-md);
  border-left: 2px solid var(--glass-border);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.reply-item {
  display: flex;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius);
  background: var(--glass-bg);
  border: 1px solid var(--border);
}

.reply-avatar {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-full);
  object-fit: cover;
  flex-shrink: 0;
}

.reply-body {
  flex: 1;
  min-width: 0;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: 2px;
}

.reply-author {
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--text-primary);
  text-decoration: none;
}

.reply-author:hover {
  color: var(--primary);
}

.reply-time {
  font-size: 0.6875rem;
  color: var(--text-muted);
}

.reply-content {
  font-size: 0.8125rem;
  color: var(--text-primary);
  line-height: 1.5;
}

/* 空状态 */
.empty-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-md);
  text-align: center;
  padding: var(--spacing-2xl);
  color: var(--text-muted);
}

.error-hint {
  color: var(--error);
}

/* 回复指示器 */
.reply-indicator {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--primary-light);
  border-radius: var(--radius) var(--radius) 0 0;
  font-size: 0.8125rem;
  color: var(--primary);
  margin: calc(-1 * var(--spacing-md)) calc(-1 * var(--spacing-md)) var(--spacing-md);
}

.reply-indicator strong {
  font-weight: 600;
}

.cancel-reply-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  font-size: 0.75rem;
  padding: 2px 6px;
  border-radius: var(--radius-xs);
  transition: all var(--transition);
}

.cancel-reply-btn:hover {
  color: var(--error);
  background: var(--error-light);
}

/* 侧边栏 */
.post-sidebar {
  width: 280px;
  flex-shrink: 0;
  position: sticky;
  top: 80px;
  height: fit-content;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.toc-card,
.info-card {
  padding: var(--spacing-lg);
  border-radius: var(--radius-lg);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border-wet);
  box-shadow: var(--glass-shadow-wet);
  transition: all var(--transition);
}

.toc-card:hover,
.info-card:hover {
  background: var(--glass-hover);
  box-shadow: var(--shadow-md);
}

.sidebar-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: 0.9375rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
  padding-bottom: var(--spacing-sm);
  border-bottom: 1px solid var(--glass-border);
}

/* 目录导航 */
.toc-nav {
  display: flex;
  flex-direction: column;
  gap: 2px;
  max-height: 400px;
  overflow-y: auto;
}

.toc-link {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-xs) var(--spacing-sm);
  font-size: 0.8125rem;
  color: var(--text-secondary);
  text-decoration: none;
  border-radius: var(--radius-sm);
  transition: all var(--transition);
  position: relative;
}

.toc-indicator {
  width: 3px;
  height: 0;
  background: var(--primary);
  border-radius: var(--radius-full);
  transition: all var(--transition);
  flex-shrink: 0;
}

.toc-link:hover {
  background: var(--primary-light);
  color: var(--primary);
}

.toc-link:hover .toc-indicator {
  height: 60%;
}

.toc-link.active {
  background: var(--primary-light);
  color: var(--primary);
  font-weight: 600;
}

.toc-link.active .toc-indicator {
  height: 100%;
}

.toc-link.level-2 { padding-left: var(--spacing-md); }
.toc-link.level-3 { padding-left: var(--spacing-lg); }
.toc-link.level-4 { padding-left: var(--spacing-xl); }

/* 文章信息列表 */
.info-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.875rem;
  padding: var(--spacing-xs) 0;
}

.info-label {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  color: var(--text-muted);
}

.info-value {
  font-weight: 700;
  color: var(--text-primary);
  background: var(--primary-light);
  padding: 2px 8px;
  border-radius: var(--radius-full);
  font-size: 0.8125rem;
}

/* Loading & Error States */
.loading-state,
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-md);
  padding: var(--spacing-3xl);
  border-radius: var(--radius-lg);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border-wet);
}

.loading-state {
  color: var(--text-muted);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--border);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-state h2 {
  color: var(--text-primary);
  font-size: 1.25rem;
}

.error-state svg {
  color: var(--error);
  opacity: 0.6;
}

.error-actions {
  display: flex;
  gap: var(--spacing-sm);
}

.btn-outline {
  padding: 0.5rem 1rem;
  border: 1px solid var(--glass-border);
  border-radius: var(--radius);
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 0.875rem;
  transition: all var(--transition);
  text-decoration: none;
  display: inline-flex;
  align-items: center;
}

.btn-outline:hover {
  border-color: var(--primary);
  color: var(--primary);
  background: var(--primary-light);
}

/* 响应式 */
@media (max-width: 992px) {
  .post-main {
    flex-direction: column;
  }
  
  .post-sidebar {
    width: 100%;
    position: static;
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: var(--spacing-md);
  }
}

@media (max-width: 768px) {
  .post-sidebar {
    display: flex;
    flex-direction: column;
    gap: var(--spacing-md);
  }
  
  .post-title {
    font-size: 1.625rem;
  }
  
  .post-actions {
    flex-wrap: wrap;
    gap: var(--spacing-sm);
  }

  .post-cover {
    height: 200px;
  }

  .author-card {
    flex-direction: column;
    gap: var(--spacing-md);
    align-items: flex-start;
  }

  .author-actions {
    width: 100%;
    justify-content: flex-start;
  }
}

@media (max-width: 480px) {
  .post-cover {
    height: 160px;
  }

  .post-content {
    padding: var(--spacing-md);
  }

  .comment-section {
    padding: var(--spacing-md);
  }

  .post-title {
    font-size: 1.375rem;
  }
}
</style>
