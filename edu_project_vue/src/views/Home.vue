<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { postApi } from '@/api/post'
import { trendingApi } from '@/api/trending'
import { statsApi } from '@/api/admin'
import { likeApi } from '@/api/like'
import { collectApi } from '@/api/collect'
import { useUserStore } from '@/stores/user'
import { useLogger } from '@/utils/logger'
import { toast } from '@/utils/toast'
import { formatRelativeTime, getSafeImageUrl } from '@/utils'
import type { Post, Tag, CommunityStats } from '@/types'

import HomeFilters from '@/components/home/HomeFilters.vue'
import HomeSidebar from '@/components/home/HomeSidebar.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const logger = useLogger('Home')

const posts = ref<Post[]>([])
const hotPosts = ref<Post[]>([])
const hotTags = ref<Tag[]>([])
const stats = ref<Partial<CommunityStats>>({})
const loading = ref(true)
const error = ref(false)
const errorMessage = ref('')
const currentPage = ref(1)
const totalPages = ref(1)
const pageSize = 10
const currentFilter = ref('latest')
const likedPosts = ref(new Set<number | string>())
const collectedPosts = ref(new Set<number | string>())
const sidebarLoaded = ref(false)
const sidebarLoading = ref(true)
const pageReady = ref(false)

// Scroll-triggered visibility tracking
const cardVisibility = ref<boolean[]>([])
let observer: IntersectionObserver | null = null

const filters = [
  { label: '最新', value: 'latest' },
  { label: '最热', value: 'hot' },
  { label: '精华', value: 'essence' }
]

const displayedPages = computed(() => {
  const pages: number[] = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, start + 4)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

const fetchPosts = async (): Promise<void> => {
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
    // Reset visibility for new posts
    cardVisibility.value = new Array(posts.value.length).fill(false)
    // Trigger staggered entrance
    nextTick(() => {
      posts.value.forEach((_, i) => {
        setTimeout(() => {
          cardVisibility.value[i] = true
        }, i * 80)
      })
    })
    if (userStore.isLoggedIn) {
      checkUserInteractionStatus()
    }
  } catch (err: any) {
    logger.error('Failed to fetch posts', { error: err.message })
    error.value = true
    errorMessage.value = err.response?.data?.message || err.message || '加载文章失败'
    toast.error(err.response?.data?.message || '加载文章列表失败')
  } finally {
    loading.value = false
  }
}

const checkUserInteractionStatus = async (): Promise<void> => {
  if (!userStore.isLoggedIn) return
  const postIds = posts.value.map((p) => p.id)
  if (postIds.length === 0) return
  try {
    const results = await Promise.allSettled([
      likeApi.checkLikeStatusBatch(postIds),
      collectApi.checkCollectStatusBatch(postIds)
    ])
    if (results[0].status === 'fulfilled') {
      const likedList = (results[0].value?.data as unknown as boolean[]) || []
      likedPosts.value = new Set(postIds.filter((_, i) => likedList[i] === true))
    } else {
      logger.error('Failed to check like status batch', { error: results[0].reason?.message })
    }
    if (results[1].status === 'fulfilled') {
      const collectedList = (results[1].value?.data as unknown as boolean[]) || []
      collectedPosts.value = new Set(postIds.filter((_, i) => collectedList[i] === true))
    } else {
      logger.error('Failed to check collect status batch', { error: results[1].reason?.message })
    }
  } catch (err) {
    logger.error('Failed to check interaction status', { error: (err as Error).message })
  }
}

const setFilter = (value: string): void => {
  if (currentFilter.value === value) return
  currentFilter.value = value
  currentPage.value = 1
  fetchPosts()
}

const retryFetch = (): void => {
  fetchPosts()
}

const changePage = (page: number): void => {
  currentPage.value = page
  router.push({ query: { ...route.query, page: page > 1 ? String(page) : undefined } })
}

const fetchSidebarData = async (): Promise<void> => {
  if (sidebarLoaded.value) return
  sidebarLoaded.value = true
  sidebarLoading.value = true
  await Promise.allSettled([fetchHotPosts(), fetchHotTags(), fetchStats()])
  sidebarLoading.value = false
}

const fetchHotPosts = async (): Promise<void> => {
  try {
    const response = await trendingApi.getHotPosts({ pageNum: 1, pageSize: 5 })
    hotPosts.value = response?.data?.records || []
  } catch (err) {
    logger.error('Failed to fetch hot posts', { error: (err as Error).message })
    hotPosts.value = []
  }
}

