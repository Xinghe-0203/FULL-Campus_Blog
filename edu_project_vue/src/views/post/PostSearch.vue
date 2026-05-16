<template>
  <div class="post-search-page">
    <button class="back-btn" @click="router.back()">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
      返回
    </button>
    <div class="search-container">
      <div class="page-header">
        <h1>热门文章</h1>
      </div>
      
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
      <div v-else-if="error" class="error-state">
        <p>加载失败，请稍后重试</p>
        <button class="btn btn-primary" @click="fetchHotPosts">重新加载</button>
      </div>
      <div v-else-if="posts.length > 0" class="post-list">
        <div v-for="post in posts" :key="post.id" class="post-item card">
          <img v-if="post.coverImage" :src="getSafeImageUrl(post.coverImage)" alt="" class="post-cover" />
          <div class="post-body">
            <h2 class="post-title">
              <router-link :to="`/post/${post.id}`">{{ post.title }}</router-link>
            </h2>
            <p class="post-excerpt">{{ truncateText(post.summary || post.content, 150) }}</p>
            <div class="post-tags" v-if="post.tags && post.tags.length">
              <span
                v-for="tag in post.tags.slice(0, 3)"
                :key="tag.id"
                class="tag"
              >
                {{ tag.name }}
              </span>
            </div>
            <div class="post-meta">
              <span class="post-author">
                <img :src="post.avatar || '/default-avatar.png'" :alt="post.nickname || post.username" class="author-avatar" />
                <router-link :to="`/user/${post.userId}`" class="author-name">{{ post.nickname || post.username || '匿名' }}</router-link>
              </span>
              <span class="post-time">{{ formatRelativeTime(post.createTime) }}</span>
              <div class="post-stats">
                <span>{{ post.viewCount || 0 }} 阅读</span>
                <span>{{ post.likeCount || 0 }} 点赞</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <p>暂无文章</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { trendingApi } from '../../api/trending'
import { formatRelativeTime, truncateText, getSafeImageUrl } from '../../utils'
import { useLogger } from '../../utils/logger'

const router = useRouter()
const logger = useLogger('PostSearch')
const posts = ref([])
const loading = ref(false)
const error = ref(false)

const fetchHotPosts = async () => {
  loading.value = true
  error.value = false
  try {
    const response = await trendingApi.getHotPosts({ pageNum: 1, pageSize: 50 })
    posts.value = response.data?.records || []
  } catch (err) {
    logger.error('Failed to fetch hot posts', { error: err.message })
    error.value = true
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchHotPosts()
})
</script>

<style scoped>
.post-search-page {
  max-width: 800px;
  margin: 0 auto;
  padding: var(--spacing-lg);
}

.page-header {
  margin-bottom: var(--spacing-lg);
}

.page-header h1 {
  font-size: 1.5rem;
  font-weight: 700;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.post-item {
  padding: var(--spacing-lg);
}
.back-btn { display: flex; align-items: center; gap: 4px; padding: 8px 12px; background: transparent; border: 1px solid var(--border); border-radius: 8px; color: var(--text-secondary); cursor: pointer; font-size: 0.875rem; transition: all 0.2s; width: fit-content; margin-bottom: var(--spacing-lg); }
.back-btn:hover { background: var(--border); color: var(--text-primary); }

.post-cover {
  width: 100%;
  height: 160px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 12px;
}

.post-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: var(--spacing-sm);
}

.post-title a {
  color: var(--text-primary);
}

.post-title a:hover {
  color: var(--primary);
}

.post-excerpt {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-bottom: var(--spacing-sm);
}

.post-tags {
  display: flex;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
}

.tag {
  padding: 0.25rem 0.75rem;
  font-size: 0.75rem;
  background: var(--primary-light);
  color: var(--primary);
  border-radius: var(--radius-full);
  text-decoration: none;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  font-size: 0.75rem;
  color: var(--text-muted);
}

.post-author {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  text-decoration: none;
  color: var(--text-primary);
}

.author-avatar {
  width: 24px;
  height: 24px;
  border-radius: var(--radius-full);
  object-fit: cover;
}

.author-name {
  font-size: 0.8125rem;
  color: var(--text-primary);
  text-decoration: none;
  font-weight: 500;
}

.author-name:hover {
  color: var(--primary);
}

.post-stats {
  margin-left: auto;
  display: flex;
  gap: var(--spacing-md);
}

.empty-state {
  text-align: center;
  padding: var(--spacing-2xl);
}

.empty-state p {
  color: var(--text-muted);
  margin-bottom: var(--spacing-md);
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-2xl);
  color: var(--text-muted);
  gap: var(--spacing-md);
}

.error-state {
  text-align: center;
  padding: var(--spacing-2xl);
}

.error-state p {
  color: var(--error);
  margin-bottom: var(--spacing-md);
}
</style>
