<template>
  <div class="admin-users">
    <div class="admin-container">
      <div class="page-header">
        <h1>用户管理</h1>
      </div>
      
      <div class="search-bar">
        <input 
          v-model="searchQuery" 
          type="text" 
          placeholder="搜索用户..."
          @keyup.enter="fetchUsers"
        />
        <button class="btn btn-primary btn-sm" @click="fetchUsers">搜索</button>
      </div>
      
      <div v-if="loading" class="loading-skeleton">
        <div class="table-container card">
          <table class="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>用户</th>
                <th>邮箱</th>
                <th>角色</th>
                <th>状态</th>
                <th>注册时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="i in 5" :key="i">
                <td><div class="sk-line w-30"></div></td>
                <td><div class="sk-line w-60"></div></td>
                <td><div class="sk-line w-70"></div></td>
                <td><div class="sk-line w-40"></div></td>
                <td><div class="sk-line w-40"></div></td>
                <td><div class="sk-line w-60"></div></td>
                <td><div class="sk-line w-60"></div></td>
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
        <button class="btn btn-primary" @click="fetchUsers">重试</button>
      </div>
      
      <div v-else class="table-container card">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>用户</th>
              <th>邮箱</th>
              <th>角色</th>
              <th>状态</th>
              <th>注册时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="users.length === 0">
              <td colspan="7" class="empty-cell">暂无用户</td>
            </tr>
            <tr v-for="user in users" :key="user.id">
              <td>{{ user.id }}</td>
              <td>
                <div class="user-cell">
                  <img :src="user.avatar || '/default-avatar.png'" :alt="user.username" class="user-avatar" />
                  <span>{{ user.nickname || user.username }}</span>
                </div>
              </td>
              <td>{{ user.email }}</td>
              <td>
                <span class="badge" :class="user.role === 'admin' ? 'badge-warning' : ''">
                  {{ user.role }}
                </span>
              </td>
              <td>
                <span class="status" :class="user.status === 1 ? 'active' : 'disabled'">
                  {{ user.status === 1 ? '正常' : '禁用' }}
                </span>
              </td>
              <td>{{ formatDate(user.createTime) }}</td>
              <td>
                <div class="actions">
                  <button 
                    class="btn btn-sm btn-ghost"
                    @click="toggleUserStatus(user)"
                  >
                    {{ user.status === 1 ? '禁用' : '启用' }}
                  </button>
                  <button 
                    class="btn btn-sm btn-ghost"
                    @click="resetPassword(user)"
                  >
                    重置密码
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
import { ref, onMounted } from 'vue'
import { adminApi } from '../../api/admin'
import { formatDate } from '../../utils'
import { useLogger } from '../../utils/logger'
import { toast } from '../../utils/toast'
import { useConfirm } from '../../composables/useConfirm'

const logger = useLogger('AdminUsers')
const { confirm, ConfirmDialog } = useConfirm()

const users = ref([])
const searchQuery = ref('')
const currentPage = ref(1)
const totalPages = ref(1)
const loading = ref(true)
const error = ref('')

const fetchUsers = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await adminApi.getUserList({
      pageNum: currentPage.value,
      pageSize: 20,
      keyword: searchQuery.value
    })
    const pageData = response.data || {}
    users.value = pageData.records || []
    totalPages.value = pageData.pages || 1
  } catch (err) {
    logger.error('Failed to fetch users', { error: err.message })
    error.value = '加载失败，请重试'
    users.value = []
  } finally {
    loading.value = false
  }
}

const toggleUserStatus = async (user) => {
  const newStatus = user.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  const ok = await confirm(`确定${action}该用户吗？`, `${action}用户`)
  if (!ok) return
  
  try {
    await adminApi.handleUserStatus(user.id, { status: newStatus })
    user.status = newStatus
  } catch (err) {
    logger.error('Failed to toggle user status', { error: err.message })
    toast.error('操作失败')
  }
}

const resetPassword = async (user) => {
  const ok = await confirm('确定重置该用户密码吗？', '重置密码')
  if (!ok) return
  
  try {
    const res = await adminApi.resetUserPassword(user.id)
    const newPassword = res.data
    toast.success(`密码已重置成功，新密码为: ${newPassword}`)
  } catch (err) {
    logger.error('Failed to reset password', { error: err.message })
    toast.error('操作失败')
  }
}

const changePage = (page) => {
  currentPage.value = page
  fetchUsers()
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.admin-users {
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

.search-bar {
  display: flex;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-lg);
}

.search-bar input {
  flex: 1;
  padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid var(--border);
  border-radius: var(--radius);
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

.user-cell {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  object-fit: cover;
}

.badge {
  padding: 0.25rem 0.5rem;
  font-size: 0.75rem;
  border-radius: var(--radius);
  background: var(--background);
  color: var(--text-secondary);
}

.badge-warning {
  background: rgba(245, 158, 11, 0.1);
  color: #F59E0B;
}

.status {
  font-size: 0.75rem;
}

.status.active {
  color: var(--success);
}

.status.disabled {
  color: var(--error);
}

.actions {
  display: flex;
  gap: var(--spacing-xs);
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
.sk-line.w-60 { width: 60%; }
.sk-line.w-70 { width: 70%; }

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
