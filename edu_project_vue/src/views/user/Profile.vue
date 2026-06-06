<template>
  <div class="profile-page">
    <button class="back-btn" @click="goBack">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
      返回
    </button>

    <div v-if="pageError" class="error-card card">
      <div class="error-card-body">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="error-icon"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
        <p>{{ pageError }}</p>
        <button class="btn btn-primary" @click="initLoad">重新加载</button>
      </div>
    </div>

    <template v-else>
      <div class="hero-section card">
        <div class="hero-cover" @scroll="handleCoverScroll" ref="coverRef">
          <img :src="getSafeImageUrl(user?.coverImage, '/default-cover.jpg')" alt="" class="cover-image" :style="{ transform: `translateY(${coverScrollY * 0.3}px)` }" />
          <div class="hero-gradient"></div>
          <button class="cover-upload-btn" title="更换封面图" @click="triggerCoverUpload" :disabled="coverUploading">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
              <circle cx="12" cy="13" r="4"/>
            </svg>
          </button>
          <input type="file" accept="image/jpeg,image/png,image/gif,image/webp" ref="coverInputRef" hidden @change="handleCoverUpload" />
        </div>
        <div class="hero-info">
          <div v-if="loading.user" class="hero-skeleton">
            <div class="skeleton avatar-skeleton"></div>
            <div class="skeleton skeleton-title"></div>
            <div class="skeleton skeleton-bio"></div>
            <div class="skeleton skeleton-stats-row"></div>
          </div>
          <template v-else>
            <div class="hero-avatar-wrapper">
              <img :src="user?.avatar || '/default-avatar.png'" :alt="user?.nickname" class="hero-avatar" @error="onAvatarError" />
            </div>
            <h1 class="hero-name">{{ user?.nickname || user?.username }}</h1>
            <p class="hero-bio">{{ user?.bio || '这个人很懒，什么都没写' }}</p>
            <div class="hero-stats">
              <div class="stat-card glass">
                <span class="stat-value">{{ stats.postCount || 0 }}</span>
                <span class="stat-label">文章</span>
              </div>
              <div class="stat-card glass">
                <span class="stat-value">{{ stats.likeCount || 0 }}</span>
                <span class="stat-label">获赞</span>
              </div>
              <router-link to="/followers" class="stat-card glass stat-link">
                <span class="stat-value">{{ stats.followerCount || 0 }}</span>
                <span class="stat-label">粉丝</span>
              </router-link>
              <router-link to="/following" class="stat-card glass stat-link">
                <span class="stat-value">{{ stats.followingCount || 0 }}</span>
                <span class="stat-label">关注</span>
              </router-link>
            </div>
          </template>
          <div class="hero-actions">
            <router-link to="/profile-edit" class="btn btn-primary">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
              编辑资料
            </router-link>
            <router-link to="/password-change" class="btn btn-ghost">修改密码</router-link>
          </div>
        </div>
      </div>

      <div class="tabs-section card">
        <div class="tabs-header" ref="tabsRef">
          <button v-for="(tab, i) in tabs" :key="tab.key" :ref="el => { if (el) tabEls[i] = el }" class="tab-btn" :class="{ active: activeTab === tab.key }" @click="switchTab(tab.key, i)">
            {{ tab.label }}
          </button>
          <div class="tabs-slider" :style="sliderStyle"></div>
        </div>
        <div class="tabs-body">
            <div v-show="activeTab === 'posts'" class="tab-panel" key="posts">
              <div v-if="loading.posts" class="skeleton-list">
                <div v-for="n in 3" :key="n" class="skeleton-card-item">
                  <div class="skeleton skeleton-card-title"></div>
                  <div class="skeleton skeleton-card-text"></div>
                  <div class="skeleton skeleton-card-text short"></div>
                  <div class="skeleton skeleton-card-meta"></div>
                </div>
              </div>
              <div v-else-if="postsError" class="tab-error">
                <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                <p>{{ postsError }}</p>
                <button class="btn btn-sm btn-primary" @click="fetchPosts">重试</button>
              </div>
              <div v-else-if="posts.length === 0" class="empty-state">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" class="empty-icon"><path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z"/><polyline points="14 2 14 8 20 8"/></svg>
                <p class="empty-title">还没有发表文章</p>
                <p class="empty-text">写下你的第一篇博客，分享你的想法</p>
                <router-link to="/post-edit" class="btn btn-primary">写文章</router-link>
              </div>
              <div v-else>
                <div v-for="post in posts" :key="post.id" class="post-card">
                  <div class="post-card-body">
                    <h3 class="post-card-title">
                      <router-link :to="`/post/${post.id}`">{{ post.title }}</router-link>
                    </h3>
                    <p class="post-card-excerpt">{{ truncateText(post.summary, 120) }}</p>
                    <div class="post-card-meta">
                      <span>
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                        {{ formatRelativeTime(post.createTime) }}
                      </span>
                      <span>
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                        {{ post.viewCount || 0 }}
                      </span>
                      <span>
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg>
                        {{ post.likeCount || 0 }}
                      </span>
                      <span>
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                        {{ post.commentCount || 0 }}
                      </span>
                    </div>
                  </div>
                  <div class="post-card-actions">
                    <router-link :to="`/post-edit/${post.id}`" class="post-action-btn edit">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                      编辑
                    </router-link>
                    <button class="post-action-btn delete" @click="openConfirm('删除文章', `确定删除「${post.title}」吗？删除后不可恢复。`, () => deletePost(post.id))">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                      删除
                    </button>
                  </div>
                </div>
                <div v-if="postsTotalPages > 1" class="pagination-section">
                  <div class="pagination">
                    <button class="pagination-btn" :disabled="postsPage <= 1" @click="postsPage--; fetchPosts()">上一页</button>
                    <button v-for="p in postsTotalPages" :key="p" class="pagination-btn" :class="{ active: p === postsPage }" @click="postsPage = p; fetchPosts()">{{ p }}</button>
                    <button class="pagination-btn" :disabled="postsPage >= postsTotalPages" @click="postsPage++; fetchPosts()">下一页</button>
                  </div>
                  <span class="pagination-info">共 {{ postsTotal }} 篇</span>
                </div>
              </div>
            </div>

            <div v-show="activeTab === 'likes'" class="tab-panel" key="likes">
              <div v-if="loading.likes" class="skeleton-list">
                <div v-for="n in 3" :key="n" class="skeleton-card-item">
                  <div class="skeleton skeleton-card-title"></div>
                  <div class="skeleton skeleton-card-text"></div>
                  <div class="skeleton skeleton-card-meta"></div>
                </div>
              </div>
              <div v-else-if="likesError" class="tab-error">
                <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                <p>{{ likesError }}</p>
                <button class="btn btn-sm btn-primary" @click="fetchLikes">重试</button>
              </div>
              <div v-else-if="likes.length === 0" class="empty-state">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" class="empty-icon"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg>
                <p class="empty-title">还没有点赞过文章</p>
                <p class="empty-text">浏览文章，为你喜欢的内容点赞</p>
                <router-link to="/" class="btn btn-primary">去发现</router-link>
              </div>
              <div v-else>
                <div v-for="like in likes" :key="like.likeId" class="post-card">
                  <div class="post-card-body">
                    <h3 class="post-card-title">
                      <router-link :to="`/post/${like.postId}`">{{ like.title }}</router-link>
                    </h3>
                    <p class="post-card-excerpt">{{ truncateText(like.summary, 120) }}</p>
                    <div class="post-card-meta">
                      <span>
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg>
                        点赞于 {{ formatRelativeTime(like.likeTime) }}
                      </span>
                    </div>
                  </div>
                </div>
                <div v-if="likesTotalPages > 1" class="pagination-section">
                  <div class="pagination">
                    <button class="pagination-btn" :disabled="likesPage <= 1" @click="likesPage--; fetchLikes()">上一页</button>
                    <button v-for="p in likesTotalPages" :key="p" class="pagination-btn" :class="{ active: p === likesPage }" @click="likesPage = p; fetchLikes()">{{ p }}</button>
                    <button class="pagination-btn" :disabled="likesPage >= likesTotalPages" @click="likesPage++; fetchLikes()">下一页</button>
                  </div>
                  <span class="pagination-info">共 {{ likesTotal }} 条</span>
                </div>
              </div>
            </div>

            <div v-show="activeTab === 'comments'" class="tab-panel" key="comments">
              <div v-if="loading.comments" class="skeleton-list">
                <div v-for="n in 3" :key="n" class="skeleton-card-item">
                  <div class="skeleton skeleton-card-title"></div>
                  <div class="skeleton skeleton-card-text"></div>
                  <div class="skeleton skeleton-card-meta"></div>
                </div>
              </div>
              <div v-else-if="commentsError" class="tab-error">
                <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                <p>{{ commentsError }}</p>
                <button class="btn btn-sm btn-primary" @click="fetchComments">重试</button>
              </div>
              <div v-else-if="comments.length === 0" class="empty-state">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" class="empty-icon"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                <p class="empty-title">还没有发表过评论</p>
                <p class="empty-text">在文章下方留下你的见解</p>
              </div>
              <div v-else>
                <div v-for="comment in comments" :key="comment.id" class="comment-card">
                  <div class="comment-bubble">{{ comment.content }}</div>
                  <div class="comment-meta">
                    <span>评论于</span>
                    <router-link :to="`/post/${comment.postId}`" class="comment-post-title">{{ comment.postTitle }}</router-link>
                    <span class="comment-dot">·</span>
                    <span>{{ formatRelativeTime(comment.createTime) }}</span>
                  </div>
                </div>
                <div v-if="commentsTotalPages > 1" class="pagination-section">
                  <div class="pagination">
                    <button class="pagination-btn" :disabled="commentsPage <= 1" @click="commentsPage--; fetchComments()">上一页</button>
                    <button v-for="p in commentsTotalPages" :key="p" class="pagination-btn" :class="{ active: p === commentsPage }" @click="commentsPage = p; fetchComments()">{{ p }}</button>
                    <button class="pagination-btn" :disabled="commentsPage >= commentsTotalPages" @click="commentsPage++; fetchComments()">下一页</button>
                  </div>
                  <span class="pagination-info">共 {{ commentsTotal }} 条</span>
                </div>
              </div>
            </div>

            <div v-show="activeTab === 'circle'" class="tab-panel" key="circle">
              <div v-if="loading.circles" class="skeleton-list">
                <div v-for="n in 3" :key="n" class="skeleton-card-item">
                  <div class="skeleton skeleton-card-title"></div>
                  <div class="skeleton skeleton-card-text"></div>
                  <div class="skeleton skeleton-card-text short"></div>
                </div>
              </div>
              <div v-else-if="circlesError" class="tab-error">
                <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                <p>{{ circlesError }}</p>
                <button class="btn btn-sm btn-primary" @click="fetchCircles">重试</button>
              </div>
              <div v-else-if="circles.length === 0" class="empty-state">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" class="empty-icon"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                <p class="empty-title">还没有发布校友圈动态</p>
                <p class="empty-text">分享你的校园生活点滴</p>
                <router-link to="/circle" class="btn btn-primary">去发布</router-link>
              </div>
              <div v-else>
                <div v-for="post in circles" :key="post.id" class="post-card">
                  <div class="post-card-body">
                    <p class="post-card-excerpt">{{ truncateText(post.content, 120) }}</p>
                    <div class="post-card-meta">
                      <span>
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg>
                        {{ post.likeCount || 0 }}
                      </span>
                      <span>
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                        {{ post.commentCount || 0 }}
                      </span>
                      <span>{{ formatRelativeTime(post.createTime) }}</span>
                      <router-link :to="`/circle/${post.id}`" class="post-action-link">查看详情</router-link>
                    </div>
                  </div>
                </div>
                <div v-if="circlesTotalPages > 1" class="pagination-section">
                  <div class="pagination">
                    <button class="pagination-btn" :disabled="circlesPage <= 1" @click="circlesPage--; fetchCircles()">上一页</button>
                    <button v-for="p in circlesTotalPages" :key="p" class="pagination-btn" :class="{ active: p === circlesPage }" @click="circlesPage = p; fetchCircles()">{{ p }}</button>
                    <button class="pagination-btn" :disabled="circlesPage >= circlesTotalPages" @click="circlesPage++; fetchCircles()">下一页</button>
                  </div>
                  <span class="pagination-info">共 {{ circlesTotal }} 条</span>
                </div>
              </div>
            </div>
        </div>
      </div>
    </template>

    <ConfirmDialog />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { userApi } from '../../api/user'
