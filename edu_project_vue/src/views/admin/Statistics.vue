<template>
  <div class="admin-statistics">
    <div class="admin-container">
      <div class="page-header">
        <h1>数据统计</h1>
      </div>
      
      <div v-if="loading" class="loading-skeleton">
        <div class="stats-grid">
          <div v-for="i in 6" :key="i" class="stat-card card">
            <div class="sk-title"></div>
            <div class="stat-list">
              <div v-for="j in 3" :key="j" class="stat-item">
                <div class="sk-label"></div>
                <div class="sk-value"></div>
              </div>
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
          <h3>用户统计</h3>
          <div class="stat-list">
            <div class="stat-item">
              <span class="label">总用户数</span>
              <span class="value">{{ stats.userCount || stats.userStats?.totalUsers || 0 }}</span>
            </div>
            <div class="stat-item">
              <span class="label">今日新增</span>
              <span class="value">{{ stats.userStats?.todayNewUsers || 0 }}</span>
            </div>
            <div class="stat-item">
              <span class="label">活跃用户</span>
              <span class="value">{{ stats.userStats?.activeUsers || 0 }}</span>
            </div>
          </div>
        </div>
        
        <div class="stat-card card">
          <h3>文章统计</h3>
          <div class="stat-list">
            <div class="stat-item">
              <span class="label">总文章数</span>
              <span class="value">{{ stats.postCount || stats.postStats?.totalPosts || 0 }}</span>
            </div>
            <div class="stat-item">
              <span class="label">今日发布</span>
              <span class="value">{{ stats.postStats?.todayNewPosts || 0 }}</span>
            </div>
            <div class="stat-item">
              <span class="label">总评论数</span>
              <span class="value">{{ stats.commentCount || stats.postStats?.totalComments || 0 }}</span>
            </div>
          </div>
        </div>
        
        <div class="stat-card card">
          <h3>互动统计</h3>
          <div class="stat-list">
            <div class="stat-item">
              <span class="label">总点赞数</span>
              <span class="value">{{ stats.postStats?.totalLikes || 0 }}</span>
            </div>
            <div class="stat-item">
              <span class="label">总收藏数</span>
              <span class="value">{{ stats.postStats?.totalCollects || 0 }}</span>
            </div>
            <div class="stat-item">
              <span class="label">总关注数</span>
              <span class="value">{{ stats.engagementStats?.totalFollows || 0 }}</span>
            </div>
          </div>
        </div>
        
        <div class="stat-card card">
          <h3>内容审核</h3>
          <div class="stat-list">
            <div class="stat-item">
              <span class="label">待处理举报</span>
              <span class="value">{{ stats.reportStats?.pendingReports || 0 }}</span>
            </div>
            <div class="stat-item">
              <span class="label">本月处理举报</span>
              <span class="value">{{ stats.reportStats?.monthHandledReports || 0 }}</span>
            </div>
            <div class="stat-item">
              <span class="label">总举报数</span>
              <span class="value">{{ stats.reportStats?.totalReports || 0 }}</span>
            </div>
          </div>
        </div>

        <div class="stat-card card">
          <h3>校友圈统计</h3>
          <div class="stat-list">
            <div class="stat-item">
              <span class="label">总动态数</span>
              <span class="value">{{ stats.circleStats?.totalPosts || 0 }}</span>
            </div>
            <div class="stat-item">
              <span class="label">今日新增</span>
              <span class="value">{{ stats.circleStats?.todayNewPosts || 0 }}</span>
            </div>
            <div class="stat-item">
              <span class="label">总评论数</span>
              <span class="value">{{ stats.circleStats?.totalComments || 0 }}</span>
            </div>
            <div class="stat-item">
              <span class="label">总点赞数</span>
              <span class="value">{{ stats.circleStats?.totalLikes || 0 }}</span>
            </div>
            <div class="stat-item">
              <span class="label">总转发数</span>
              <span class="value">{{ stats.circleStats?.totalReposts || 0 }}</span>
            </div>
          </div>
        </div>

        <div class="stat-card card">
          <h3>标签统计</h3>
          <div class="stat-list">
            <div class="stat-item">
              <span class="label">总标签数</span>
              <span class="value">{{ stats.tagCount || stats.tagStats?.totalTags || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '../../api/admin'
import { useLogger } from '../../utils/logger'

const logger = useLogger('AdminStatistics')
const stats = ref({})
const loading = ref(true)
const error = ref('')

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
})
</script>

<style scoped>
.admin-statistics {
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

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-md);
}

.stat-card {
  padding: var(--spacing-lg);
}

.stat-card h3 {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
  padding-bottom: var(--spacing-sm);
  border-bottom: 1px solid var(--border);
}

.stat-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.label {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.value {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--primary);
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}

.loading-skeleton {
  padding: var(--spacing-lg) 0;
}

.sk-title {
  width: 80px;
  height: 16px;
  background: linear-gradient(90deg, #eee 25%, #f5f5f5 50%, #eee 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
  margin-bottom: var(--spacing-md);
}

.sk-label {
  width: 60px;
  height: 14px;
  background: linear-gradient(90deg, #eee 25%, #f5f5f5 50%, #eee 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
}

.sk-value {
  width: 50px;
  height: 20px;
  background: linear-gradient(90deg, #eee 25%, #f5f5f5 50%, #eee 75%);
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
  color: #666;
}

.error-state h3 {
  margin: 12px 0 16px;
  font-size: 16px;
}
</style>
