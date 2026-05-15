<template>
  <div class="home-page">
    <div class="home-container">
      <main class="main-content">
        <div class="section-header">
          <h2 class="section-title">文章列表</h2>
          <div class="filter-tabs">
            <button
              v-for="f in filters"
              :key="f.value"
              class="filter-tab"
              :class="{ active: currentFilter === f.value }"
              @click="setFilter(f.value)"
            >
              {{ f.label }}
            </button>
            <span class="filter-indicator" :style="indicatorStyle"></span>
          </div>
        </div>

        <Transition name="fade" mode="out-in">
          <div v-if="loading" key="loading" class="skeleton-list">
            <div v-for="i in 3" :key="i" class="skeleton-card">
              <div class="skeleton-card-header">
                <div class="skeleton skeleton-avatar"></div>
                <div class="skeleton skeleton-name"></div>
              </div>
              <div class="skeleton skeleton-title"></div>
              <div class="skeleton skeleton-text"></div>
              <div class="skeleton skeleton-text short"></div>
              <div class="skeleton-card-footer">
                <div class="skeleton skeleton-stat"></div>
                <div class="skeleton skeleton-stat"></div>
                <div class="skeleton skeleton-stat"></div>
              </div>
            </div>
          </div>

          <div v-else-if="error" key="error" class="error-state">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="8" x2="12" y2="12"/>
              <line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
            <h3>加载失败</h3>
            <p>{{ errorMessage }}</p>
            <button class="btn btn-primary" @click="retryFetch">重新加载</button>
          </div>

          <div v-else-if="posts.length === 0" key="empty" class="empty-state">
            <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
              <line x1="12" y1="18" x2="12" y2="12"/>
              <line x1="9" y1="15" x2="15" y2="15"/>
            </svg>
            <h3 class="empty-title">还没有文章</h3>
            <p class="empty-desc">成为第一个分享知识的人吧</p>
            <router-link to="/post-edit" class="btn btn-primary">写第一篇</router-link>
          </div>

          <div v-else key="list" class="post-list">
            <TransitionGroup name="stagger">
              <article
                v-for="(post, index) in posts"
                :key="post.id"
                class="post-card card"
                :style="{ '--i': index }"
              >
                <div class="post-card-inner">
                  <div class="post-author-row">
                    <router-link :to="`/user/${post.userId}`" class="post-author">
                      <img :src="post.avatar || '/default-avatar.png'" alt="" class="author-avatar" loading="lazy" />
                      <span class="author-name">{{ post.nickname || post.username }}</span>
                    </router-link>
                    <span class="post-time">{{ formatRelativeTime(post.createTime) }}</span>
                  </div>
                  <img v-if="post.coverImage" :src="getSafeImageUrl(post.coverImage)" alt="" class="post-cover" @click="openPreview([getSafeImageUrl(post.coverImage)], 0)" style="cursor: pointer" />
                  <h3 class="post-title">
                    <router-link :to="`/post/${post.id}`">{{ post.title }}</router-link>
                  </h3>
                  <p class="post-excerpt" v-if="post.summary">{{ post.summary }}</p>
                  <div class="post-tags" v-if="post.tags?.length">
                    <span
                      v-for="tag in post.tags.slice(0, 3)"
                      :key="tag.id"
                      class="tag"
                    >
                      {{ tag.name }}
                    </span>
                  </div>
                  <div class="post-actions">
                    <button
                      class="action-btn like-btn"
                      :class="{ active: likedPosts.has(post.id) }"
                      @click="toggleLike(post)"
                      :disabled="!userStore.isLoggedIn"
                    >
                      <svg width="18" height="18" viewBox="0 0 24 24" :fill="likedPosts.has(post.id) ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
                        <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                      </svg>
                      <span>{{ post.likeCount || 0 }}</span>
                    </button>
                    <button class="action-btn comment-btn" @click="goToPost(post)">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                      </svg>
                      <span>{{ post.commentCount || 0 }}</span>
                    </button>
                    <button
                      class="action-btn collect-btn"
                      :class="{ active: collectedPosts.has(post.id) }"
                      @click="toggleCollect(post)"
                      :disabled="!userStore.isLoggedIn"
                    >
                      <svg width="18" height="18" viewBox="0 0 24 24" :fill="collectedPosts.has(post.id) ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
                        <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/>
                      </svg>
                      <span>{{ post.collectCount || 0 }}</span>
                    </button>
                    <button class="action-btn share-btn" @click="sharePost(post)">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <circle cx="18" cy="5" r="3"/>
                        <circle cx="6" cy="12" r="3"/>
                        <circle cx="18" cy="19" r="3"/>
                        <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/>
                        <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/>
                      </svg>
                      <span>{{ post.shareCount || 0 }}</span>
                    </button>
                  </div>
                </div>
              </article>
            </TransitionGroup>
          </div>
        </Transition>

        <div v-if="totalPages > 1" class="pagination">
          <button class="page-btn" :disabled="currentPage <= 1" @click="changePage(currentPage - 1)">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="15 18 9 12 15 6"/>
            </svg>
            上一页
          </button>
          <button
            v-for="page in displayedPages"
            :key="page"
            class="page-btn"
            :class="{ active: page === currentPage }"
            @click="changePage(page)"
          >
            {{ page }}
          </button>
          <button class="page-btn" :disabled="currentPage >= totalPages" @click="changePage(currentPage + 1)">
            下一页
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="9 18 15 12 9 6"/>
            </svg>
          </button>
        </div>
      </main>

      <aside class="sidebar">
        <div class="sidebar-card card">
          <h3 class="sidebar-title">热门文章</h3>
          <div v-if="sidebarLoading" class="sidebar-skeleton">
            <div v-for="i in 5" :key="i" class="sidebar-skeleton-item">
              <div class="skeleton skeleton-rank"></div>
              <div class="skeleton skeleton-line"></div>
            </div>
          </div>
          <div v-else-if="hotPosts.length > 0" class="hot-posts">
            <router-link
              v-for="(item, index) in hotPosts"
              :key="item.id"
              :to="`/post/${item.id}`"
              class="hot-post-item"
            >
              <span class="hot-rank" :class="{ top: index < 3 }">{{ index + 1 }}</span>
              <span class="hot-post-title">{{ item.title }}</span>
              <span class="hot-views">{{ formatNumber(item.viewCount || 0) }}</span>
            </router-link>
          </div>
          <div v-else class="sidebar-empty">暂无热门文章</div>
        </div>

        <div class="sidebar-card card">
          <h3 class="sidebar-title">热门标签</h3>
          <div v-if="sidebarLoading" class="sidebar-skeleton">
            <div class="skeleton skeleton-tag-line"></div>
          </div>
          <div v-else-if="hotTags.length > 0" class="tag-cloud">
            <router-link
              v-for="tag in hotTags"
              :key="tag.id"
              :to="`/search?keyword=${encodeURIComponent(tag.name)}`"
              class="tag-item"
              :style="{ fontSize: getTagSize(tag.postCount || 0) }"
            >
              {{ tag.name }}
              <sup class="tag-count">{{ tag.postCount }}</sup>
            </router-link>
          </div>
          <div v-else class="sidebar-empty">暂无热门标签</div>
        </div>

        <div class="sidebar-card card">
          <h3 class="sidebar-title">社区统计</h3>
          <div v-if="sidebarLoading" class="sidebar-skeleton">
            <div class="stats-grid-skeleton">
              <div v-for="i in 4" :key="i" class="skeleton skeleton-stat-item"></div>
            </div>
          </div>
          <div v-else class="stats-grid">
            <div class="stat-item">
              <div class="stat-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <line x1="16" y1="13" x2="8" y2="13"/>
                  <line x1="16" y1="17" x2="8" y2="17"/>
                </svg>
              </div>
              <div class="stat-value">{{ formatNumber(stats.postCount || 0) }}</div>
              <div class="stat-label">文章</div>
            </div>
            <div class="stat-item">
              <div class="stat-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                  <circle cx="9" cy="7" r="4"/>
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                  <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                </svg>
              </div>
              <div class="stat-value">{{ formatNumber(stats.userCount || 0) }}</div>
              <div class="stat-label">用户</div>
            </div>
            <div class="stat-item">
              <div class="stat-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                </svg>
              </div>
              <div class="stat-value">{{ formatNumber(stats.commentCount || 0) }}</div>
              <div class="stat-label">评论</div>
            </div>
            <div class="stat-item">
              <div class="stat-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
                  <line x1="7" y1="7" x2="7.01" y2="7"/>
                </svg>
              </div>
              <div class="stat-value">{{ formatNumber(stats.tagCount || 0) }}</div>
              <div class="stat-label">标签</div>
            </div>
          </div>
        </div>
      </aside>
    </div>
    <ImagePreview :images="previewImages" :initial-index="previewIndex" :show="previewShow" @close="previewShow = false" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { postApi } from '@/api/post'