import { postApi } from '../../api/post'
import { likeApi } from '../../api/like'
import { commentApi } from '../../api/comment'
import { followApi } from '../../api/follow'
import { mediaApi } from '../../api/media'
import { circleApi } from '../../api/circle'
import { formatRelativeTime, truncateText, getSafeImageUrl } from '../../utils'
import { useLogger } from '../../utils/logger'
import { toast } from '../../utils/toast'
import { useConfirm } from '../../composables/useConfirm'

const userStore = useUserStore()
const router = useRouter()
const logger = useLogger('Profile')

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}

const pageError = ref('')
const user = ref<any>(null)
const stats = ref<Record<string, any>>({})

const coverUploading = ref(false)
const coverInputRef = ref<HTMLInputElement | null>(null)
const coverRef = ref<HTMLElement | null>(null)
const coverScrollY = ref(0)

const loading = reactive({ user: false, posts: false, likes: false, comments: false, circles: false })

const posts = ref<any[]>([])
const likes = ref<any[]>([])
const comments = ref<any[]>([])

const postsPage = ref(1)
const postsTotal = ref(0)
const postsTotalPages = ref(1)
const pageSize = 10

const likesPage = ref(1)
const likesTotal = ref(0)
const likesTotalPages = ref(1)

const commentsPage = ref(1)
const commentsTotal = ref(0)
const commentsTotalPages = ref(1)

