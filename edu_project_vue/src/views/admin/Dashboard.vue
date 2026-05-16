<template>
  <div class="admin-dashboard">
    <div class="admin-container">
      <div class="admin-header">
        <h1>管理后台</h1>
      </div>
      
      <div class="admin-nav">
        <router-link to="/admin" class="nav-item active">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="3" width="7" height="7"/>
            <rect x="14" y="3" width="7" height="7"/>
            <rect x="14" y="14" width="7" height="7"/>
            <rect x="3" y="14" width="7" height="7"/>
          </svg>
          仪表盘
        </router-link>
        <router-link to="/admin/users" class="nav-item">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
          用户管理
        </router-link>
        <router-link to="/admin/posts" class="nav-item">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
            <line x1="16" y1="13" x2="8" y2="13"/>
            <line x1="16" y1="17" x2="8" y2="17"/>
          </svg>
          文章管理
        </router-link>
        <router-link to="/admin/reports" class="nav-item">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
            <line x1="12" y1="9" x2="12" y2="13"/>
            <line x1="12" y1="17" x2="12.01" y2="17"/>
          </svg>
          举报管理
        </router-link>
        <router-link to="/admin/statistics" class="nav-item">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="20" x2="18" y2="10"/>
            <line x1="12" y1="20" x2="12" y2="4"/>
            <line x1="6" y1="20" x2="6" y2="14"/>
          </svg>
          数据统计
        </router-link>
      </div>
      
      <div class="admin-content">
        <div v-if="loading" class="loading-skeleton">
          <div class="stats-grid">
            <div v-for="i in 4" :key="i" class="stat-card card">
              <div class="stat-icon-skeleton"></div>
              <div class="stat-info">
                <div class="skeleton-value"></div>
                <div class="skeleton-label"></div>
              </div>
            </div>
          </div>
        </div>
        
        <div v-else-if="error" class="error-state">
          <svg width="60" height="60" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          <h3>{{ error }}</h3>
          <button class="btn btn-primary" @click="fetchStats">重试</button>
        </div>
        
        <div v-else class="stats-grid">
          <div class="stat-card card">
            <div class="stat-icon users">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
              </svg>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stats.userCount || 0 }}</span>
              <span class="stat-label">用户总数</span>
            </div>
          </div>
          
          <div class="stat-card card">
            <div class="stat-icon posts">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </svg>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stats.postCount || 0 }}</span>
              <span class="stat-label">文章总数</span>
            </div>
          </div>
          
          <div class="stat-card card">
            <div class="stat-icon comments">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              </svg>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stats.commentCount || 0 }}</span>
              <span class="stat-label">评论总数</span>
            </div>
          </div>
          
          <div class="stat-card card">
            <div class="stat-icon reports">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
              </svg>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stats.pendingReportCount || 0 }}</span>
              <span class="stat-label">待处理举报</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { adminApi } from '../../api/admin'
import { useLogger } from '../../utils/logger'

const logger = useLogger('AdminDashboard')
const stats = ref({})
const loading = ref(true)
const error = ref('')
let refreshTimer = null

const fetchStats = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await adminApi.getStatistics()
    stats.value = response.data || {}
  } catch (err) {
    logger.error('Failed to fetch stats', { error: err.message })
    error.value = '加载失败，请重试'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchStats()
  refreshTimer = setInterval(fetchStats, 60000)
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
})
</script>

<style scoped>
.admin-dashboard {
  padding: var(--spacing-lg);
}

.admin-container {
  max-width: 1200px;
  margin: 0 auto;
}

.admin-header {
  margin-bottom: var(--spacing-lg);
}

.admin-header h1 {
  font-size: 1.5rem;
  font-weight: 700;
}

.admin-nav {
  display: flex;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-xl);
  padding-bottom: var(--spacing-md);
  border-bottom: 1px solid var(--border);
}

.nav-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  font-size: 0.875rem;
  color: var(--text-secondary);
  text-decoration: none;
  border-radius: var(--radius);
  transition: all var(--transition);
}

.nav-item:hover {
  background: var(--background);
  color: var(--primary);
}

.nav-item.active {
  background: var(--primary-light);
  color: var(--primary);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-md);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
}

.stat-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
}

.stat-icon.users {
  background: var(--blue-light, rgba(59, 130, 246, 0.1));
  color: var(--blue, #3B82F6);
}

.stat-icon.posts {
  background: var(--green-light, rgba(16, 185, 129, 0.1));
  color: var(--green, #10B981);
}

.stat-icon.comments {
  background: var(--purple-light, rgba(139, 92, 246, 0.1));
  color: var(--purple, #8B5CF6);
}

.stat-icon.reports {
  background: var(--red-light, rgba(239, 68, 68, 0.1));
  color: var(--red, #EF4444);
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-label {
  font-size: 0.75rem;
  color: var(--text-muted);
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .admin-nav {
    flex-wrap: wrap;
  }
}

.loading-skeleton {
  padding: var(--spacing-lg) 0;
}

.stat-icon-skeleton {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  background: linear-gradient(90deg, var(--skeleton-base) 25%, var(--skeleton-highlight) 50%, var(--skeleton-base) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-value {
  width: 60px;
  height: 24px;
  background: linear-gradient(90deg, var(--skeleton-base) 25%, var(--skeleton-highlight) 50%, var(--skeleton-base) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
  margin-bottom: 4px;
}

.skeleton-label {
  width: 80px;
  height: 12px;
  background: linear-gradient(90deg, var(--skeleton-base) 25%, var(--skeleton-highlight) 50%, var(--skeleton-base) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
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
