<template>
  <div class="user-profile-page">
    <button class="back-btn" @click="goBack">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
      返回
    </button>
    <div v-if="pageError" class="error-card card">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="error-icon"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
      <p>{{ pageError }}</p>
      <button class="btn btn-primary" @click="initLoad">重新加载</button>
    </div>

    <template v-else>
      <div class="hero-section card">
        <div class="hero-cover">
          <img :src="getSafeImageUrl(user?.coverImage, '/default-cover.jpg')" alt="" />
          <div class="hero-gradient"></div>
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
            <p class="hero-bio" v-html="sanitizeText(user?.bio) || '这个人很懒，什么都没写'"></p>
            <div class="hero-stats">
              <div class="stat-item">
                <span class="stat-value">{{ stats.postCount || 0 }}</span>
                <span class="stat-label">文章</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ stats.likeCount || 0 }}</span>
                <span class="stat-label">获赞</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ stats.followerCount || 0 }}</span>
                <span class="stat-label">粉丝</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ stats.followingCount || 0 }}</span>
                <span class="stat-label">关注</span>
              </div>
            </div>
          </template>
          <div v-if="userStore.isLoggedIn && String(userStore.userId) !== String(route.params.id)" class="hero-actions">
            <button class="btn" :class="isFollowing ? 'btn-secondary' : 'btn-primary'" @click="toggleFollow" :disabled="followLoading">
              <template v-if="followLoading">
                <span class="loading-dots"></span>
              </template>
              <template v-else>
                <svg v-if="isFollowing" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="18" y1="8" x2="23" y2="13"/><line x1="23" y1="8" x2="18" y2="13"/></svg>
                <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/></svg>
                {{ isFollowing ? '已关注' : '关注' }}
              </template>
            </button>
            <button class="btn btn-ghost" @click="sendMessage">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
              私信
            </button>
          </div>
        </div>
      </div>

      <div class="section-card card">
        <div class="section-tabs">
          <button class="section-tab" :class="{ active: activeTab === 'posts' }" @click="activeTab = 'posts'; fetchPosts()">文章</button>
          <button class="section-tab" :class="{ active: activeTab === 'circle' }" @click="activeTab = 'circle'; fetchCircles()">校友圈</button>
        </div>
        <div v-if="activeTab === 'posts'">
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
            <p class="empty-title">TA还没有发表文章</p>
          </div>
          <div v-else>
            <div v-for="post in posts" :key="post.id" class="post-card">
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
        <div v-else>
          <div v-if="loading.circles" class="skeleton-list">
            <div v-for="n in 3" :key="n" class="skeleton-card-item">
              <div class="skeleton skeleton-card-title"></div>
              <div class="skeleton skeleton-card-text"></div>
              <div class="skeleton skeleton-card-meta"></div>
            </div>
          </div>
          <div v-else-if="circlesError" class="tab-error">
            <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            <p>{{ circlesError }}</p>
            <button class="btn btn-sm btn-primary" @click="fetchCircles">重试</button>
          </div>
          <div v-else-if="circles.length === 0" class="empty-state">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" class="empty-icon"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
            <p class="empty-title">TA还没有发布校友圈动态</p>
          </div>
          <div v-else>
            <div v-for="post in circles" :key="post.id" class="post-card">
              <p class="post-card-excerpt">{{ truncateText(post.content, 120) }}</p>
              <div v-if="post.mediaUrls && post.mediaUrls.length > 0" class="post-media-grid">
                <img v-for="(url, idx) in post.mediaUrls" :key="idx" :src="url" class="post-media-thumb" @click="previewImage(url)" />
              </div>
              <div class="post-card-meta">
                <span>
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                  {{ formatRelativeTime(post.createTime) }}
                </span>
                <span>
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg>
                  {{ post.likeCount || 0 }}
                </span>
                <span>
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                  {{ post.commentCount || 0 }}
                </span>
                <router-link :to="`/circle/${post.id}`" class="post-action-link">查看详情</router-link>
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
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import DOMPurify from 'dompurify'
import { useUserStore } from '../../stores/user'
import { userApi } from '../../api/user'
import { postApi } from '../../api/post'
import { followApi } from '../../api/follow'
import { circleApi } from '../../api/circle'
import { formatRelativeTime, truncateText, getSafeImageUrl } from '../../utils'
import { useLogger } from '../../utils/logger'
import { toast } from '../../utils/toast'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const logger = useLogger('UserProfile')

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}

