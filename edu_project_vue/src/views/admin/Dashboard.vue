<template>
  <div class="admin-dashboard">
    <div class="admin-container">
      <div class="admin-header">
        <div>
          <h1>仪表盘</h1>
          <p class="header-subtitle">校园博客论坛管理系统</p>
        </div>
        <div class="refresh-indicator" :class="{ refreshing: loading }">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="23 4 23 10 17 10"/>
            <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
          </svg>
          <span>{{ loading ? '刷新中...' : '自动刷新 60s' }}</span>
        </div>
      </div>

      <div v-if="loading" class="loading-skeleton">
        <div class="stats-grid">
          <div v-for="i in 8" :key="i" class="stat-card glass">
            <div class="stat-icon-skeleton"></div>
            <div class="stat-info">
              <div class="skeleton-value"></div>
              <div class="skeleton-label"></div>
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="error" class="error-state glass">
        <svg width="60" height="60" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
        <h3>{{ error }}</h3>
        <button class="btn btn-primary" @click="fetchStats">重试</button>
      </div>

      <div v-else>
        <div class="stats-grid">
          <div class="stat-card glass stat-users gradient-border-blue">
            <div class="stat-icon gradient-info">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
              </svg>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ formatNumber(stats.userCount || 0) }}</span>
              <span class="stat-label">用户总数</span>
            </div>
          </div>

          <div class="stat-card glass stat-posts gradient-border-green">
            <div class="stat-icon gradient-success">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </svg>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ formatNumber(stats.postCount || 0) }}</span>
              <span class="stat-label">文章总数</span>
            </div>
          </div>

          <div class="stat-card glass gradient-border-purple">
            <div class="stat-icon" style="background: linear-gradient(135deg, var(--purple), #7C3AED);">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              </svg>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ formatNumber(stats.circlePostCount || 0) }}</span>
              <span class="stat-label">校友圈动态</span>
            </div>
          </div>

          <div class="stat-card glass gradient-border-purple">
            <div class="stat-icon gradient-warning">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                <line x1="12" y1="7" x2="12" y2="13"/>
                <line x1="12" y1="17" x2="12.01" y2="17"/>
              </svg>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ formatNumber(stats.commentCount || 0) }}</span>
              <span class="stat-label">评论总数</span>
            </div>
          </div>

          <div class="stat-card glass gradient-border-blue">
            <div class="stat-icon" style="background: linear-gradient(135deg, var(--info), #2563EB);">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
                <line x1="7" y1="7" x2="7.01" y2="7"/>
              </svg>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ formatNumber(stats.tagCount || 0) }}</span>
              <span class="stat-label">标签总数</span>
            </div>
          </div>

          <div class="stat-card glass gradient-border-orange">
            <div class="stat-icon" style="background: linear-gradient(135deg, var(--orange), #EA580C);">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <path d="M2 12h20"/>
                <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
              </svg>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ formatNumber(stats.topicCount || 0) }}</span>
              <span class="stat-label">话题总数</span>
            </div>
          </div>

          <div class="stat-card glass gradient-border-red">
            <div class="stat-icon gradient-error">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
              </svg>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ formatNumber(stats.pendingReportCount || 0) }}</span>
              <span class="stat-label">待处理举报</span>
            </div>
          </div>

          <div class="stat-card glass gradient-border-green">
            <div class="stat-icon gradient-success">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
              </svg>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ formatNumber(stats.userStats?.activeUsers || 0) }}</span>
              <span class="stat-label">活跃用户</span>
            </div>
          </div>
        </div>

        <div class="dashboard-grid">
          <div class="glass quick-actions">
            <h3 class="section-title">快捷操作</h3>
            <div class="action-grid">
              <router-link to="/admin/users" class="action-btn">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                  <circle cx="9" cy="7" r="4"/>
                </svg>
                <span>用户管理</span>
              </router-link>
              <router-link to="/admin/posts" class="action-btn">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                </svg>
                <span>文章管理</span>
              </router-link>
              <router-link to="/admin/circle" class="action-btn">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                </svg>
                <span>校友圈</span>
              </router-link>
              <router-link to="/admin/tags" class="action-btn">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
                  <line x1="7" y1="7" x2="7.01" y2="7"/>
                </svg>
                <span>标签管理</span>
              </router-link>
              <router-link to="/admin/topics" class="action-btn">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10"/>
                  <path d="M2 12h20"/>
                  <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
                </svg>
                <span>话题管理</span>
              </router-link>
              <router-link to="/admin/reports" class="action-btn">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
                  <line x1="12" y1="9" x2="12" y2="13"/>
                </svg>
                <span>举报管理</span>
              </router-link>
            </div>
          </div>

          <div class="glass today-overview">
            <h3 class="section-title">今日概览</h3>
            <div class="today-list">
              <div class="today-item">
                <div class="today-dot info"></div>
                <div class="today-content">
                  <span class="today-text">今日新增用户</span>
                  <span class="today-value text-info">+{{ stats.userStats?.todayNewUsers || 0 }}</span>
                </div>
              </div>
              <div class="today-item">
                <div class="today-dot success"></div>
                <div class="today-content">
                  <span class="today-text">今日新增文章</span>
                  <span class="today-value text-success">+{{ stats.postStats?.todayNewPosts || 0 }}</span>
                </div>
              </div>
              <div class="today-item">
                <div class="today-dot" style="background: var(--purple);"></div>
                <div class="today-content">
                  <span class="today-text">今日新增动态</span>
                  <span class="today-value" style="color: var(--purple);">+{{ stats.circleStats?.todayNewPosts || 0 }}</span>
                </div>
              </div>
              <div class="today-item">
                <div class="today-dot error"></div>
                <div class="today-content">
                  <span class="today-text">待处理举报</span>
                  <span class="today-value text-error">{{ stats.pendingReportCount || 0 }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { adminApi } from '../../api/admin'
import { formatNumber } from '../../utils'
import { useLogger } from '../../utils/logger'

const logger = useLogger('AdminDashboard')
const stats = ref<Record<string, any>>({})
const loading = ref(true)
const error = ref('')
let refreshTimer: ReturnType<typeof setInterval> | null = null

const fetchStats = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await adminApi.getStatistics()
    stats.value = response.data || {}
  } catch (err: any) {
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
  max-width: 1400px;
  margin: 0 auto;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--spacing-lg);
}