const fetchHotTags = async (): Promise<void> => {
  try {
    const response = await trendingApi.getHotTags()
    hotTags.value = response?.data?.records || []
  } catch (err) {
    logger.error('Failed to fetch hot tags', { error: (err as Error).message })
    hotTags.value = []
  }
}

const fetchStats = async (): Promise<void> => {
  try {
    const response = await statsApi.getCommunityStats()
    stats.value = response?.data || {}
  } catch (err) {
    logger.error('Failed to fetch stats', { error: (err as Error).message })
    stats.value = {}
  }
}

// Intersection Observer for sidebar entrance
const setupSidebarObserver = (): void => {
  observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add('in-view')
          observer?.unobserve(entry.target)
        }
      })
    },
    { threshold: 0.1, rootMargin: '0px 0px -40px 0px' }
  )
}

watch(
  () => route.query.page,
  (newPage) => {
    currentPage.value = parseInt(newPage as string) || 1
    fetchPosts()
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
)

onMounted(() => {
  if (route.query.page) {
    currentPage.value = parseInt(route.query.page as string) || 1
  }
  fetchPosts()
  fetchSidebarData()
  setupSidebarObserver()
  // Trigger page entrance animation after a brief tick
  requestAnimationFrame(() => {
    pageReady.value = true
  })
})

onUnmounted(() => {
  observer?.disconnect()
})
</script>

<template>
  <div class="home-page" :class="{ 'page-ready': pageReady }">
    <!-- Hero: minimal editorial strip -->
    <header class="hero-strip">
      <div class="hero-inner">
        <div class="hero-text">
          <h1 class="hero-title">
            <span class="hero-title-word">Campus</span>
            <span class="hero-title-word hero-title-word--accent">Blog</span>
          </h1>
          <p class="hero-tagline">分享知识，探索无限可能</p>
        </div>
        <div class="hero-meta">
          <span class="hero-meta-dot"></span>
          <span class="hero-meta-text">Discover &middot; Write &middot; Share</span>
        </div>
      </div>
    </header>

    <div class="home-container">
      <main class="main-content">
        <h2 class="sr-only">文章列表</h2>

        <!-- Section header -->
        <div class="section-header">
          <div class="section-title-group">
            <h2 class="section-title">文章</h2>
            <span class="section-title-line"></span>
          </div>
          <HomeFilters
            :filters="filters"
            :current-filter="currentFilter"
            @update="setFilter"
          />
        </div>

        <!-- Asymmetric Bento Post Grid -->
        <div class="bento-grid">
          <Transition name="fade" mode="out-in">
            <!-- Loading State -->
            <div v-if="loading" key="loading" class="skeleton-grid" role="status" aria-busy="true" aria-label="加载中">
              <div v-for="i in 4" :key="i" class="skeleton-card" :class="{ 'skeleton-card--large': i === 1 }">
                <div class="skeleton-card-body">
                  <div class="skeleton-card-header">
                    <div class="skeleton skeleton-avatar"></div>
                    <div class="skeleton skeleton-name"></div>
                  </div>
                  <div class="skeleton skeleton-title"></div>
                  <div class="skeleton skeleton-text"></div>
                  <div class="skeleton skeleton-text short"></div>
                </div>
              </div>
            </div>

            <!-- Error State -->
            <div v-else-if="error" key="error" class="error-state" role="alert">
              <div class="error-icon">
                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10"/>
                  <line x1="12" y1="8" x2="12" y2="12"/>
                  <line x1="12" y1="16" x2="12.01" y2="16"/>
                </svg>
              </div>
              <h3>加载失败</h3>
              <p>{{ errorMessage }}</p>
              <button class="btn-retry" @click="retryFetch">重新加载</button>
            </div>

            <!-- Empty State -->
            <div v-else-if="posts.length === 0" key="empty" class="empty-state">
              <div class="empty-icon">
                <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <line x1="12" y1="18" x2="12" y2="12"/>
                  <line x1="9" y1="15" x2="15" y2="15"/>
                </svg>
              </div>
              <h3 class="empty-title">还没有文章</h3>
              <p class="empty-desc">成为第一个分享知识的人吧</p>
              <router-link to="/post-edit" class="btn-write">写第一篇</router-link>
            </div>

            <!-- Bento Grid of Posts -->
            <div v-else key="list" class="bento-posts">
              <article
                v-for="(post, index) in posts"
                :key="post.id"
                class="bento-card"
                :class="{
                  'bento-card--visible': cardVisibility[index]
                }"
              >
                <!-- Standard card -->
                <div class="bento-standard" @click="$router.push(`/post/${post.id}`)">
                  <div class="bento-standard-cover" :class="{ 'bento-standard-cover--empty': !post.coverImage }">
                    <img
                      v-if="post.coverImage"
                      :src="getSafeImageUrl(post.coverImage)"
                      :alt="post.title + ' 封面图'"
                      class="bento-standard-img"
                      loading="lazy"
                    />
                  </div>
                  <div class="bento-standard-body">
                    <div class="bento-author">
                      <img
                        :src="post.avatar || '/default-avatar.png'"
                        :alt="post.nickname || post.username || '用户'"
                        class="bento-avatar bento-avatar--squircle"
                        loading="lazy"
                      />
                      <span class="bento-author-name">{{ post.nickname || post.username }}</span>
                    </div>
                    <h3 class="bento-title">
                      <router-link :to="`/post/${post.id}`" @click.stop>{{ post.title }}</router-link>
                    </h3>
                    <p class="bento-excerpt" v-if="post.summary">{{ post.summary }}</p>
                    <div class="bento-tags" v-if="post.tags?.length">
                      <span
                        v-for="tag in post.tags.slice(0, 2)"
                        :key="tag.id"
                        class="bento-tag"
                      >
                        {{ tag.name }}
                      </span>
                    </div>
                    <div class="bento-footer">
                      <span class="bento-time">{{ formatRelativeTime(post.createTime) }}</span>
                      <div class="bento-stats">
                        <span class="bento-stat">
                          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
                          {{ post.likeCount || 0 }}
                        </span>
                        <span class="bento-stat">
                          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                          {{ post.commentCount || 0 }}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </article>
            </div>
          </Transition>
        </div>

        <!-- Pagination -->
        <nav v-if="totalPages > 1" class="pagination" aria-label="分页导航">
          <button
            class="page-btn page-btn--nav"
            :disabled="currentPage <= 1"
            @click="changePage(currentPage - 1)"
            aria-label="上一页"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="15 18 9 12 15 6"/>
            </svg>
          </button>

          <div class="page-numbers">
            <button
              v-for="page in displayedPages"
              :key="page"
              class="page-btn"
              :class="{ active: page === currentPage }"
              :aria-current="page === currentPage ? 'page' : undefined"
              @click="changePage(page)"
            >
              {{ page }}
            </button>
          </div>

          <button
            class="page-btn page-btn--nav"
            :disabled="currentPage >= totalPages"
            @click="changePage(currentPage + 1)"
            aria-label="下一页"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="9 18 15 12 9 6"/>
            </svg>
          </button>

          <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
        </nav>
      </main>

      <aside class="sidebar-wrapper">
        <HomeSidebar
          :hot-posts="hotPosts"
          :hot-tags="hotTags"
          :stats="stats"
          :loading="sidebarLoading"
        />
      </aside>
    </div>

  </div>