// XSS防护：对用户生成内容进行净化
const sanitizeText = (text) => {
  if (!text) return ''
  return DOMPurify.sanitize(text, { ALLOWED_TAGS: [] })
}

const pageError = ref('')
const user = ref(null)
const stats = ref({})
const isFollowing = ref(false)
const followLoading = ref(false)

const loading = reactive({ user: false, posts: false, circles: false })
const activeTab = ref('posts')

const posts = ref([])
const postsPage = ref(1)
const postsTotal = ref(0)
const postsTotalPages = ref(1)
const postsError = ref('')

const circles = ref([])
const circlesPage = ref(1)
const circlesTotal = ref(0)
const circlesTotalPages = ref(1)
const circlesError = ref('')

const pageSize = 10

function onAvatarError(e) {
  e.target.src = '/default-avatar.png'
}

async function initLoad() {
  if (userStore.isLoggedIn && String(userStore.userId) === String(route.params.id)) {
    router.replace('/profile')
    return
  }
  pageError.value = ''
  loading.user = true
  try {
    const response = await userApi.getUserById(route.params.id)
    user.value = response.data

    const [statsResponse, postsResponse] = await Promise.all([
      followApi.getFollowCounts(route.params.id),
      postApi.getPostList({ userId: route.params.id, pageNum: 1, pageSize: 100 })
    ])
    const countsData = statsResponse.data || {}
    const postsData = postsResponse.data || {}
    const totalLikes = (postsData.records || []).reduce((sum, post) => sum + (post.likeCount || 0), 0)
    stats.value = {
      ...countsData,
      postCount: postsData.total || 0,
      likeCount: totalLikes
    }

    if (userStore.isLoggedIn) {
      const followResponse = await followApi.checkFollowStatus(route.params.id)
      isFollowing.value = followResponse.data?.following ?? false
    }
  } catch (error) {
    logger.error('Failed to fetch user', { error: error.message })
    pageError.value = '用户不存在或加载失败'
  } finally {
    loading.user = false
  }
}

async function fetchPosts() {
  loading.posts = true
  postsError.value = ''
  try {
    const response = await postApi.getPostList({ userId: route.params.id, pageNum: postsPage.value, pageSize })
    const data = response.data || {}
    posts.value = data.records || []
    postsTotal.value = data.total || 0
    postsTotalPages.value = data.pages || 1
  } catch (error) {
    logger.error('Failed to fetch posts', { error: error.message })
    postsError.value = '加载文章列表失败'
  } finally {
    loading.posts = false
  }
}

async function fetchCircles() {
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
  } catch (err) {
    logger.error('fetch circles error', { error: err.message })
    circlesError.value = err.response?.data?.message || '加载失败'
    circles.value = []
  } finally {
    loading.circles = false
  }
}

async function toggleFollow() {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  followLoading.value = true
  try {
    await followApi.toggleFollow(route.params.id)
    isFollowing.value = !isFollowing.value
    stats.value.followerCount = (stats.value.followerCount || 0) + (isFollowing.value ? 1 : -1)
    toast.success(isFollowing.value ? '关注成功' : '已取消关注')
  } catch (error) {
    logger.error('Failed to toggle follow', { error: error.message })
    toast.error('操作失败')
  } finally {
    followLoading.value = false
  }
}

function sendMessage() {
  router.push(`/messages?userId=${route.params.id}`)
}

watch(() => route.params.id, () => {
  posts.value = []
  postsError.value = ''
  postsPage.value = 1
  circles.value = []
  circlesError.value = ''
  circlesPage.value = 1
  initLoad()
  fetchPosts()
})

