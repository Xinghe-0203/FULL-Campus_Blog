<template>
  <div class="my-reports-page">
    <button class="back-btn" @click="router.back()">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
      返回
    </button>
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
    <div v-else-if="error" class="error-card card">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="error-icon"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
      <p>{{ error }}</p>
      <button class="btn btn-primary" @click="fetchReports">重新加载</button>
    </div>
    <div v-else-if="reports.length === 0" class="empty-state card">
      <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" class="empty-icon"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/><circle cx="12" cy="16" r="1"/><circle cx="12" cy="12" r="1"/><circle cx="12" cy="8" r="1"/></svg>
      <p class="empty-title">还没有举报记录</p>
    </div>
    <div v-else class="report-list">
      <div v-for="report in reports" :key="report.id" class="report-item card">
        <div class="report-header">
          <span class="report-type">{{ report.targetType === 'post' ? '文章' : report.targetType === 'comment' ? '评论' : report.targetType === 'user' ? '用户' : report.targetType }}</span>
          <span class="report-status" :class="report.status === 0 ? 'pending' : report.status === 2 ? 'resolved' : 'rejected'">
            {{ report.status === 0 ? '待处理' : report.status === 2 ? '已处理' : '已驳回' }}
          </span>
        </div>
        <p class="report-reason">{{ report.reason }}</p>
        <span class="report-time">{{ formatRelativeTime(report.createTime) }}</span>
      </div>
      <div v-if="totalPages > 1" class="pagination-section">
        <div class="pagination">
          <button class="pagination-btn" :disabled="page <= 1" @click="page--; fetchReports()">上一页</button>
          <button v-for="p in totalPages" :key="p" class="pagination-btn" :class="{ active: p === page }" @click="page = p; fetchReports()">{{ p }}</button>
          <button class="pagination-btn" :disabled="page >= totalPages" @click="page++; fetchReports()">下一页</button>
        </div>
        <span class="pagination-info">共 {{ total }} 条</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { reportApi } from '../../api/report'
import { formatRelativeTime } from '../../utils'
import { useLogger } from '../../utils/logger'

const router = useRouter()
const logger = useLogger('MyReports')
const reports = ref([])
const loading = ref(false)
const error = ref('')
const page = ref(1)
const total = ref(0)
const totalPages = ref(1)
const pageSize = 10

const fetchReports = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await reportApi.getMyReports({ pageNum: page.value, pageSize })
    const data = response.data || {}
    reports.value = data.records || []
    total.value = data.total || 0
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
.back-btn { display: flex; align-items: center; gap: 4px; padding: 8px 12px; background: transparent; border: 1px solid var(--border); border-radius: 8px; color: var(--text-secondary); cursor: pointer; font-size: 0.875rem; transition: all 0.2s; width: fit-content; margin-bottom: 16px; }
.back-btn:hover { background: var(--border); color: var(--text-primary); }
.page-header { margin-bottom: 24px; }
.page-header h1 { font-size: 1.5rem; font-weight: 700; }
.report-item { padding: 16px; margin-bottom: 12px; }
.report-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.report-type { font-size: 0.75rem; background: var(--primary-light); color: var(--primary); padding: 2px 8px; border-radius: 4px; }
.report-status { font-size: 0.75rem; padding: 2px 8px; border-radius: 4px; }
.report-status.pending { background: var(--warning-light); color: var(--warning); }
.report-status.resolved { background: var(--success-light); color: var(--success); }
.report-status.rejected { background: var(--error-light); color: var(--error); }
.report-reason { font-size: 0.9375rem; font-weight: 500; margin-bottom: 4px; }
.report-time { font-size: 0.75rem; color: var(--text-muted); }
.error-card { text-align: center; padding: 60px 24px; display: flex; flex-direction: column; align-items: center; gap: 16px; }
.error-card p { color: var(--text-secondary); font-size: 0.875rem; }
.empty-state { padding: 80px 24px; text-align: center; }
.empty-icon { color: var(--text-muted); opacity: 0.3; margin-bottom: 16px; }
.empty-title { font-size: 1rem; font-weight: 600; color: var(--text-primary); margin-bottom: 4px; }
.skeleton-list { display: flex; flex-direction: column; gap: 12px; }
.skeleton-card-item { padding: 16px; border: 1px solid var(--border); border-radius: 8px; }
.skeleton-card-title { width: 60%; height: 18px; margin-bottom: 8px; }
.skeleton-card-text { width: 90%; height: 14px; margin-bottom: 6px; }
.skeleton-card-meta { width: 120px; height: 14px; }
.pagination-section { display: flex; align-items: center; justify-content: center; gap: 16px; padding-top: 20px; }
.pagination-info { font-size: 0.75rem; color: var(--text-muted); }
@media (max-width: 768px) {
  .my-reports-page { padding: 16px; }
  .report-item { padding: 14px; }
}
</style>
