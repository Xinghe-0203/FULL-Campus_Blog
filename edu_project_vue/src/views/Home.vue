<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { postApi } from '@/api/post'
import { trendingApi } from '@/api/trending'
import { statsApi } from '@/api/admin'
import { likeApi } from '@/api/like'
import { collectApi } from '@/api/collect'
import { useUserStore } from '@/stores/user'
import ImagePreview from '../components/common/ImagePreview.vue'
import { formatNumber } from '@/utils'
import { useLogger } from '@/utils/logger'
import { toast } from '@/utils/toast'

import HomeFilters from '@/components/home/HomeFilters.vue'
import PostCardList from '@/components/home/PostCardList.vue'
import HomeSidebar from '@/components/home/HomeSidebar.vue'

const route = useRoute()
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

const checkUserInteractionStatus = async () => {
  if (!userStore.isLoggedIn) return
  const postIds = posts.value.map(p => p.id)
  if (postIds.length === 0) return
  try {
    const results = await Promise.allSettled([
      likeApi.checkLikeStatusBatch(postIds),
      collectApi.checkCollectStatusBatch(postIds)
    ])
    if (results[0].status === 'fulfilled') {
      const likedList = results[0].value?.data || []
      likedPosts.value = new Set(postIds.filter((_, i) => likedList[i] === true))
    } else {
      logger.error('Failed to check like status batch', { error: results[0].reason?.message })
    }
    if (results[1].status === 'fulfilled') {
      const collectedList = results[1].value?.data || []
      collectedPosts.value = new Set(postIds.filter((_, i) => collectedList[i] === true))
    } else {
      logger.error('Failed to check collect status batch', { error: results[1].reason?.message })
    }
  } catch (err) {
    logger.error('Failed to check interaction status', { error: err.message })
  }
}

const handlePreview = (images, index) => {
  previewImages.value = images
  previewIndex.value = index
  previewShow.value = true
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

const fetchHotPosts = async () => {
  try {
    const response = await trendingApi.getHotPosts({ pageNum: 1, pageSize: 5 })
    hotPosts.value = response?.data?.records || []
  } catch (err) {
    logger.error('Failed to fetch hot posts', { error: err.message })
    hotPosts.value = []
  }
}

const fetchHotTags = async () => {
  try {
    const response = await trendingApi.getHotTags()
    hotTags.value = response?.data?.records || []
  } catch (err) {
    logger.error('Failed to fetch hot tags', { error: err.message })
    hotTags.value = []
  }
}

const fetchStats = async () => {
  try {
    const response = await statsApi.getCommunityStats()
    stats.value = response?.data || {}
  } catch (err) {
    logger.error('Failed to fetch stats', { error: err.message })
    stats.value = {}
  }
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

<template>
  <div class="home-page">
    <div class="home-container">
      <main class="main-content">
        <div class="section-header">
          <h2 class="section-title">文章列表</h2>
          <HomeFilters
            :filters="filters"
            :current-filter="currentFilter"
            @update="setFilter"
          />
        </div>

        <PostCardList
          :posts="posts"
          :liked-posts="likedPosts"
          :collected-posts="collectedPosts"
          :loading="loading"
          :error="error"
          :error-message="errorMessage"
          @retry="retryFetch"
          @update:liked-posts="(val) => likedPosts = val"
          @update:collected-posts="(val) => collectedPosts = val"
        />

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

      <HomeSidebar
        :hot-posts="hotPosts"
        :hot-tags="hotTags"
        :stats="stats"
        :loading="sidebarLoading"
      />
    </div>
    <ImagePreview
      :images="previewImages"
      :initial-index="previewIndex"
      :show="previewShow"
      @close="previewShow = false"
    />
  </div>
</template>

<style scoped>
.home-page {
  padding: 24px 0;
  min-height: calc(100vh - 64px);
}

.home-container {
  max-width: var(--container-xl);
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

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 32px;
  padding: 16px 0;
}

.page-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 40px;
  height: 40px;
  padding: 0 12px;
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-secondary);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius);
  cursor: pointer;
  transition: all var(--transition);
  box-shadow: var(--glass-shadow);
}

.page-btn:hover:not(:disabled):not(.active) {
  background: var(--glass-hover);
  border-color: var(--primary);
  color: var(--primary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.page-btn.active {
  background: linear-gradient(135deg, var(--primary-start), var(--primary-end));
  border-color: transparent;
  color: white;
  box-shadow: var(--shadow-sm), var(--shadow-glow-primary);
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

@media (max-width: 992px) {
  .home-container {
    grid-template-columns: 1fr;
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
  .pagination {
    flex-wrap: wrap;
  }
}
</style>