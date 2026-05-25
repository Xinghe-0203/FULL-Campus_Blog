<template>
  <div class="admin-posts">
    <div class="admin-container">
      <div class="page-header">
        <h2>文章管理</h2>
      </div>

      <div class="filter-bar">
        <div class="tabs glass">
          <button class="tab-btn" :class="{ active: activeTab === 'all' }" @click="activeTab = 'all'">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/>
            </svg>
            全部文章
          </button>
          <button class="tab-btn" :class="{ active: activeTab === 'pending' }" @click="activeTab = 'pending'">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
            </svg>
            待审核
            <span v-if="pendingCount > 0" class="tab-badge">{{ pendingCount }}</span>
          </button>
        </div>

        <div class="search-filter glass">
          <div class="search-input-wrapper">
            <svg class="search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
            <input v-model="searchQuery" type="text" placeholder="搜索文章标题..." @keyup.enter="fetchPosts" />
          </div>
          <select v-model="statusFilter" class="filter-select" @change="fetchPosts">
            <option value="">全部状态</option>
            <option value="0">待审核</option>
            <option value="1">已发布</option>
            <option value="2">已驳回</option>
          </select>
          <button class="btn btn-primary btn-sm" @click="fetchPosts">搜索</button>
        </div>
      </div>

      <div v-if="loading" class="loading-skeleton">
        <div class="table-container glass">
          <table class="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>标题</th>
                <th>作者</th>
                <th>分类</th>
                <th>状态</th>
                <th>发布时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="i in 5" :key="i">
                <td><div class="sk-line w-20"></div></td>
                <td><div class="sk-line w-80"></div></td>
                <td><div class="sk-line w-50"></div></td>
                <td><div class="sk-line w-40"></div></td>
                <td><div class="sk-line w-40"></div></td>
                <td><div class="sk-line w-60"></div></td>
                <td><div class="sk-line w-50"></div></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div v-else-if="error" class="error-state glass">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
        <h3>{{ error }}</h3>
        <button class="btn btn-primary" @click="fetchPosts">重试</button>
      </div>

      <div v-else class="table-container glass">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>标题</th>
              <th>作者</th>
              <th>分类</th>
              <th>状态</th>
              <th>发布时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="posts.length === 0">
              <td colspan="7" class="empty-cell">
                <div class="empty-content">
                  <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/>
                  </svg>
                  <span>暂无文章数据</span>
                </div>
              </td>
            </tr>
            <tr v-for="post in posts" :key="post.id">
              <td class="id-cell">{{ post.id }}</td>
              <td>
                <router-link :to="`/post/${post.id}`" class="post-title-link">
                  {{ post.title }}
                </router-link>
              </td>
              <td class="author-cell">{{ post.nickname || post.username }}</td>
              <td>
                <span v-if="post.category" class="category-badge">{{ post.category }}</span>
                <span v-else class="text-muted">-</span>
              </td>
              <td>
                <span class="status-badge" :class="getStatusClass(post.status)">
                  <span class="status-dot" :class="getStatusDotClass(post.status)"></span>
                  {{ getStatusText(post.status) }}
                </span>
              </td>
              <td class="time-cell">{{ formatDate(post.createTime) }}</td>
              <td>
                <div class="actions">
                  <button v-if="post.status === 0" class="btn btn-xs btn-success-outline" @click="approvePost(post)">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>
                    通过
                  </button>
                  <button v-if="post.status === 0" class="btn btn-xs btn-danger-outline" @click="rejectPost(post)">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                    拒绝
                  </button>
                  <button class="btn btn-xs btn-ghost danger" @click="deletePost(post)">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                    </svg>
                    删除
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="totalPages > 1" class="pagination-wrapper">
        <div class="pagination glass">
          <button class="pagination-btn" :disabled="currentPage <= 1" @click="changePage(currentPage - 1)">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
            上一页
          </button>
          <span class="page-info">第 {{ currentPage }} / {{ totalPages }} 页</span>
          <button class="pagination-btn" :disabled="currentPage >= totalPages" @click="changePage(currentPage + 1)">
            下一页
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
          </button>
        </div>
      </div>
    </div>
    <ConfirmDialog />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { adminApi } from '../../api/admin'
import { formatDate } from '../../utils'
import { useLogger } from '../../utils/logger'
import { toast } from '../../utils/toast'
import { useConfirm } from '../../composables/useConfirm'

