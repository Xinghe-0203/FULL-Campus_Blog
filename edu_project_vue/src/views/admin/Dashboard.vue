<template>
  <div class="admin-dashboard">
    <header class="dashboard-header">
      <div class="header-title">
        <h1>仪表盘</h1>
        <span class="header-sub">校园博客论坛管理系统</span>
      </div>
      <div class="header-meta">
        <span class="auto-refresh" :class="{ active: loading }">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="23 4 23 10 17 10"/>
            <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
          </svg>
          {{ loading ? '刷新中' : '60s 自动刷新' }}
        </span>
      </div>
    </header>

    <!-- Loading skeleton -->
    <div v-if="loading" class="stats-grid">
      <div v-for="i in 8" :key="i" class="stat-card skeleton-card">
        <div class="skel-icon"></div>
        <div class="skel-text">
          <div class="skel-line skel-num"></div>
          <div class="skel-line skel-label"></div>
        </div>
      </div>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="error-block">
      <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
      </svg>
      <p>{{ error }}</p>
      <button class="btn btn-sm btn-primary" @click="fetchStats">重试</button>
    </div>

    <!-- Content -->
    <template v-else>
      <!-- Stats cards -->
      <div class="stats-grid">
        <div class="stat-card" v-for="card in statCards" :key="card.label">
          <div class="stat-icon" :style="{ background: card.bg, color: card.fg }">
            <component :is="card.icon" />
          </div>
          <div class="stat-body">
            <span class="stat-value">{{ formatNumber(card.value) }}</span>
            <span class="stat-label">{{ card.label }}</span>
          </div>
        </div>
      </div>

      <!-- Bottom panels -->
      <div class="panels-grid">
        <!-- Quick actions -->
        <section class="panel">
          <h3 class="panel-title">快捷操作</h3>
          <div class="actions-row">
            <router-link v-for="action in quickActions" :key="action.label" :to="action.to" class="action-chip">
              <component :is="action.icon" />
              <span>{{ action.label }}</span>
            </router-link>
          </div>
        </section>

        <!-- Today overview -->
        <section class="panel">
          <h3 class="panel-title">今日概览</h3>
          <div class="today-rows">
            <div v-for="item in todayItems" :key="item.label" class="today-row">
              <span class="today-dot" :style="{ background: item.color }"></span>
              <span class="today-label">{{ item.label }}</span>
              <span class="today-value" :style="{ color: item.color }">{{ item.value }}</span>
            </div>
          </div>
        </section>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, h, onMounted, onUnmounted } from 'vue'
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

// Inline SVG icon components
const IconUsers = () => h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
  h('path', { d: 'M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2' }),
  h('circle', { cx: 9, cy: 7, r: 4 })
])

const IconPosts = () => h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
  h('path', { d: 'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z' }),
  h('polyline', { points: '14 2 14 8 20 8' })
])

const IconCircle = () => h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
  h('path', { d: 'M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z' })
])

const IconComment = () => h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
  h('path', { d: 'M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z' }),
  h('line', { x1: 12, y1: 7, x2: 12, y2: 13 }),
  h('line', { x1: 12, y1: 17, x2: 12.01, y2: 17 })
])

const IconTag = () => h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
  h('path', { d: 'M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z' }),
  h('line', { x1: 7, y1: 7, x2: 7.01, y2: 7 })
])

const IconTopic = () => h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
  h('circle', { cx: 12, cy: 12, r: 10 }),
  h('path', { d: 'M2 12h20' }),
  h('path', { d: 'M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z' })
])

const IconAlert = () => h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
  h('path', { d: 'M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z' })
])

const IconActiveUser = () => h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
  h('path', { d: 'M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2' }),
  h('circle', { cx: 9, cy: 7, r: 4 }),
  h('path', { d: 'M23 21v-2a4 4 0 0 0-3-3.87' }),
  h('path', { d: 'M16 3.13a4 4 0 0 1 0 7.75' })
])

const statCards = computed(() => [
  { label: '用户总数', value: stats.value.userCount || 0, icon: IconUsers, bg: 'rgba(13, 148, 136, 0.1)', fg: '#0D9488' },
  { label: '文章总数', value: stats.value.postCount || 0, icon: IconPosts, bg: 'rgba(5, 150, 105, 0.1)', fg: '#059669' },
  { label: '校友圈动态', value: stats.value.circlePostCount || 0, icon: IconCircle, bg: 'rgba(139, 92, 246, 0.1)', fg: '#8B5CF6' },
  { label: '评论总数', value: stats.value.commentCount || 0, icon: IconComment, bg: 'rgba(217, 119, 6, 0.1)', fg: '#D97706' },
  { label: '标签总数', value: stats.value.tagCount || 0, icon: IconTag, bg: 'rgba(37, 99, 235, 0.1)', fg: '#2563EB' },
  { label: '话题总数', value: stats.value.topicCount || 0, icon: IconTopic, bg: 'rgba(249, 115, 22, 0.1)', fg: '#F97316' },
  { label: '待处理举报', value: stats.value.pendingReportCount || 0, icon: IconAlert, bg: 'rgba(220, 38, 38, 0.1)', fg: '#DC2626' },
  { label: '活跃用户', value: stats.value.userStats?.activeUsers || 0, icon: IconActiveUser, bg: 'rgba(5, 150, 105, 0.1)', fg: '#059669' },
])

