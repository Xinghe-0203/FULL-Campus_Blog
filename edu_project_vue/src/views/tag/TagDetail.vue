<template>
  <div class="tag-detail-page">
    <button class="back-btn" @click="router.back()">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
      返回
    </button>

    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="error" class="error-state">
      <p>{{ error }}</p>
      <button class="btn btn-primary" @click="fetchTagDetail">重试</button>
    </div>

    <template v-else-if="tag">
      <div class="tag-header glass">
        <div class="tag-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg>
        </div>
        <div class="tag-info">
          <h1 class="tag-name">{{ tag.name }}</h1>
          <div class="tag-stats">
            <span>{{ totalPosts }} 篇文章</span>
          </div>
        </div>
      </div>

      <div class="tag-posts">
        <div v-if="postsLoading" class="loading-state">
          <div class="loading-spinner"></div>
          <p>加载文章中...</p>
        </div>
        <div v-else-if="posts.length === 0" class="empty-state">
          <p>暂无相关文章</p>
        </div>
        <div v-else class="posts-list">
          <div v-for="item in posts" :key="item.id" class="post-item card">
            <img v-if="item.coverImage" :src="getSafeImageUrl(item.coverImage)" alt="" class="post-cover" />
            <div class="post-body">
              <h2 class="post-title">
                <router-link :to="`/post/${item.id}`">{{ item.title }}</router-link>
              </h2>
              <p class="post-excerpt">{{ truncateText(item.summary || item.content, 150) }}</p>
              <div class="post-tags" v-if="item.tags && item.tags.length">
                <span v-for="t in item.tags" :key="t.id" class="tag-badge">{{ t.name }}</span>
              </div>
              <div class="post-meta">
                <span class="post-author">
                  <img :src="item.avatar || defaultAvatar" :alt="item.nickname || item.username" class="author-avatar" />
                  <router-link :to="`/user/${item.userId}`" class="author-name">{{ item.nickname || item.username || '匿名' }}</router-link>
                </span>
                <span class="post-time">{{ formatRelativeTime(item.createTime) }}</span>
                <div class="post-stats">
                  <span>{{ item.viewCount || 0 }} 阅读</span>
                  <span>{{ item.likeCount || 0 }} 点赞</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="hasMore" class="load-more">
          <button class="btn btn-secondary" @click="loadMore" :disabled="postsLoading">
            {{ postsLoading ? '加载中...' : '加载更多' }}
          </button>
        </div>

        <div v-else-if="posts.length > 0" class="no-more-state">
          <p>没有更多文章了</p>
        </div>
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
.tag-detail-page {
  max-width: 900px;
  margin: 0 auto;
  padding: var(--spacing-lg);
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius);
  color: var(--text-secondary);
  cursor: pointer;
  margin-bottom: var(--spacing-md);
  transition: all var(--transition);
}

.back-btn:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.loading-state, .error-state {
  text-align: center;
  padding: var(--spacing-3xl);
}

.error-state {
  color: var(--error);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--glass-border);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto var(--spacing-md);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.tag-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  padding: var(--spacing-xl);
  border-radius: var(--radius-lg);
  margin-bottom: var(--spacing-lg);
}

.tag-icon {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary-start), var(--primary-end));
  color: white;
  border-radius: var(--radius);
  box-shadow: var(--shadow-glow-primary);
}

.tag-info {
  flex: 1;
}

.tag-name {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.tag-stats {
  font-size: 0.85rem;
  color: var(--text-muted);
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.post-item {
  display: flex;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
}

.post-cover {
  width: 120px;
  height: 80px;
  object-fit: cover;
  border-radius: var(--radius);
  flex-shrink: 0;
}

.post-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.post-title {
  font-size: 1rem;
  font-weight: 600;
  margin: 0;
}

.post-title a {
  color: var(--text-primary);
}

.post-title a:hover {
  color: var(--primary);
}

.post-excerpt {
  font-size: 0.85rem;
  color: var(--text-secondary);
  line-height: 1.5;
  margin: 0;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-xs);
}

.tag-badge {
  font-size: 0.75rem;
  padding: 2px 8px;
  background: var(--primary-light);
  color: var(--primary);
  border-radius: var(--radius-full);
}

.post-meta {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  font-size: 0.8rem;
  color: var(--text-muted);
  margin-top: auto;
}

.post-author {
  display: flex;
  align-items: center;
  gap: 6px;
}

.author-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  object-fit: cover;
}

.author-name {
  color: var(--primary);
}

.author-name:hover {
  text-decoration: underline;
}

.post-stats {
  display: flex;
  gap: var(--spacing-sm);
}

.load-more {
  text-align: center;
  padding: var(--spacing-lg);
}

.no-more-state {
  text-align: center;
  padding: var(--spacing-lg);
  color: var(--text-muted);
  font-size: 0.875rem;
}

.empty-state {
  text-align: center;
  padding: var(--spacing-3xl);
  color: var(--text-muted);
}

@media (max-width: 768px) {
  .tag-detail-page { padding: var(--spacing-md); }
  .tag-header { flex-direction: column; text-align: center; }
  .post-item { flex-direction: column; }
  .post-cover { width: 100%; height: 150px; }
}
</style>