const postsError = ref('')
const likesError = ref('')
const commentsError = ref('')

const circles = ref<any[]>([])
const circlesPage = ref(1)
const circlesTotal = ref(0)
const circlesTotalPages = ref(1)
const circlesError = ref('')

const activeTab = ref('posts')
const activeTabIndex = ref(0)
const tabEls = ref<any[]>([])
const tabs = [
  { key: 'posts', label: '我的文章' },
  { key: 'circle', label: '校友圈' },
  { key: 'likes', label: '我的点赞' },
  { key: 'comments', label: '我的评论' }
]

const { confirm, ConfirmDialog } = useConfirm()

const sliderStyle = computed(() => {
  const el = tabEls.value[activeTabIndex.value]
  if (!el) return { width: '0px' }
  return {
    width: `${el.offsetWidth}px`,
    transform: `translateX(${el.offsetLeft}px)`
  }
})

function handleCoverScroll() {
  if (coverRef.value) {
    coverScrollY.value = coverRef.value.scrollTop || 0
  }
}

function onAvatarError(e: Event) {
  (e.target as HTMLImageElement).src = '/default-avatar.png'
}

const triggerCoverUpload = () => {
  coverInputRef.value?.click()
}

const handleCoverUpload = async (e: Event) => {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  coverUploading.value = true
  try {
    const res = await mediaApi.uploadFile(file, 'cover')
    const uploadData = res.data as any
    if (uploadData?.fileUrl) {
      await userApi.updateCoverImage(uploadData.fileUrl)
      if (user.value) user.value.coverImage = uploadData.fileUrl
      toast.success('封面图更新成功')
    }
  } catch (err: any) {
    logger.error('cover upload error', { error: err.message })
    toast.error(err.response?.data?.message || '封面上传失败')
  } finally {
    coverUploading.value = false
    if (coverInputRef.value) coverInputRef.value.value = ''
  }
}