const logger = useLogger('AdminPosts')
const { confirm, ConfirmDialog } = useConfirm()

const posts = ref([])
const activeTab = ref('all')
const searchQuery = ref('')
const statusFilter = ref('')
const currentPage = ref(1)
const totalPages = ref(1)
const pendingCount = ref(0)
const loading = ref(true)
const error = ref('')

const fetchPosts = async () => {
  loading.value = true
  error.value = ''
  try {
    const params = { pageNum: currentPage.value, pageSize: 20 }
    if (activeTab.value === 'pending') {
      const response = await adminApi.getReviewList(params)
      const data = response.data || {}
      posts.value = data.records || []
      totalPages.value = data.pages || 1
      pendingCount.value = data.total || 0
    } else {
      if (searchQuery.value) params.keyword = searchQuery.value
      if (statusFilter.value !== '') params.status = statusFilter.value
      const response = await adminApi.getPostList(params)
      const data = response.data || {}
      posts.value = data.records || []
      totalPages.value = data.pages || 1
    }
  } catch (err) {
    logger.error('Failed to fetch posts', { error: err.message })
    error.value = '加载失败，请重试'
    posts.value = []
  } finally {
    loading.value = false
  }
}

const getStatusClass = (status) => {
  const classes = { 0: 'status-pending', 1: 'status-active', 2: 'status-rejected' }
  return classes[status] || ''
}

const getStatusDotClass = (status) => {
  const classes = { 0: 'pending', 1: 'active', 2: 'rejected' }
  return classes[status] || ''
}

const getStatusText = (status) => {
  const texts = { 0: '待审核', 1: '已发布', 2: '已驳回' }
  return texts[status] || '未知'
}

const approvePost = async (post) => {
  const ok = await confirm('确定通过该文章审核吗？', '通过审核')
  if (!ok) return
  try {
    await adminApi.approvePost(post.id)
    post.status = 1
    toast.success('审核通过')
    if (activeTab.value === 'pending') {
      posts.value = posts.value.filter(p => p.id !== post.id)
    }
  } catch (err) {
    logger.error('Failed to approve post', { error: err.message })
    toast.error('操作失败')
  }
}

const rejectPost = async (post) => {
  const reason = prompt('请输入驳回原因:')
  if (!reason || !reason.trim()) return
  try {
    await adminApi.rejectPost(post.id, reason.trim())
    post.status = 2
    toast.success('已拒绝')
    if (activeTab.value === 'pending') {
      posts.value = posts.value.filter(p => p.id !== post.id)
    }
  } catch (err) {
    logger.error('Failed to reject post', { error: err.message })
    toast.error('操作失败')
  }
}

const deletePost = async (post) => {
  const ok = await confirm('确定删除该文章吗？此操作不可恢复。', '删除文章')
  if (!ok) return
  try {
    await adminApi.deletePost(post.id)
    posts.value = posts.value.filter(p => p.id !== post.id)
    toast.success('删除成功')
  } catch (err) {
    logger.error('Failed to delete post', { error: err.message })
    toast.error('删除失败')
  }
}

const changePage = (page) => {
  currentPage.value = page
  fetchPosts()
}

watch(activeTab, () => {
  currentPage.value = 1
  searchQuery.value = ''
  statusFilter.value = ''
  fetchPosts()
})

onMounted(() => {
  fetchPosts()
})
</script>

<style scoped>
.admin-posts {
  padding: var(--spacing-lg);
  min-height: 100vh;
}

.admin-container {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: var(--spacing-lg);
}

.page-header h2 {
  font-size: 1.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, var(--primary-start), var(--primary-end));
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.filter-bar {
  display: flex;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
  flex-wrap: wrap;
}

.tabs {
  display: flex;
  gap: var(--spacing-xs);
  padding: var(--spacing-xs);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--glass-shadow);
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm) var(--spacing-md);
  font-size: 0.8125rem;
  background: none;
  border: none;
  border-radius: var(--radius);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition);
  position: relative;
}

.tab-btn:hover {
  background: var(--primary-light);
  color: var(--primary);
}

.tab-btn.active {
  background: linear-gradient(135deg, var(--primary-start), var(--primary-end));
  color: var(--text-inverse);
  box-shadow: var(--shadow-sm);
}

.tab-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  font-size: 0.6875rem;
  font-weight: 600;
  background: var(--error);
  color: white;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-filter {
  flex: 1;
  display: flex;
  gap: var(--spacing-sm);
  padding: var(--spacing-xs);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--glass-shadow);
  min-width: 300px;
}

