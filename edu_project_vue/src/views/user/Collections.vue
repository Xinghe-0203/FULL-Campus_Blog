<template>
  <div class="collections-page">
    <button class="back-btn" @click="router.back()">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
      返回
    </button>
    <div class="page-header">
      <h1>我的收藏</h1>
    </div>

    <div v-if="loading && collections.length === 0" class="skeleton-list">
      <div v-for="n in 3" :key="n" class="skeleton-card-item">
        <div class="skeleton skeleton-card-title"></div>
        <div class="skeleton skeleton-card-text"></div>
        <div class="skeleton skeleton-card-text short"></div>
        <div class="skeleton skeleton-card-meta"></div>
      </div>
    </div>

    <div v-else-if="error" class="error-card card">
      <p>{{ error }}</p>
      <button class="btn btn-sm btn-primary" @click="fetchCollections">重试</button>
    </div>

    <div v-else-if="collections.length === 0" class="empty-state card">
      <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" class="empty-icon"><path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/></svg>
      <p class="empty-title">还没有收藏文章</p>
      <p class="empty-text">去发现感兴趣的内容吧</p>
      <router-link to="/" class="btn btn-primary">去发现</router-link>
    </div>

    <div v-else class="collection-list">
      <div v-for="item in collections" :key="item.collectId" class="collection-card card">
        <div class="collection-card-body">
          <h3 class="collection-title">
            <router-link :to="`/post/${item.postId}`">{{ item.title }}</router-link>
          </h3>
          <p v-if="item.summary" class="collection-excerpt">{{ truncateText(item.summary, 120) }}</p>
          <div class="collection-meta">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/></svg>
            收藏于 {{ formatRelativeTime(item.collectTime) }}
          </div>
        </div>
        <button class="btn btn-sm btn-ghost collection-remove-btn" @click="confirmRemove(item)">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          取消收藏
        </button>
      </div>

      <div v-if="totalPages > 1" class="pagination-section">
        <div class="pagination">
          <button class="pagination-btn" :disabled="page <= 1" @click="page--; fetchCollections()">上一页</button>
          <button v-for="p in totalPages" :key="p" class="pagination-btn" :class="{ active: p === page }" @click="page = p; fetchCollections()">{{ p }}</button>
          <button class="pagination-btn" :disabled="page >= totalPages" @click="page++; fetchCollections()">下一页</button>
        </div>
        <span class="pagination-info">共 {{ total }} 篇</span>
      </div>
    </div>

    <ConfirmDialog />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { collectApi } from '../../api/collect'
import { useConfirm } from '../../composables/useConfirm'
import { formatRelativeTime, truncateText } from '../../utils'
import { useLogger } from '../../utils/logger'
import { toast } from '../../utils/toast'

const router = useRouter()
const logger = useLogger('Collections')
const { confirm, ConfirmDialog } = useConfirm()
const collections = ref([])
const loading = ref(false)
const error = ref('')
const page = ref(1)
const total = ref(0)
const totalPages = ref(1)
const pageSize = 10

async function confirmRemove(item) {
  const ok = await confirm(`确定要取消收藏「${item.title}」吗？`, '取消收藏')
  if (ok) removeCollection(item.postId)
}

async function fetchCollections() {
  loading.value = true
  error.value = ''
  try {
    const response = await collectApi.getMyCollections({ pageNum: page.value, pageSize })
    const data = response.data || {}
    collections.value = data.records || []
    total.value = data.total || 0
    totalPages.value = data.pages || 1
  } catch (err) {
    logger.error('Failed to fetch collections', { error: err.message })
    error.value = '加载收藏列表失败'
  } finally {
    loading.value = false
  }
}

async function removeCollection(postId) {
  try {
    await collectApi.toggleCollect(postId)
    collections.value = collections.value.filter(c => c.postId !== postId)
    total.value = Math.max(0, total.value - 1)
    toast.success('已取消收藏')
    if (collections.value.length === 0 && page.value > 1) {
      page.value--
      fetchCollections()
    }
  } catch (err) {
    logger.error('Failed to remove collection', { error: err.message })
    toast.error('操作失败')
  }
}

onMounted(() => {
  fetchCollections()
})
</script>

<style scoped>
.collections-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
}

.back-btn { display: flex; align-items: center; gap: 4px; padding: 8px 12px; background: transparent; border: 1px solid var(--border); border-radius: 8px; color: var(--text-secondary); cursor: pointer; font-size: 0.875rem; transition: all 0.2s; width: fit-content; margin-bottom: 16px; }
.back-btn:hover { background: var(--border); color: var(--text-primary); }

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 1.5rem;
  font-weight: 700;
}

.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.skeleton-card-item {
  padding: 20px;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  background: var(--surface);
}

.skeleton-card-title {
  width: 60%;
  height: 20px;
  margin-bottom: 10px;
}

.skeleton-card-text {
  width: 85%;
  height: 14px;
  margin-bottom: 6px;
}

.skeleton-card-text.short {
  width: 40%;
}

.skeleton-card-meta {
  width: 180px;
  height: 14px;
  margin-top: 12px;
}

.collection-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.collection-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  gap: 16px;
  transition: all 0.2s;
}

.collection-card:hover {
  box-shadow: var(--shadow);
}

.collection-card-body {
  flex: 1;
  min-width: 0;
}

.collection-title {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 6px;
}

.collection-title a {
  color: var(--text-primary);
  text-decoration: none;
  transition: color 0.2s;
}

.collection-title a:hover {
  color: var(--primary);
}

.collection-excerpt {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.collection-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.75rem;
  color: var(--text-muted);
}

.collection-remove-btn {
  flex-shrink: 0;
  color: var(--text-muted);
}

.collection-remove-btn:hover {
  color: var(--error) !important;
  background: rgba(239, 68, 68, 0.08) !important;
}

.error-card {
  padding: 40px 24px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.error-card p {
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.empty-state {
  padding: 80px 24px;
  text-align: center;
}

.empty-icon {
  color: var(--text-muted);
  opacity: 0.3;
  margin-bottom: 16px;
}

.empty-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
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
  padding-top: 20px;
}

.pagination-info {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  animation: fadeIn 0.2s ease;
}

.modal-dialog {
  width: 380px;
  max-width: 90vw;
  padding: 28px;
}

.modal-title {
  font-size: 1.125rem;
  font-weight: 600;
  margin-bottom: 8px;
}

.modal-message {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-bottom: 24px;
  line-height: 1.5;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@media (max-width: 768px) {
  .collections-page {
    padding: 16px;
  }

  .collection-card {
    flex-direction: column;
    padding: 16px;
    gap: 12px;
  }
}
</style>
