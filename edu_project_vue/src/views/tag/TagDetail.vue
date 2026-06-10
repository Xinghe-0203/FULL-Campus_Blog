<template>
  <div class="tag-detail-page">
    <button class="back-link" @click="router.back()">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"/></svg>
      <span>返回</span>
    </button>

    <!-- Loading -->
    <div v-if="loading" class="state-block">
      <div class="loading-spinner"></div>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="state-block">
      <p class="state-error-text">{{ error }}</p>
      <button class="btn btn-primary btn-sm" @click="fetchTagDetail">重试</button>
    </div>

    <template v-else-if="tag">
      <!-- Tag header -->
      <header class="tag-head">
        <h1 class="tag-name">{{ tag.name }}</h1>
        <p class="tag-count">{{ totalPosts }} 篇文章</p>
      </header>

      <!-- Posts list -->
      <div class="posts-area">
        <div v-if="postsLoading && posts.length === 0" class="state-block">
          <div class="loading-spinner"></div>
        </div>

        <div v-else-if="posts.length === 0" class="state-block state-empty">
          <p class="state-empty-text">暂无相关文章</p>
        </div>

        <div v-else class="post-stack">
          <router-link
            v-for="item in posts"
            :key="item.id"
            :to="`/post/${item.id}`"
            class="post-row"
          >
            <div v-if="item.coverImage" class="post-row-cover">
              <img :src="getSafeImageUrl(item.coverImage)" alt="" />
            </div>
            <div class="post-row-body">
              <h2 class="post-row-title">{{ item.title }}</h2>
              <p class="post-row-excerpt">{{ truncateText(item.summary || item.content, 120) }}</p>
              <div class="post-row-meta">
                <img :src="item.avatar || defaultAvatar" :alt="item.nickname || item.username" class="post-row-avatar" />
                <span class="post-row-author">{{ item.nickname || item.username || '匿名' }}</span>
                <span class="meta-dot">&middot;</span>
                <span class="post-row-time">{{ formatRelativeTime(item.createTime) }}</span>
                <span class="meta-dot">&middot;</span>
                <span class="post-row-stat">{{ item.viewCount || 0 }} 阅读</span>
              </div>
              <div v-if="item.tags && item.tags.length" class="post-row-tags">
                <span v-for="t in item.tags" :key="t.id" class="mini-tag">{{ t.name }}</span>
              </div>
            </div>
          </router-link>
        </div>

        <!-- Load more -->
        <div v-if="hasMore" class="load-more-row">
          <button class="btn btn-secondary" @click="loadMore" :disabled="postsLoading">
            {{ postsLoading ? '加载中...' : '加载更多' }}
          </button>
        </div>

        <p v-else-if="posts.length > 0" class="state-end">已显示全部文章</p>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { postApi } from '../../api/post'
import { tagApi } from '../../api/tag'
import { formatRelativeTime, truncateText, getSafeImageUrl } from '../../utils'
import { useLogger } from '../../utils/logger'

const route = useRoute()
const router = useRouter()
const logger = useLogger('TagDetail')

const tag = ref<any>(null)
const loading = ref(false)
const error = ref('')
const posts = ref<any[]>([])
const postsLoading = ref(false)
const currentPage = ref(1)
const totalPages = ref(1)
const hasMore = ref(false)
const totalPosts = ref(0)
const pageSize = 10

const defaultAvatar = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 48 48"><rect width="48" height="48" rx="24" fill="#e0e0e0"/><text x="24" y="30" text-anchor="middle" fill="#999" font-size="20" font-family="sans-serif">?</text></svg>')

const fetchTagDetail = async () => {
  loading.value = true
  error.value = ''
  try {
    const tagId = String(route.params.id)
    const res = await tagApi.getTagById(tagId)
    tag.value = res.data
  } catch (err: any) {
    logger.error('Failed to fetch tag', { error: err.message })
    error.value = '标签不存在或已被删除'
  } finally {
    loading.value = false
  }
}

const fetchTagPosts = async () => {
  postsLoading.value = true
  try {
    const tagId = String(route.params.id)
    const res = await postApi.advancedSearch({
      tagId: tagId as any,
      pageNum: currentPage.value,
      pageSize: pageSize
    })
    const pageData = res.data || {}
    const records = pageData.records || []
    if (currentPage.value === 1) {
      posts.value = records
    } else {
      posts.value.push(...records)
    }
    totalPages.value = pageData.pages || 1
    totalPosts.value = pageData.total || 0
    hasMore.value = currentPage.value < totalPages.value
  } catch (err: any) {
    logger.error('Failed to fetch posts', { error: err.message })
  } finally {
    postsLoading.value = false
  }
}