async function openConfirm(title: string, message: string, onConfirm: () => void) {
  const ok = await confirm(message, title)
  if (ok) onConfirm()
}

function switchTab(key: string, index: number) {
  activeTab.value = key
  activeTabIndex.value = index
  if (key === 'posts') fetchPosts()
  else if (key === 'circle') fetchCircles()
  else if (key === 'likes') fetchLikes()
  else if (key === 'comments') fetchComments()
}

async function initLoad() {
  pageError.value = ''
  loading.user = true
  try {
    const [userResponse, statsResponse] = await Promise.all([
      userApi.getUserById(userStore.userId!),
      followApi.getFollowCounts(userStore.userId!)
    ])
    user.value = userResponse.data

    const countsData = (statsResponse.data || {}) as any
    let postCount = 0
    let likeCount = 0
    try {
      const postsResp = await postApi.getMyPosts({ pageNum: 1, pageSize: 1 })
      postCount = (postsResp.data as any)?.total || 0
    } catch { /* ignore */ }
    try {
      const likesResp = await likeApi.getMyLikes({ pageNum: 1, pageSize: 1 })
      likeCount = (likesResp.data as any)?.total || 0
    } catch { /* ignore */ }
    stats.value = {
      postCount,
      likeCount,
      ...countsData
    }
  } catch (error: any) {
    logger.error('Failed to fetch user', { error: error.message })
    pageError.value = '加载用户信息失败，请稍后重试'
  } finally {
    loading.user = false
  }
}

