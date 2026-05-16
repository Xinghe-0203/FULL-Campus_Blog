<template>
  <div class="admin-posts">
    <div class="admin-container">
      <div class="page-header">
        <h1>文章管理</h1>
      </div>
      
      <div class="tabs">
        <button 
          class="tab-btn"
          :class="{ active: activeTab === 'all' }"
          @click="activeTab = 'all'"
        >
          全部文章
        </button>
        <button 
          class="tab-btn"
          :class="{ active: activeTab === 'pending' }"
          @click="activeTab = 'pending'"
        >
          待审核
        </button>
      </div>
      
      <div v-if="loading" class="loading-skeleton">
        <div class="table-container card">
          <table class="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>标题</th>
                <th>作者</th>
                <th>状态</th>
                <th>发布时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="i in 5" :key="i">
                <td><div class="sk-line w-30"></div></td>
                <td><div class="sk-line w-80"></div></td>
                <td><div class="sk-line w-50"></div></td>
                <td><div class="sk-line w-40"></div></td>
                <td><div class="sk-line w-60"></div></td>
                <td><div class="sk-line w-50"></div></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      
      <div v-else-if="error" class="error-state">
        <svg width="60" height="60" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
        <h3>{{ error }}</h3>
        <button class="btn btn-primary" @click="fetchPosts">重试</button>
      </div>
      
      <div v-else class="table-container card">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>标题</th>
              <th>作者</th>
              <th>状态</th>
              <th>发布时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="posts.length === 0">
              <td colspan="6" class="empty-cell">暂无文章</td>
            </tr>
            <tr v-for="post in posts" :key="post.id">
              <td>{{ post.id }}</td>
              <td>
                <router-link :to="`/post/${post.id}`" class="post-title-link">
                  {{ post.title }}
                </router-link>
              </td>
              <td>{{ post.nickname || post.username }}</td>
              <td>
                <span class="status" :class="getStatusClass(post.status)">
                  {{ getStatusText(post.status) }}
                </span>
              </td>
              <td>{{ formatDate(post.createTime) }}</td>
              <td>
                <div class="actions">
                  <button 
                    v-if="post.status === 0"
                    class="btn btn-sm btn-primary"
                    @click="approvePost(post)"
                  >
                    通过
                  </button>
                  <button 
                    v-if="post.status === 0"
                    class="btn btn-sm btn-ghost"
                    @click="rejectPost(post)"
                  >
                    拒绝
                  </button>
                  <button 
                    class="btn btn-sm btn-ghost danger"
                    @click="deletePost(post)"
                  >
                    删除
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <div v-if="totalPages > 1" class="pagination">
        <button 
          class="pagination-btn"
          :disabled="currentPage <= 1"
          @click="changePage(currentPage - 1)"
        >
          上一页
        </button>
        <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
        <button 
          class="pagination-btn"
          :disabled="currentPage >= totalPages"
          @click="changePage(currentPage + 1)"
        >
          下一页
        </button>
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
const currentPage = ref(1)
const totalPages = ref(1)
const loading = ref(true)
const error = ref('')

const fetchPosts = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = activeTab.value === 'pending'
      ? await adminApi.getReviewList({ pageNum: currentPage.value, pageSize: 20 })
      : await adminApi.getPostList({ pageNum: currentPage.value, pageSize: 20 })
    posts.value = response.data?.records || []
    totalPages.value = response.data?.pages || 1
  } catch (err) {
    logger.error('Failed to fetch posts', { error: err.message })
    error.value = '加载失败，请重试'
    posts.value = []
  } finally {
    loading.value = false
  }
}

const getStatusClass = (status) => {
  const classes = {
    0: 'warning',
    1: 'active',
    2: 'danger'
  }
  return classes[status] || ''
}

const getStatusText = (status) => {
  const texts = {
    0: '待审核',
    1: '已发布',
    2: '已驳回'
  }
  return texts[status] || '未知'
}

const approvePost = async (post) => {
  try {
    await adminApi.approvePost(post.id)
    post.status = 1
    toast.success('审核通过')
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
  fetchPosts()
})

onMounted(() => {
  fetchPosts()
})
</script>

<style scoped>
.admin-posts {
  padding: var(--spacing-lg);
}

.admin-container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: var(--spacing-lg);
}

.page-header h1 {
  font-size: 1.5rem;
  font-weight: 700;
}

.tabs {
  display: flex;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-lg);
}

.tab-btn {
  padding: var(--spacing-sm) var(--spacing-md);
  font-size: 0.875rem;
  background: none;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition);
}

.tab-btn:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.tab-btn.active {
  background: var(--primary);
  border-color: var(--primary);
  color: white;
}

.table-container {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: var(--spacing-md);
  text-align: left;
  border-bottom: 1px solid var(--border);
}

.data-table th {
  font-weight: 600;
  color: var(--text-primary);
  background: var(--background);
}

.post-title-link {
  color: var(--text-primary);
  text-decoration: none;
}

.post-title-link:hover {
  color: var(--primary);
}

.status {
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  border-radius: var(--radius);
}

.status.active {
  background: rgba(16, 185, 129, 0.1);
  color: var(--success);
}

.status.warning {
  background: rgba(245, 158, 11, 0.1);
  color: var(--warning);
}

.status.danger {
  background: rgba(239, 68, 68, 0.1);
  color: var(--error);
}

.actions {
  display: flex;
  gap: var(--spacing-xs);
}

.danger {
  color: var(--error) !important;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-md);
  margin-top: var(--spacing-lg);
}

.page-info {
  font-size: 0.875rem;
  color: var(--text-muted);
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

.sk-line.w-30 { width: 30%; }
.sk-line.w-40 { width: 40%; }
.sk-line.w-50 { width: 50%; }
.sk-line.w-60 { width: 60%; }
.sk-line.w-80 { width: 80%; }

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.empty-cell {
  text-align: center;
  padding: var(--spacing-xl);
  color: var(--text-muted);
  font-size: 0.875rem;
}

.error-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-secondary);
}

.error-state h3 {
  margin: 12px 0 16px;
  font-size: 16px;
}
</style>