import { trendingApi } from '@/api/trending'
import { statsApi } from '@/api/admin'
import { likeApi } from '@/api/like'
import { collectApi } from '@/api/collect'
import { shareApi } from '@/api/share'
import { useUserStore } from '@/stores/user'
import ImagePreview from '../components/common/ImagePreview.vue'
import { formatRelativeTime, formatNumber, getSafeImageUrl } from '@/utils'
import { useLogger } from '@/utils/logger'
import { toast } from '@/utils/toast'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const logger = useLogger('Home')

const posts = ref([])
const hotPosts = ref([])
const hotTags = ref([])
const stats = ref({})
const loading = ref(true)
const error = ref(false)
const errorMessage = ref('')
const currentPage = ref(1)
const totalPages = ref(1)
const pageSize = 10
const currentFilter = ref('latest')
const likedPosts = ref(new Set())
const collectedPosts = ref(new Set())
const sidebarLoaded = ref(false)
const sidebarLoading = ref(true)
const previewShow = ref(false)
const previewImages = ref([])
const previewIndex = ref(0)

const openPreview = (images, index) => {
  previewImages.value = images
  previewIndex.value = index
  previewShow.value = true
}

const filters = [
  { label: '最新', value: 'latest' },
  { label: '最热', value: 'hot' },
  { label: '精华', value: 'essence' }
]

