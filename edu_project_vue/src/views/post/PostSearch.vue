<template>
  <div class="post-search-page">
    <div class="search-container">
      <div class="page-header">
        <h1>热门文章</h1>
      </div>
      
      <div v-if="loading" class="loading-state">
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
                <img src="/default-avatar.png" alt="热门文章" class="author-avatar" />
                <span>热门文章</span>
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
import { trendingApi } from '../../api/trending'
import { formatRelativeTime, truncateText, getSafeImageUrl } from '../../utils'
import { useLogger } from '../../utils/logger'

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
  text-align: center;
  padding: var(--spacing-2xl);
}

.loading-state p {
  color: var(--text-muted);
}

.error-state {
  text-align: center;
  padding: var(--spacing-2xl);
}

.error-state p {
  color: #F44336;
  margin-bottom: var(--spacing-md);
}
</style>