onMounted(() => {
  initLoad()
  fetchPosts()
})
</script>

<style scoped>
.user-profile-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.back-btn { display: flex; align-items: center; gap: 4px; padding: 8px 12px; background: transparent; border: 1px solid var(--border); border-radius: 8px; color: var(--text-secondary); cursor: pointer; font-size: 0.875rem; transition: all 0.2s; width: fit-content; }
.back-btn:hover { background: var(--border); color: var(--text-primary); }

.hero-section {
  overflow: visible;
}

.hero-cover {
  position: relative;
  height: 180px;
  overflow: hidden;
}

.hero-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hero-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 50%, rgba(0,0,0,0.5));
}

.hero-info {
  padding: 0 24px 24px;
  position: relative;
}

.hero-avatar-wrapper {
  margin-top: -40px;
  margin-bottom: 12px;
}

.hero-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid var(--surface);
  box-shadow: var(--shadow-md);
}

.hero-name {
  font-size: 1.25rem;
  font-weight: 700;
  margin-bottom: 4px;
}

.hero-bio {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-bottom: 16px;
}

.hero-stats {
  display: flex;
  gap: 28px;
  margin-bottom: 16px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 1.125rem;
  font-weight: 700;
  line-height: 1.3;
}

.stat-label {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.hero-actions {
  display: flex;
  gap: 8px;
}

.hero-skeleton {
  padding: 12px 0;
}

.avatar-skeleton {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  margin-bottom: 16px;
}

.skeleton-title {
  width: 160px;
  height: 24px;
  margin-bottom: 8px;
}

.skeleton-bio {
  width: 240px;
  height: 16px;
  margin-bottom: 16px;
}

.skeleton-stats-row {
  width: 320px;
  height: 40px;
  margin-bottom: 16px;
}

.section-card {
  padding: 20px;
}

.section-tabs { display: flex; gap: 8px; padding-bottom: 16px; border-bottom: 1px solid var(--border); margin-bottom: 16px; }
.section-tab { padding: 8px 16px; border: none; background: transparent; color: var(--text-secondary); font-size: 0.875rem; cursor: pointer; border-radius: 8px; transition: all 0.2s; }
.section-tab:hover { background: var(--border); }
.section-tab.active { background: var(--primary); color: #fff; }
.section-header { display: none; }
.post-media-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin: 8px 0; }
.post-media-thumb { width: 100%; aspect-ratio: 1; object-fit: cover; border-radius: 4px; cursor: pointer; }
.post-action-link { color: var(--primary); text-decoration: none; font-size: 0.75rem; margin-left: auto; }
.post-action-link:hover { text-decoration: underline; }

.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 12px 0;
}

.skeleton-card-item {
  padding: 16px;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
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
  padding: 16px 0;
  border-bottom: 1px solid var(--border);
}

.post-card:last-child {
  border-bottom: none;
}

.post-card-title {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 6px;
}

.post-card-title a {
  color: var(--text-primary);
  text-decoration: none;
  transition: color 0.2s;
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
}

.post-card-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pagination-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding-top: 20px;
  border-top: 1px solid var(--border);
  margin-top: 4px;
}

.pagination-info {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.error-card {
  text-align: center;
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

.error-card p {
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.tab-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 40px 20px;
  color: var(--text-muted);
}

.tab-error p {
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
  font-size: 0.9375rem;
  color: var(--text-secondary);
  margin-bottom: 16px;
}

.loading-dots {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid transparent;
  border-top-color: currentColor;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .user-profile-page {
    padding: 16px;
    gap: 16px;
  }

  .hero-cover {
    height: 130px;
  }

  .hero-avatar {
    width: 68px;
    height: 68px;
  }

  .hero-info {
    padding: 0 16px 16px;
  }

  .hero-stats {
    gap: 20px;
  }

  .stat-value {
    font-size: 1rem;
  }

  .post-card-meta {
    flex-wrap: wrap;
    gap: 10px;
  }
}
</style>