async function fetchPosts() {
  loading.posts = true
  postsError.value = ''
  try {
    const response = await postApi.getMyPosts({ pageNum: postsPage.value, pageSize })
    const data = (response.data || {}) as any
    posts.value = data.records || []
    postsTotal.value = data.total || 0
    postsTotalPages.value = data.pages || 1
  } catch (error: any) {
    logger.error('Failed to fetch posts', { error: error.message })
    postsError.value = error.response?.data?.message || '加载文章列表失败'
  } finally {
    loading.posts = false
  }
}

async function fetchLikes() {
  loading.likes = true
  likesError.value = ''
  try {
    const response = await likeApi.getMyLikes({ pageNum: likesPage.value, pageSize })
    const data = (response.data || {}) as any
    likes.value = data.records || []
    likesTotal.value = data.total || 0
    likesTotalPages.value = data.pages || 1
  } catch (error: any) {
    logger.error('Failed to fetch likes', { error: error.message })
    likesError.value = error.response?.data?.message || '加载点赞列表失败'
  } finally {
    loading.likes = false
  }
}

async function fetchComments() {
  loading.comments = true
  commentsError.value = ''
  try {
    const response = await commentApi.getMyComments({ pageNum: commentsPage.value, pageSize })
    const data = (response.data || {}) as any
    comments.value = data.records || []
    commentsTotal.value = data.total || 0
    commentsTotalPages.value = data.pages || 1
  } catch (error: any) {
    logger.error('Failed to fetch comments', { error: error.message })
    commentsError.value = error.response?.data?.message || '加载评论列表失败'
  } finally {
    loading.comments = false
  }
}

const fetchCircles = async () => {
  if (!user.value?.id) return
  loading.circles = true
  circlesError.value = ''
  try {
    const res = await circleApi.getUserPosts(user.value.id, { pageNum: circlesPage.value, pageSize })
    const data = res.data || {}
    if (Array.isArray(data)) {
      circles.value = data
      circlesTotal.value = data.length
      circlesTotalPages.value = 1
    } else {
      circles.value = data.records || []
      circlesTotal.value = data.total || 0
      circlesTotalPages.value = data.pages || 1
    }
  } catch (err: any) {
    logger.error('fetch circles error', { error: err.message })
    circlesError.value = err.response?.data?.message || '加载失败'
    circles.value = []
  } finally {
    loading.circles = false
  }
}

async function deletePost(postId: number) {
  try {
    await postApi.deletePost(postId)
    posts.value = posts.value.filter(p => p.id !== postId)
    postsTotal.value = Math.max(0, postsTotal.value - 1)
    if (posts.value.length === 0 && postsPage.value > 1) {
      postsPage.value--
      await fetchPosts()
    }
    toast.success('删除成功')
  } catch (error: any) {
    logger.error('Failed to delete post', { error: error.message })
    toast.error(error.response?.data?.message || '删除失败')
  }
}