</template>

<style scoped>
/* ==================== Page Entrance ==================== */
.home-page {
  padding: 0 0 56px;
  min-height: calc(100vh - 64px);
  opacity: 0;
  transform: translateY(12px);
  transition: opacity 0.6s cubic-bezier(0.25, 0.1, 0.25, 1),
              transform 0.6s cubic-bezier(0.25, 0.1, 0.25, 1);
}

.home-page.page-ready {
  opacity: 1;
  transform: translateY(0);
}

/* ==================== Subtle Texture ==================== */
.home-page::before {
  content: '';
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: -1;
  opacity: 0.35;
  background-image:
    radial-gradient(circle at 20% 20%, rgba(13, 148, 136, 0.03) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(249, 115, 22, 0.02) 0%, transparent 50%);
}

/* ==================== Hero Strip ==================== */
.hero-strip {
  border-bottom: 1px solid var(--border);
  margin-bottom: 36px;
}

.hero-inner {
  max-width: var(--container-xl);
  margin: 0 auto;
  padding: 44px 24px 36px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
}

.hero-text {
  min-width: 0;
}

.hero-title {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 6px;
  font-family: var(--font-display);
}

.hero-title-word {
  font-size: 2rem;
  font-weight: 800;
  letter-spacing: -0.04em;
  color: var(--text-primary);
  line-height: 1.1;
}

.hero-title-word--accent {
  font-style: italic;
  color: var(--purple);
}

.hero-tagline {
  font-size: 0.875rem;
  color: var(--purple-muted);
  font-weight: 400;
  letter-spacing: 0.01em;
  margin: 0;
}

.hero-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.hero-meta-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--purple);
  opacity: 0.6;
  animation: meta-pulse 3s ease-in-out infinite;
}

@keyframes meta-pulse {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 0.2; }
}

.hero-meta-text {
  font-size: 0.75rem;
  color: var(--purple-muted);
  letter-spacing: 0.06em;
  text-transform: uppercase;
  font-weight: 500;
}