.admin-header h1 {
  font-size: 1.75rem;
  font-weight: 700;
  background: linear-gradient(135deg, var(--primary-start), var(--primary-end));
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.header-subtitle {
  font-size: 0.875rem;
  color: var(--text-muted);
  margin-top: 0.25rem;
}

.refresh-indicator {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: 0.75rem;
  color: var(--text-muted);
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--radius-full);
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
}

.refresh-indicator.refreshing svg {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-xl);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border-wet);
  border-radius: var(--radius-lg);
  box-shadow: var(--glass-shadow-wet);
  transition: all var(--transition-slow);
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  pointer-events: none;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg), var(--glass-shadow-wet);
}

.stat-icon {
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  color: white;
  flex-shrink: 0;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 0.75rem;
  color: var(--text-muted);
  margin-top: 0.25rem;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-md);
}

.glass {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border-wet);
  border-radius: var(--radius-lg);
  box-shadow: var(--glass-shadow-wet);
  padding: var(--spacing-lg);
  position: relative;
  overflow: hidden;
}

.glass::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.15), transparent);
  pointer-events: none;
}

.section-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
  padding-bottom: var(--spacing-sm);
  border-bottom: 1px solid var(--glass-border);
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--spacing-sm);
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md);
  background: var(--primary-light);
  border: 1px solid transparent;
  border-radius: var(--radius);
  color: var(--primary);
  text-decoration: none;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all var(--transition);
}

.action-btn:hover {
  background: var(--glass-hover);
  border-color: var(--primary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.today-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.today-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) 0;
}

.today-dot {
  width: 10px;
  height: 10px;
  border-radius: var(--radius-full);
  flex-shrink: 0;
}

.today-dot.info { background: var(--info); }
.today-dot.success { background: var(--success); }
.today-dot.error { background: var(--error); }

.today-content {
  display: flex;
  justify-content: space-between;
  flex: 1;
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--radius-sm);
}

.today-text {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.today-value {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-primary);
}

.text-info { color: var(--info); }
.text-success { color: var(--success); }
.text-error { color: var(--error); }

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .dashboard-grid {
    grid-template-columns: 1fr;
  }

  .action-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .admin-header {
    flex-direction: column;
    gap: var(--spacing-sm);
  }
}

.loading-skeleton {
  padding: var(--spacing-lg) 0;
}

.stat-icon-skeleton {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-md);
  background: linear-gradient(90deg, var(--skeleton-base) 25%, var(--skeleton-highlight) 50%, var(--skeleton-base) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-value {
  width: 60px;
  height: 28px;
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

.error-state {
  text-align: center;
  padding: var(--spacing-3xl);
  color: var(--text-secondary);
}

.error-state h3 {
  margin: var(--spacing-md) 0 var(--spacing-lg);
  font-size: 1rem;
}
</style>