onMounted(() => {
  initLoad()
  nextTick(() => fetchPosts())
})
</script>

<style scoped>
.profile-page {
  grid-column: 1 / -1;
  max-width: var(--container-xl);
  margin: 0 auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius);
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all var(--transition);
  width: fit-content;
  box-shadow: var(--glass-shadow);
}

.back-btn:hover {
  background: var(--glass-hover);
  color: var(--primary);
  border-color: var(--primary);
  transform: translateY(-1px);
}

.hero-section {
  overflow: visible;
  border-radius: var(--radius-xl);
}

.hero-cover {
  position: relative;
  height: 220px;
  overflow: hidden;
  border-radius: var(--radius-xl) var(--radius-xl) 0 0;
}

.cover-image {
  width: 100%;
  height: 120%;
  object-fit: cover;
  will-change: transform;
  transition: transform 0.1s linear;
}

.hero-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 30%, rgba(0,0,0,0.6) 100%);
}

.cover-upload-btn {
  position: absolute;
  bottom: 16px;
  right: 16px;
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  background: rgba(0,0,0,0.5);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  color: #fff;
  border: 1px solid rgba(255,255,255,0.2);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all var(--transition);
  z-index: 2;
}

.hero-cover:hover .cover-upload-btn {
  opacity: 1;
}

.cover-upload-btn:hover {
  background: rgba(0,0,0,0.7);
  transform: scale(1.05);
}

.cover-upload-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.hero-info {
  padding: 0 28px 28px;
  position: relative;
}

.hero-avatar-wrapper {
  margin-top: -52px;
  margin-bottom: 14px;
  position: relative;
  display: inline-block;
}

.hero-avatar {
  width: 104px;
  height: 104px;
  border-radius: var(--radius-full);
  object-fit: cover;
  border: 4px solid var(--surface-solid);
  box-shadow: var(--shadow-lg);
  transition: transform var(--transition);
}

.hero-avatar:hover {
  transform: scale(1.03);
}

.hero-name {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.hero-bio {
  font-size: 0.9375rem;
  color: var(--text-secondary);
  margin-bottom: 20px;
  line-height: 1.5;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 14px 10px;
  border-radius: var(--radius-md);
  transition: all var(--transition);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.stat-link {
  text-decoration: none;
  color: inherit;
}

.stat-value {
  display: block;
  font-size: 1.375rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 0.75rem;
  color: var(--text-muted);
  margin-top: 2px;
}

.hero-actions {
  display: flex;
  gap: 10px;
}

.hero-skeleton {
  padding: 12px 0;
}

.avatar-skeleton {
  width: 104px;
  height: 104px;
  border-radius: var(--radius-full);
  margin-bottom: 16px;
}

.skeleton-title {
  width: 180px;
  height: 28px;
  margin-bottom: 10px;
}

.skeleton-bio {
  width: 280px;
  height: 16px;
  margin-bottom: 18px;
}

.skeleton-stats-row {
  width: 360px;
  height: 48px;
  margin-bottom: 16px;
}

.tabs-section {
  border-radius: var(--radius-xl);
}

.tabs-header {
  display: flex;
  position: relative;
  border-bottom: 1px solid var(--glass-border);
  padding: 0 8px;
}

.tab-btn {
  flex: 1;
  padding: 16px 20px;
  font-size: 0.9375rem;
  font-weight: 500;
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  transition: color var(--transition);
  position: relative;
  z-index: 1;
}

.tab-btn:hover {
  color: var(--primary);
}

.tab-btn.active {
  color: var(--primary);
}

.tabs-slider {
  position: absolute;
  bottom: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--primary-start), var(--primary-end));
  border-radius: 3px 3px 0 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 2;
}

.tabs-body {
  padding: 24px;
  min-height: 200px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--transition);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.skeleton-card-item {
  padding: 18px;
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-md);
  background: var(--surface);
}

.skeleton-card-title {
  width: 60%;
  height: 20px;
  margin-bottom: 10px;
}