const displayedPages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, start + 4)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

const indicatorStyle = computed(() => {
  const idx = filters.findIndex(f => f.value === currentFilter.value)
  return { transform: `translateX(${idx * 100}%)` }
})

const fetchPosts = async () => {
  loading.value = true
  error.value = false
  errorMessage.value = ''
  try {
    const response = await postApi.getPostList({
      pageNum: currentPage.value,
      pageSize,
      sort: currentFilter.value
    })
    const pageData = response.data || {}
    posts.value = pageData.records || []
    totalPages.value = pageData.pages || 1
    if (userStore.isLoggedIn) {
      checkUserInteractionStatus()
    }
  } catch (err) {
    logger.error('Failed to fetch posts', { error: err.message })
    error.value = true
    errorMessage.value = err.response?.data?.message || err.message || '加载文章失败'
    toast.error(err.response?.data?.message || '加载文章列表失败')
  } finally {
    loading.value = false
  }
}

const fetchHotPosts = async () => {
  try {
    const response = await trendingApi.getHotPosts({ pageNum: 1, pageSize: 5 })
    hotPosts.value = response?.records || []
  } catch (err) {
    logger.error('Failed to fetch hot posts', { error: err.message })
    hotPosts.value = []
  }
}

const fetchHotTags = async () => {
  try {
    const response = await trendingApi.getHotTags()
    hotTags.value = response?.records || []
  } catch (err) {
    logger.error('Failed to fetch hot tags', { error: err.message })
    hotTags.value = []
  }
}

const fetchStats = async () => {
  try {
    const response = await statsApi.getCommunityStats()
    stats.value = response || {}
  } catch (err) {
    logger.error('Failed to fetch stats', { error: err.message })
    stats.value = {}
  }
}