const loadMore = () => {
  currentPage.value++
  fetchTagPosts()
}

onMounted(() => {
  fetchTagDetail()
  fetchTagPosts()
})
</script>

<style scoped>
/* =====================================================
   Tag Detail — Typography-Forward Minimalist
   ===================================================== */

.tag-detail-page {
  max-width: 720px;
  margin: 0 auto;
  padding: var(--spacing-xl) var(--spacing-lg);
}

/* ---------- Back link ---------- */

.back-link {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-1_5);
  padding: var(--spacing-2) 0;
  font-size: var(--text-sm);
  font-family: var(--font-sans);
  font-weight: var(--font-medium);
  color: var(--text-muted);
  background: none;
  border: none;
  cursor: pointer;
  transition: color var(--transition-fast);
  margin-bottom: var(--spacing-lg);
}

.back-link:hover {
  color: var(--primary);
}

/* ---------- Tag header ---------- */

.tag-head {
  padding-bottom: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
  border-bottom: 1px solid var(--border-solid);
}

.tag-name {
  font-family: var(--font-display);
  font-size: var(--text-5xl);
  font-weight: var(--font-extrabold);
  color: var(--text-primary);
  letter-spacing: -0.035em;
  line-height: var(--leading-tight);
  margin: 0 0 var(--spacing-2);
}

.tag-count {
  font-size: var(--text-base);
  color: var(--text-muted);
  margin: 0;
}

/* ---------- Posts ---------- */

.post-stack {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-1);
}

.post-row {
  display: flex;
  gap: var(--spacing-5);
  padding: var(--spacing-5) 0;
  text-decoration: none;
  border-bottom: 1px solid var(--border);
  transition: background var(--transition-fast);
}

.post-row:last-child {
  border-bottom: none;
}

.post-row:hover {
  background: var(--primary-subtle);
}

.post-row-cover {
  width: 140px;
  height: 96px;
  flex-shrink: 0;
  border-radius: var(--radius);
  overflow: hidden;
}

.post-row-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.post-row-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-2);
}

.post-row-title {
  font-family: var(--font-display);
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin: 0;
  line-height: var(--leading-snug);
  letter-spacing: var(--tracking-tight);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-row:hover .post-row-title {
  color: var(--primary);
}

.post-row-excerpt {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: var(--leading-normal);
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-row-meta {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.post-row-avatar {
  width: 18px;
  height: 18px;
  border-radius: var(--radius-full);
  object-fit: cover;
  flex-shrink: 0;
}

.post-row-author {
  font-weight: var(--font-medium);
  color: var(--text-secondary);
}

.meta-dot {
  opacity: 0.35;
}

.post-row-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-1_5);
  margin-top: var(--spacing-1);
}

.mini-tag {
  font-size: 0.6875rem;
  font-weight: var(--font-medium);
  color: var(--primary);
  background: var(--primary-light);
  padding: 1px var(--spacing-2);
  border-radius: var(--radius-sm);
  line-height: 1.5;
}

/* ---------- States ---------- */

.state-block {
  text-align: center;
  padding: var(--spacing-3xl) var(--spacing-lg);
}

.state-error-text {
  font-size: var(--text-sm);
  color: var(--error);
  margin-bottom: var(--spacing-md);
}

.state-empty-text {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  color: var(--text-muted);
}

.state-end {
  text-align: center;
  font-size: var(--text-xs);
  color: var(--text-muted);
  padding: var(--spacing-lg) 0;
}

.load-more-row {
  text-align: center;
  padding: var(--spacing-lg) 0;
}

/* ---------- Responsive ---------- */

@media (max-width: 768px) {
  .tag-detail-page {
    padding: var(--spacing-lg) var(--spacing-md);
  }

  .tag-name {
    font-size: var(--text-4xl);
  }

  .post-row {
    flex-direction: column;
    gap: var(--spacing-3);
    padding: var(--spacing-4) 0;
  }

  .post-row-cover {
    width: 100%;
    height: 160px;
    order: -1;
  }

  .post-row-title {
    font-size: var(--text-lg);
  }
}

@media (max-width: 480px) {
  .tag-name {
    font-size: var(--text-3xl);
  }
}
</style>