.skeleton-card-text {
  width: 90%;
  height: 14px;
  margin-bottom: 6px;
}

.skeleton-card-text.short {
  width: 50%;
}

.skeleton-card-meta {
  width: 200px;
  height: 14px;
  margin-top: 10px;
}

.post-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 18px 0;
  border-bottom: 1px solid var(--glass-border);
  gap: 16px;
}

.post-card:last-child {
  border-bottom: none;
}

.post-card-body {
  flex: 1;
  min-width: 0;
}

.post-card-title {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 6px;
  line-height: 1.4;
}

.post-card-title a {
  color: var(--text-primary);
  text-decoration: none;
  transition: color var(--transition);
}

.post-card-title a:hover {
  color: var(--primary);
}

.post-card-excerpt {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-card-meta {
  display: flex;
  gap: 16px;
  font-size: 0.75rem;
  color: var(--text-muted);
  flex-wrap: wrap;
}

.post-card-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.post-card-actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
  padding-top: 4px;
}

.post-action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  font-size: 0.75rem;
  font-weight: 500;
  border: none;
  border-radius: var(--radius);
  cursor: pointer;
  transition: all var(--transition);
  text-decoration: none;
  background: transparent;
  color: var(--text-muted);
}

.post-action-link {
  font-weight: 500;
  color: var(--primary);
  text-decoration: none;
  font-size: 0.75rem;
}

.post-action-link:hover {
  text-decoration: underline;
}

.post-action-btn.edit:hover {
  background: var(--primary-light);
  color: var(--primary);
}

.post-action-btn.delete:hover {
  background: var(--error-light);
  color: var(--error);
}

.comment-card {
  padding: 16px 0;
  border-bottom: 1px solid var(--glass-border);
}

.comment-card:last-child {
  border-bottom: none;
}

.comment-bubble {
  font-size: 0.875rem;
  color: var(--text-primary);
  line-height: 1.6;
  margin-bottom: 8px;
  padding: 12px 16px;
  background: var(--surface);
  border-radius: var(--radius-md);
  border: 1px solid var(--glass-border);
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.75rem;
  color: var(--text-muted);
  flex-wrap: wrap;
}

.comment-post-title {
  font-weight: 500;
  color: var(--primary);
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.comment-dot {
  color: var(--text-muted);
}

.tab-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 48px 20px;
  color: var(--text-muted);
}

.tab-error p {
  font-size: 0.875rem;
}

.error-card {
  text-align: center;
  border-radius: var(--radius-xl);
}

.error-card-body {
  padding: 60px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.error-icon {
  color: var(--text-muted);
  opacity: 0.5;
}

.error-card-body p {
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.empty-state {
  padding: 60px 24px;
  text-align: center;
}

.empty-icon {
  color: var(--text-muted);
  opacity: 0.3;
  margin-bottom: 16px;
}

.empty-title {
  font-size: 1.0625rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.empty-text {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-bottom: 20px;
}

.pagination-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding-top: 24px;
  border-top: 1px solid var(--glass-border);
  margin-top: 8px;
}

.pagination-info {
  font-size: 0.75rem;
  color: var(--text-muted);
}

@media (max-width: 768px) {
  .profile-page {
    padding: 16px;
    gap: 16px;
  }

  .hero-cover {
    height: 150px;
  }

  .cover-image {
    height: 130%;
  }

  .hero-avatar {
    width: 80px;
    height: 80px;
  }

  .avatar-skeleton {
    width: 80px;
    height: 80px;
  }

  .hero-info {
    padding: 0 18px 18px;
  }

  .hero-stats {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }

  .stat-value {
    font-size: 1.125rem;
  }

  .tabs-body {
    padding: 16px;
  }

  .post-card {
    flex-direction: column;
  }

  .post-card-actions {
    width: 100%;
    justify-content: flex-end;
    padding-top: 10px;
    border-top: 1px solid var(--glass-border);
  }
}

@media (max-width: 480px) {
  .hero-name {
    font-size: 1.25rem;
  }

  .hero-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