const checkUserInteractionStatus = async () => {
  if (!userStore.isLoggedIn) return
  const postIds = posts.value.map(p => p.id)
  if (postIds.length === 0) return
  try {
    const [likedResult, collectedResult] = await Promise.all([
      likeApi.checkLikeStatusBatch(postIds),
      collectApi.checkCollectStatusBatch(postIds)
    ])
    const likedList = likedResult?.data || []
    const collectedList = collectedResult?.data || []
    likedPosts.value = new Set(postIds.filter((_, i) => likedList[i]))
    collectedPosts.value = new Set(postIds.filter((_, i) => collectedList[i]))
  } catch (err) {
    logger.error('Failed to check interaction status', { error: err.message })
  }
}

const toggleLike = async (post) => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  try {
    await likeApi.toggleLike(post.id)
    if (likedPosts.value.has(post.id)) {
      likedPosts.value.delete(post.id)
      post.likeCount = Math.max(0, (post.likeCount || 0) - 1)
    } else {
      likedPosts.value.add(post.id)
      post.likeCount = (post.likeCount || 0) + 1
    }
  } catch (err) {
    logger.error('Failed to toggle like', { error: err.message })
    toast.error(err.response?.data?.message || '操作失败')
  }
}

const toggleCollect = async (post) => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  try {
    await collectApi.toggleCollect(post.id)
    if (collectedPosts.value.has(post.id)) {
      collectedPosts.value.delete(post.id)
      post.collectCount = Math.max(0, (post.collectCount || 0) - 1)
    } else {
      collectedPosts.value.add(post.id)
      post.collectCount = (post.collectCount || 0) + 1
    }
  } catch (err) {
    logger.error('Failed to toggle collect', { error: err.message })
    toast.error(err.response?.data?.message || '操作失败')
  }
}

const sharePost = async (post) => {
  const url = `${window.location.origin}/post/${post.id}`
  try {
    await navigator.clipboard.writeText(url)
    toast.success('链接已复制')
    if (userStore.isLoggedIn) {
      shareApi.recordShare(post.id).then(() => {
        post.shareCount = (post.shareCount || 0) + 1
      }).catch(() => {})
    }
  } catch {
    toast.error('复制失败，请手动复制链接')
  }
}

const goToPost = (post) => {
  router.push(`/post/${post.id}`)
}

const setFilter = (value) => {
  if (currentFilter.value === value) return
  currentFilter.value = value
  currentPage.value = 1
  fetchPosts()
}

const retryFetch = () => {
  fetchPosts()
}

const changePage = (page) => {
  currentPage.value = page
  fetchPosts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const getTagSize = (count) => {
  if (count > 20) return '0.95rem'
  if (count > 10) return '0.875rem'
  if (count > 5) return '0.8125rem'
  return '0.75rem'
}

const fetchSidebarData = async () => {
  if (sidebarLoaded.value) return
  sidebarLoaded.value = true
  sidebarLoading.value = true
  await Promise.allSettled([
    fetchHotPosts(),
    fetchHotTags(),
    fetchStats()
  ])
  sidebarLoading.value = false
}

watch(() => route.query.page, (newPage) => {
  if (newPage) {
    currentPage.value = parseInt(newPage) || 1
    fetchPosts()
  }
})

onMounted(() => {
  if (route.query.page) {
    currentPage.value = parseInt(route.query.page) || 1
  }
  fetchPosts()
  fetchSidebarData()
})
</script>

<style scoped>
.home-page {
  padding: 24px 0;
  min-height: calc(100vh - 64px);
}

.home-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
  align-items: start;
}

.main-content {
  min-width: 0;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.section-title {
  font-size: 1.375rem;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.01em;
}

.filter-tabs {
  position: relative;
  display: flex;
  gap: 0;
  background: var(--surface);
  border-radius: var(--radius);
  padding: 3px;
  box-shadow: var(--shadow-sm);
}

.filter-tab {
  position: relative;
  z-index: 1;
  padding: 6px 18px;
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--text-secondary);
  background: transparent;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: color var(--transition-fast);
}

