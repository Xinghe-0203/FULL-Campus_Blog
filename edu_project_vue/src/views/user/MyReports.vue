<template>
  <div class="my-reports-page">
    <div class="page-header">
      <h1>我的举报</h1>
    </div>

    <div v-if="loading" class="skeleton-list">
      <div v-for="n in 3" :key="n" class="skeleton-card-item">
        <div class="skeleton skeleton-card-title"></div>
        <div class="skeleton skeleton-card-text"></div>
        <div class="skeleton skeleton-card-meta"></div>
      </div>
    </div>
    <div v-else-if="error" class="error-state">
      <p>{{ error }}</p>
      <button class="btn btn-primary" @click="fetchReports">重试</button>
    </div>
    <div v-else-if="reports.length === 0" class="empty-state">
      <p>还没有举报记录</p>
    </div>
    <div v-else class="report-list">
      <div v-for="report in reports" :key="report.id" class="report-item card">
        <div class="report-header">
          <span class="report-type">{{ report.targetType === 'POST' ? '文章' : report.targetType === 'COMMENT' ? '评论' : report.targetType === 'USER' ? '用户' : report.targetType }}</span>
          <span class="report-status" :class="report.status === 0 ? 'pending' : report.status === 1 ? 'resolved' : 'rejected'">
            {{ report.status === 0 ? '待处理' : report.status === 1 ? '已处理' : '已驳回' }}
          </span>
        </div>
        <p class="report-reason">{{ report.reason }}</p>
        <p v-if="report.description" class="report-desc">{{ report.description }}</p>
        <span class="report-time">{{ formatRelativeTime(report.createTime) }}</span>
      </div>
      <div v-if="totalPages > 1" class="pagination">
        <button class="pagination-btn" :disabled="page <= 1" @click="page--; fetchReports()">上一页</button>
        <span class="page-info">{{ page }} / {{ totalPages }}</span>
        <button class="pagination-btn" :disabled="page >= totalPages" @click="page++; fetchReports()">下一页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { reportApi } from '../../api/report'
import { formatRelativeTime } from '../../utils'
import { useLogger } from '../../utils/logger'

const logger = useLogger('MyReports')
const reports = ref([])
const loading = ref(false)
const error = ref('')
const page = ref(1)
const totalPages = ref(1)
const pageSize = 10

const fetchReports = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await reportApi.getMyReports({ pageNum: page.value, pageSize })
    const data = response.data || {}
    reports.value = data.records || []
    totalPages.value = data.pages || 1
  } catch (err) {
    logger.error('fetch reports error', { error: err.message })
    error.value = '加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchReports())
</script>

<style scoped>
.my-reports-page { max-width: 700px; margin: 0 auto; padding: 24px; }
.page-header { margin-bottom: 24px; }
.page-header h1 { font-size: 1.5rem; font-weight: 700; }
.report-item { padding: 16px; margin-bottom: 12px; }
.report-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.report-type { font-size: 0.75rem; background: var(--primary-light, #e8f0fe); color: var(--primary, #1a73e8); padding: 2px 8px; border-radius: 4px; }
.report-status { font-size: 0.75rem; padding: 2px 8px; border-radius: 4px; }
.report-status.pending { background: #fff3e0; color: #e65100; }
.report-status.resolved { background: #e8f5e9; color: #2e7d32; }
.report-status.rejected { background: #fce4ec; color: #c62828; }
.report-reason { font-size: 0.9375rem; font-weight: 500; margin-bottom: 4px; }
.report-desc { font-size: 0.8125rem; color: var(--text-secondary, #666); margin-bottom: 8px; }
.report-time { font-size: 0.75rem; color: var(--text-muted, #999); }
.error-state { text-align: center; padding: 60px 20px; display: flex; flex-direction: column; align-items: center; gap: 12px; }
.empty-state { text-align: center; padding: 60px 20px; color: var(--text-muted, #999); }
.pagination { display: flex; align-items: center; justify-content: center; gap: 16px; padding-top: 20px; }
.page-info { font-size: 0.875rem; color: var(--text-secondary, #666); }
.skeleton-list { display: flex; flex-direction: column; gap: 12px; }
.skeleton-card-item { padding: 16px; border: 1px solid var(--border, #eee); border-radius: 8px; }
.skeleton { background: linear-gradient(90deg, var(--skeleton-base, #eee) 25%, var(--skeleton-highlight, #f5f5f5) 50%, var(--skeleton-base, #eee) 75%); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: 4px; }
.skeleton-card-title { width: 60%; height: 18px; margin-bottom: 8px; }
.skeleton-card-text { width: 90%; height: 14px; margin-bottom: 6px; }
.skeleton-card-meta { width: 120px; height: 14px; }
@keyframes shimmer { 0% { background-position: -200px 0; } 100% { background-position: calc(200px + 100%) 0; } }
</style>