const quickActions = [
  { label: '用户管理', to: '/admin/users', icon: IconUsers },
  { label: '文章管理', to: '/admin/posts', icon: IconPosts },
  { label: '校友圈', to: '/admin/circle', icon: IconCircle },
  { label: '标签管理', to: '/admin/tags', icon: IconTag },
  { label: '话题管理', to: '/admin/topics', icon: IconTopic },
  { label: '举报管理', to: '/admin/reports', icon: IconAlert },
]

const todayItems = computed(() => [
  { label: '今日新增用户', value: `+${stats.value.userStats?.todayNewUsers || 0}`, color: '#2563EB' },
  { label: '今日新增文章', value: `+${stats.value.postStats?.todayNewPosts || 0}`, color: '#059669' },
  { label: '今日新增动态', value: `+${stats.value.circleStats?.todayNewPosts || 0}`, color: '#8B5CF6' },
  { label: '待处理举报', value: `${stats.value.pendingReportCount || 0}`, color: '#DC2626' },
])
</script>

<style scoped>
.admin-dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

/* Header */
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: var(--spacing-xl);
  padding-bottom: var(--spacing-md);
  border-bottom: 1px solid var(--border-solid);
}

.header-title h1 {
  font-family: var(--font-sans);
  font-size: 1.375rem;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: var(--tracking-tight);
  line-height: 1.2;
}

.header-sub {
  font-size: var(--text-xs);
  color: var(--text-muted);
  letter-spacing: var(--tracking-wide);
  text-transform: uppercase;
  margin-top: 2px;
  display: block;
}

.auto-refresh {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: var(--text-xs);
  color: var(--text-muted);
  padding: 4px 10px;
  border-radius: var(--radius-full);
  border: 1px solid var(--border-solid);
  background: var(--surface-solid);
}

.auto-refresh.active svg {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Stats grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1px;
  background: var(--border-solid);
  border: 1px solid var(--border-solid);
  border-radius: var(--radius-md);
  overflow: hidden;
  margin-bottom: var(--spacing-xl);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-lg) var(--spacing-lg);
  background: var(--surface-solid);
  transition: background var(--duration-fast) var(--ease-default);
}

.stat-card:hover {
  background: var(--gray-50);
}

.stat-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius);
  flex-shrink: 0;
}

.stat-body {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.1;
  font-variant-numeric: tabular-nums;
  letter-spacing: var(--tracking-tight);
}

.stat-label {
  font-size: var(--text-xs);
  color: var(--text-muted);
  margin-top: 2px;
}

/* Skeleton */
.skeleton-card {
  pointer-events: none;
}

.skel-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius);
  background: var(--gray-200);
  flex-shrink: 0;
  animation: pulse 1.5s ease-in-out infinite;
}

.skel-text {
  flex: 1;
}

.skel-line {
  border-radius: var(--radius-xs);
  background: var(--gray-200);
  animation: pulse 1.5s ease-in-out infinite;
}

.skel-num {
  width: 50%;
  height: 22px;
  margin-bottom: 6px;
}

.skel-label {
  width: 65%;
  height: 12px;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

/* Panels */
.panels-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-xl);
}

.panel {
  background: var(--surface-solid);
  border: 1px solid var(--border-solid);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
}

.panel-title {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
  padding-bottom: var(--spacing-sm);
  border-bottom: 1px solid var(--border-solid);
  letter-spacing: var(--tracking-wide);
  text-transform: uppercase;
}

/* Actions */
.actions-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
}

.action-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--text-secondary);
  background: var(--gray-50);
  border: 1px solid var(--border-solid);
  border-radius: var(--radius);
  text-decoration: none;
  transition: all var(--duration-fast) var(--ease-default);
}

.action-chip:hover {
  color: var(--primary);
  border-color: var(--primary);
  background: var(--primary-light);
}

/* Today overview */
.today-rows {
  display: flex;
  flex-direction: column;
}

.today-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: 10px 0;
  border-bottom: 1px solid var(--gray-100);
}

.today-row:last-child {
  border-bottom: none;
}

.today-dot {
  width: 6px;
  height: 6px;
  border-radius: var(--radius-full);
  flex-shrink: 0;
}

.today-label {
  flex: 1;
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.today-value {
  font-size: var(--text-sm);
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

/* Error */
.error-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-3xl);
  text-align: center;
  color: var(--text-muted);
}

.error-block p {
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

/* Responsive */
@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .panels-grid {
    grid-template-columns: 1fr;
  }

  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-sm);
  }

  .stat-card {
    padding: var(--spacing-md);
  }

  .stat-value {
    font-size: 1.25rem;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .actions-row {
    gap: 6px;
  }

  .action-chip {
    padding: 6px 10px;
    font-size: var(--text-xs);
  }
}
</style>