.filter-tab:hover {
  color: var(--text-primary);
}

.filter-tab.active {
  color: white;
}

.filter-indicator {
  position: absolute;
  top: 3px;
  left: 3px;
  width: calc((100% - 6px) / 3);
  height: calc(100% - 6px);
  background: var(--primary);
  border-radius: 6px;
  transition: transform var(--transition-slow) cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 0;
}

.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.skeleton-card {
  background: var(--surface);
  border-radius: var(--radius-md);
  padding: 20px;
  box-shadow: var(--shadow);
}

.skeleton-card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.skeleton-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
}

.skeleton-name {
  width: 100px;
  height: 14px;
  border-radius: var(--radius);
}

.skeleton-title {
  width: 65%;
  height: 20px;
  margin-bottom: 12px;
  border-radius: var(--radius);
}

.skeleton-text {
  width: 100%;
  height: 14px;
  margin-bottom: 8px;
  border-radius: var(--radius);
}

.skeleton-text.short {
  width: 45%;
}

.skeleton-card-footer {
  display: flex;
  gap: 24px;
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid var(--border);
}

.skeleton-stat {
  width: 60px;
  height: 14px;
  border-radius: var(--radius);
}

.skeleton {
  background: linear-gradient(90deg, var(--border) 25%, var(--background) 50%, var(--border) 75%);
  background-size: 200% 100%;
  animation: skeleton-shimmer 1.5s ease-in-out infinite;
}

@keyframes skeleton-shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
  color: var(--text-muted);
}

.error-state svg {
  margin-bottom: 16px;
  color: var(--error);
}

.error-state h3 {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.error-state p {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-bottom: 20px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
  color: var(--text-muted);
}

.empty-state svg {
  margin-bottom: 16px;
  opacity: 0.4;
}

.empty-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-bottom: 20px;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  border-radius: 12px;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  animation: card-enter 0.4s ease both;
  animation-delay: calc(var(--i, 0) * 0.08s);
  border: 1px solid transparent;
}

.post-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.1), 0 4px 12px rgba(0, 0, 0, 0.06);
  border-color: var(--primary-light);
}

.post-card-inner {
  padding: 20px;
}

.post-cover {
  width: 100%;
  height: 160px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 12px;
}

@keyframes card-enter {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.post-author-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.post-author {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: var(--text-primary);
  transition: color var(--transition-fast);
}

.post-author:hover {
  color: var(--primary);
}

.author-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  background: var(--primary-light);
  flex-shrink: 0;
}

.author-name {
  font-size: 0.875rem;
  font-weight: 500;
}

.post-time {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.post-title {
  font-size: 1.25rem;
  font-weight: 700;
  margin-bottom: 10px;
  line-height: 1.4;
}

.post-title a {
  color: var(--text-primary);
  text-decoration: none;
  transition: color var(--transition-fast);
}

.post-title a:hover {
  color: var(--primary);
}

.post-excerpt {
  font-size: 0.875rem;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 14px;
}

.tag {
  padding: 3px 10px;
  font-size: 0.75rem;
  font-weight: 500;
  background: var(--primary-light);
  color: var(--primary);
  border-radius: 999px;
  text-decoration: none;
  transition: all var(--transition-fast);
}

.tag:hover {
  background: var(--primary);
  color: white;
}

.post-actions {
  display: flex;
  gap: 4px;
  padding-top: 14px;
  border-top: 1px solid var(--border);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 6px 12px;
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--text-muted);
  background: transparent;
  border: none;
  border-radius: var(--radius);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.action-btn:hover:not(:disabled) {
  background: var(--primary-light);
  color: var(--primary);
}

.action-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.action-btn.active {
  color: var(--primary);
}

.like-btn.active svg {
  animation: heart-pop 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.collect-btn.active svg {
  animation: collect-pop 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes heart-pop {
  0% { transform: scale(1); }
  30% { transform: scale(1.35); }
  60% { transform: scale(0.9); }
  100% { transform: scale(1); }
}

@keyframes collect-pop {
  0% { transform: scale(1) rotate(0deg); }
  30% { transform: scale(1.3) rotate(-5deg); }
  60% { transform: scale(0.9) rotate(3deg); }
  100% { transform: scale(1) rotate(0deg); }
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 32px;
  padding: 16px 0;
}

.page-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 36px;
  height: 36px;
  padding: 0 10px;
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--text-secondary);
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.page-btn:hover:not(:disabled) {
  background: var(--primary-light);
  border-color: var(--primary);
  color: var(--primary);
}

.page-btn.active {
  background: var(--primary);
  border-color: var(--primary);
  color: white;
  box-shadow: 0 2px 8px rgba(74, 144, 217, 0.35);
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.sidebar {
  position: sticky;
  top: 88px;
}

.sidebar-card {
  padding: 20px;
  margin-bottom: 16px;
  border-radius: 12px;
}

.sidebar-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--primary-light);
  position: relative;
}

.sidebar-title::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 36px;
  height: 2px;
  background: var(--primary);
  border-radius: 1px;
}