.search-input-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--surface);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius);
  transition: all var(--transition);
}

.search-input-wrapper:focus-within {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-light);
}

.search-icon {
  color: var(--text-muted);
  flex-shrink: 0;
}

.search-filter input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 0.8125rem;
  color: var(--text-primary);
  outline: none;
}

.search-filter input::placeholder {
  color: var(--text-muted);
}

.filter-select {
  padding: var(--spacing-sm) var(--spacing-md);
  font-size: 0.8125rem;
  background: var(--surface);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius);
  color: var(--text-primary);
  cursor: pointer;
  transition: all var(--transition);
}

.filter-select:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-light);
}

.table-container {
  overflow-x: auto;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border-wet);
  border-radius: var(--radius-lg);
  box-shadow: var(--glass-shadow-wet);
  position: relative;
}

.table-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.15), transparent);
  pointer-events: none;
  z-index: 1;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: var(--spacing-md);
  text-align: left;
  border-bottom: 1px solid var(--glass-border);
}

.data-table th {
  font-weight: 600;
  color: var(--text-primary);
  background: var(--surface);
  font-size: 0.8125rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.data-table tbody tr {
  transition: all var(--transition);
}

.data-table tbody tr:hover {
  background: var(--primary-light);
}

.data-table tbody tr:last-child td {
  border-bottom: none;
}

.id-cell {
  font-family: var(--font-mono);
  font-size: 0.8125rem;
  color: var(--text-muted);
}

.post-title-link {
  color: var(--text-primary);
  text-decoration: none;
  font-weight: 500;
  transition: all var(--transition);
}

.post-title-link:hover {
  color: var(--primary);
}

.author-cell {
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.time-cell {
  font-size: 0.8125rem;
  color: var(--text-muted);
}

.category-badge {
  display: inline-flex;
  align-items: center;
  padding: 0.25rem 0.625rem;
  font-size: 0.75rem;
  font-weight: 500;
  border-radius: var(--radius-full);
  background: var(--primary-light);
  color: var(--primary);
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.25rem 0.625rem;
  font-size: 0.75rem;
  font-weight: 500;
  border-radius: var(--radius-full);
}

.status-active {
  background: var(--success-light);
  color: var(--success);
}

.status-pending {
  background: var(--warning-light);
  color: var(--warning);
}

.status-rejected {
  background: var(--error-light);
  color: var(--error);
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: var(--radius-full);
}

.status-dot.active { background: var(--success); }
.status-dot.pending { background: var(--warning); }
.status-dot.rejected { background: var(--error); }

.actions {
  display: flex;
  gap: var(--spacing-xs);
  flex-wrap: wrap;
}

.btn-danger-outline {
  color: var(--error) !important;
}

.btn-danger-outline:hover {
  background: var(--error-light) !important;
  color: var(--error) !important;
}

.btn-success-outline {
  color: var(--success) !important;
}

.btn-success-outline:hover {
  background: var(--success-light) !important;
  color: var(--success) !important;
}

.pagination-wrapper {
  margin-top: var(--spacing-lg);
  display: flex;
  justify-content: center;
}

.pagination {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--glass-shadow);
}

.page-info {
  font-size: 0.8125rem;
  color: var(--text-muted);
  padding: 0 var(--spacing-md);
}

.empty-cell {
  text-align: center;
  padding: var(--spacing-2xl);
}

.empty-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-sm);
  color: var(--text-muted);
}

.error-state {
  text-align: center;
  padding: var(--spacing-3xl);
  color: var(--text-secondary);
}

.error-state h3 {
  margin: var(--spacing-md) 0 var(--spacing-lg);
  font-size: 1rem;
}

.loading-skeleton {
  padding: var(--spacing-lg) 0;
}

.sk-line {
  height: 14px;
  border-radius: 4px;
  background: linear-gradient(90deg, var(--skeleton-base) 25%, var(--skeleton-highlight) 50%, var(--skeleton-base) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.sk-line.w-20 { width: 20%; }
.sk-line.w-40 { width: 40%; }
.sk-line.w-50 { width: 50%; }
.sk-line.w-60 { width: 60%; }
.sk-line.w-80 { width: 80%; }

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

@media (max-width: 768px) {
  .filter-bar {
    flex-direction: column;
  }

  .search-filter {
    flex-direction: column;
  }

  .actions {
    flex-direction: column;
  }
}
</style>