/* ==================== Main Layout ==================== */
.home-container {
  max-width: var(--container-xl);
  margin: 0 auto;
  padding: 0 24px;
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 36px;
  align-items: start;
}

.main-content {
  min-width: 0;
}

.sidebar-wrapper {
  min-width: 0;
}

/* ==================== Section Header ==================== */
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  gap: 16px;
}

.section-title-group {
  display: flex;
  align-items: center;
  gap: 14px;
}

.section-title {
  font-family: var(--font-display);
  font-size: 1.375rem;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.03em;
  line-height: 1.2;
  margin: 0;
}

.section-title-line {
  display: block;
  width: 28px;
  height: 2px;
  background: var(--primary);
  border-radius: 1px;
  opacity: 0.5;
  transition: width 0.3s ease, opacity 0.3s ease;
}

.section-header:hover .section-title-line {
  width: 44px;
  opacity: 0.8;
}

/* ==================== Bento Grid ==================== */
.bento-grid {
  min-height: 200px;
}

.bento-posts {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

/* ==================== Bento Cards ==================== */
.bento-card {
  opacity: 0;
  transform: translateY(12px);
  transition: opacity 0.45s cubic-bezier(0.25, 0.1, 0.25, 1),
              transform 0.45s cubic-bezier(0.25, 0.1, 0.25, 1);
}

.bento-card--visible {
  opacity: 1;
  transform: translateY(0);
}

/* Standard card layout */
.bento-standard {
  background: var(--surface-solid);
  border-radius: var(--radius-md);
  overflow: hidden;
  transition: box-shadow 0.3s ease;
  cursor: pointer;
}

.bento-standard:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.bento-standard-cover {
  overflow: hidden;
  min-height: 160px;
}

.bento-standard-cover--empty {
  background: linear-gradient(135deg, var(--gray-100), var(--gray-200));
}

.bento-standard-img {
  width: 100%;
  height: 160px;
  object-fit: cover;
  transition: transform 0.5s cubic-bezier(0.25, 0.1, 0.25, 1);
}

.bento-standard:hover .bento-standard-img {
  transform: scale(1.03);
}

.bento-standard-body {
  padding: 16px 18px 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* ==================== Author Row ==================== */
.bento-author {
  display: flex;
  align-items: center;
  gap: 8px;
}

.bento-avatar {
  width: 28px;
  height: 28px;
  object-fit: cover;
  flex-shrink: 0;
  background: var(--gray-100);
}

/* Squircle shape */
.bento-avatar--squircle {
  border-radius: 22%;
}

.bento-author-name {
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.bento-dot {
  color: var(--text-muted);
  font-size: 0.75rem;
}

.bento-time {
  font-size: 0.75rem;
  color: var(--text-muted);
  white-space: nowrap;
}

/* ==================== Bento Title ==================== */
.bento-title {
  font-family: var(--font-display);
  font-size: 1.0625rem;
  font-weight: 700;
  line-height: 1.35;
  letter-spacing: -0.02em;
  margin: 0;
}

.bento-title a {
  color: var(--text-primary);
  text-decoration: none;
  transition: color 0.15s ease;
}

.bento-title a:hover {
  color: var(--primary);
}

/* ==================== Bento Excerpt ==================== */
.bento-excerpt {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  max-width: 65ch;
}

.bento-featured-body .bento-excerpt {
  -webkit-line-clamp: 3;
  line-clamp: 3;
}

/* ==================== Bento Tags ==================== */
.bento-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.bento-tag {
  padding: 3px 10px;
  font-size: 0.6875rem;
  font-weight: 500;
  border-radius: var(--radius-full);
  letter-spacing: 0.01em;
  transition: background 0.15s ease;
  /* Muted pastel palette - cycling through soft colors */
  background: var(--tag-bg, rgba(13, 148, 136, 0.06));
  color: var(--tag-color, var(--primary));
}

.bento-tag:nth-child(1) { --tag-bg: rgba(13, 148, 136, 0.06); --tag-color: #0D9488; }
.bento-tag:nth-child(2) { --tag-bg: rgba(249, 115, 22, 0.06); --tag-color: #EA580C; }
.bento-tag:nth-child(3) { --tag-bg: rgba(139, 92, 246, 0.06); --tag-color: #7C3AED; }
.bento-tag:nth-child(4) { --tag-bg: rgba(236, 72, 153, 0.06); --tag-color: #DB2777; }

/* ==================== Bento Stats ==================== */
.bento-stats {
  display: flex;
  align-items: center;
  gap: 14px;
}

.bento-stat {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.75rem;
  color: var(--text-muted);
}

.bento-stat svg {
  opacity: 0.6;
}

/* ==================== Bento Footer ==================== */
.bento-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
  padding-top: 4px;
}

/* ==================== Skeleton Loading ==================== */
.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.skeleton-card {
  background: var(--surface-solid);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.skeleton-card--large {
  grid-column: 1 / -1;
}

.skeleton-card--large .skeleton-card-body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  padding: 24px;
}

.skeleton-card-body {
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skeleton-card-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.skeleton {
  background: linear-gradient(90deg,
    var(--skeleton-base) 0%,
    var(--skeleton-highlight) 50%,
    var(--skeleton-base) 100%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s ease-in-out infinite;
  border-radius: var(--radius-sm);
}

@keyframes skeleton-loading {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.skeleton-avatar {
  width: 28px;
  height: 28px;
  border-radius: 22%;
  flex-shrink: 0;
}

.skeleton-name {
  width: 80px;
  height: 13px;
}

.skeleton-title {
  width: 70%;
  height: 18px;
}

.skeleton-text {
  width: 100%;
  height: 13px;
}

.skeleton-text.short {
  width: 50%;
}

/* ==================== Error & Empty States ==================== */
.error-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 56px 24px;
  text-align: center;
  background: var(--surface-solid);
  border-radius: var(--radius-lg);
}

.error-icon,
.empty-icon {
  color: var(--text-muted);
  opacity: 0.4;
  margin-bottom: 16px;
}

.error-state h3,
.empty-title {
  font-family: var(--font-display);
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 6px;
}

.error-state p,
.empty-desc {
  font-size: 0.8125rem;
  color: var(--text-muted);
  margin: 0 0 20px;
  max-width: 320px;
}

.btn-retry,
.btn-write {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 9px 22px;
  font-size: 0.8125rem;
  font-weight: 600;
  color: white;
  background: var(--primary);
  border: none;
  border-radius: var(--radius);
  cursor: pointer;
  transition: background 0.15s ease, transform 0.1s ease;
  text-decoration: none;
}

.btn-retry:hover,
.btn-write:hover {
  background: var(--primary-hover);
}

.btn-retry:active,
.btn-write:active {
  transform: scale(0.98);
}

/* ==================== Pagination ==================== */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 36px;
  padding: 16px 0 0;
}

.page-numbers {
  display: flex;
  align-items: center;
  gap: 4px;
}

.page-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 36px;
  height: 36px;
  padding: 0 4px;
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--text-secondary);
  background: transparent;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  cursor: pointer;
  transition: all 0.15s ease;
  font-variant-numeric: tabular-nums;
}

.page-btn--nav {
  color: var(--text-muted);
  border-color: transparent;
}

.page-btn:hover:not(:disabled):not(.active) {
  background: var(--primary-light);
  border-color: rgba(13, 148, 136, 0.15);
  color: var(--primary);
}

.page-btn:active:not(:disabled) {
  transform: scale(0.96);
}

.page-btn.active {
  background: var(--primary);
  border-color: var(--primary);
  color: white;
  font-weight: 600;
}

.page-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
  pointer-events: none;
}

.page-info {
  font-size: 0.6875rem;
  color: var(--text-muted);
  margin-left: 10px;
  font-variant-numeric: tabular-nums;
  letter-spacing: 0.02em;
}

/* ==================== Responsive ==================== */
@media (max-width: 1200px) {
  .home-container {
    grid-template-columns: 1fr 280px;
    gap: 28px;
  }
}

@media (max-width: 992px) {
  .home-container {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .hero-meta {
    display: none;
  }
}

@media (max-width: 768px) {
  .home-page {
    padding-bottom: 32px;
  }

  .hero-inner {
    padding: 28px 16px 24px;
  }

  .hero-title-word {
    font-size: 1.5rem;
  }

  .hero-tagline {
    font-size: 0.8125rem;
  }

  .home-container {
    padding: 0 16px;
    gap: 20px;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    margin-bottom: 18px;
  }

  /* On mobile: single column bento */
  .bento-posts,
  .skeleton-grid {
    grid-template-columns: 1fr;
  }

  .pagination {
    gap: 4px;
    margin-top: 28px;
    flex-wrap: wrap;
  }

  .page-info {
    display: none;
  }
}

@media (max-width: 480px) {
  .hero-inner {
    padding: 20px 16px 18px;
  }

  .hero-title-word {
    font-size: 1.25rem;
  }

  .page-btn {
    min-width: 32px;
    height: 32px;
    font-size: 0.75rem;
  }
}
</style>