.hot-posts {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.hot-post-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: var(--radius);
  text-decoration: none;
  transition: all var(--transition-fast);
}

.hot-post-item:hover {
  background: var(--primary-light);
}

.hot-post-item:hover .hot-post-title {
  color: var(--primary);
}

.hot-rank {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--text-muted);
  background: var(--background);
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}

.hot-rank.top {
  background: linear-gradient(135deg, var(--primary), var(--accent));
  color: white;
}

.hot-post-title {
  flex: 1;
  font-size: 0.8125rem;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: color var(--transition-fast);
}

.hot-views {
  font-size: 0.6875rem;
  color: var(--text-muted);
  flex-shrink: 0;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 4px 12px;
  font-weight: 500;
  color: var(--text-secondary);
  background: var(--background);
  border-radius: 999px;
  text-decoration: none;
  transition: all var(--transition-fast);
  line-height: 1.4;
}

.tag-item:hover {
  background: var(--primary-light);
  color: var(--primary);
  transform: translateY(-1px);
}

.tag-count {
  font-size: 0.625rem;
  color: var(--text-muted);
  font-weight: 400;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.stat-item {
  text-align: center;
  padding: 14px 8px;
  background: var(--background);
  border-radius: var(--radius);
  transition: all var(--transition-fast);
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

.stat-icon {
  color: var(--primary);
  margin-bottom: 6px;
  opacity: 0.7;
}

.stat-value {
  font-size: 1.375rem;
  font-weight: 700;
  color: var(--primary);
  line-height: 1.2;
  margin-bottom: 2px;
}

.stat-label {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.sidebar-empty {
  text-align: center;
  padding: 16px;
  color: var(--text-muted);
  font-size: 0.8125rem;
}

.sidebar-skeleton {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sidebar-skeleton-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 0;
}

.skeleton-rank {
  width: 24px;
  height: 24px;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}

.skeleton-line {
  flex: 1;
  height: 14px;
  border-radius: var(--radius);
}

.skeleton-tag-line {
  width: 100%;
  height: 32px;
  border-radius: var(--radius);
}

.stats-grid-skeleton {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.skeleton-stat-item {
  height: 72px;
  border-radius: var(--radius);
}

.stagger-enter-active {
  transition: all 0.4s ease;
  transition-delay: calc(var(--i, 0) * 0.08s);
}

.stagger-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 992px) {
  .home-container {
    grid-template-columns: 1fr;
  }
  .sidebar {
    position: static;
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
  .sidebar-card {
    margin-bottom: 0;
  }
}

@media (max-width: 768px) {
  .home-page {
    padding: 16px 0;
  }
  .home-container {
    padding: 0 16px;
    gap: 16px;
  }
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .sidebar {
    grid-template-columns: 1fr;
  }
  .post-card-inner {
    padding: 16px;
  }
  .post-actions {
    flex-wrap: wrap;
  }
}
</style>
