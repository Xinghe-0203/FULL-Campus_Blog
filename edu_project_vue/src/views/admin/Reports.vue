<template>
  <div class="admin-reports">
    <div class="admin-container">
      <div class="page-header">
        <h1>举报管理</h1>
      </div>
      
      <div v-if="loading" class="loading-skeleton">
        <div class="table-container card">
          <table class="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>举报类型</th>
                <th>举报原因</th>
                <th>举报人</th>
                <th>状态</th>
                <th>举报时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="i in 5" :key="i">
                <td><div class="sk-line w-30"></div></td>
                <td><div class="sk-line w-50"></div></td>
                <td><div class="sk-line w-60"></div></td>
                <td><div class="sk-line w-50"></div></td>
                <td><div class="sk-line w-40"></div></td>
                <td><div class="sk-line w-60"></div></td>
                <td><div class="sk-line w-70"></div></td>
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
        <button class="btn btn-primary" @click="fetchReports">重试</button>
      </div>
      
      <div v-else class="table-container card">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>举报类型</th>
              <th>举报原因</th>
              <th>举报人</th>
              <th>状态</th>
              <th>举报时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="reports.length === 0">
              <td colspan="7" class="empty-cell">暂无举报</td>
            </tr>
            <tr v-for="report in reports" :key="report.id">
              <td>{{ report.id }}</td>
              <td>{{ report.targetType }}</td>
              <td>{{ report.reason }}</td>
              <td>{{ report.reporterName }}</td>
              <td>
                <span class="status" :class="report.status === 0 ? 'pending' : 'handled'">
                  {{ report.status === 0 ? '待处理' : '已处理' }}
                </span>
              </td>
              <td>{{ formatDate(report.createTime) }}</td>
              <td>
                <div class="actions">
                  <button 
                    v-if="report.status === 0"
                    class="btn btn-sm btn-primary"
                    @click="handleReport(report, 2)"
                  >
                    处理
                  </button>
                  <button 
                    v-if="report.status === 0"
                    class="btn btn-sm btn-ghost"
                    @click="handleReport(report, 1)"
                  >
                    驳回
                  </button>
                  <button 
                    class="btn btn-sm btn-ghost"
                    @click="viewDetail(report)"
                  >
                    查看
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

    <!-- 举报详情弹窗 -->
    <div v-if="detailDialog" class="dialog-overlay" @click.self="detailDialog = false">
      <div class="dialog card">
        <div class="dialog-header">
          <h3>举报详情 #{{ detailData?.id }}</h3>
          <button class="dialog-close" @click="detailDialog = false">×</button>
        </div>
        <div class="dialog-body" v-if="detailData">
          <div class="detail-row">
            <span class="detail-label">举报类型</span>
            <span class="detail-value">{{ detailData.targetType }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">举报原因</span>
            <span class="detail-value">{{ detailData.reason }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">详细说明</span>
            <span class="detail-value">{{ detailData.description || '无' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">举报人</span>
            <span class="detail-value">{{ detailData.reporterName }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">被举报内容ID</span>
            <span class="detail-value">{{ detailData.targetId }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">状态</span>
            <span class="detail-value">{{ detailData.status === 0 ? '待处理' : '已处理' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">举报时间</span>
            <span class="detail-value">{{ formatDate(detailData.createTime) }}</span>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-ghost" @click="detailDialog = false">关闭</button>
        </div>
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

const logger = useLogger('AdminReports')
const { confirm, ConfirmDialog } = useConfirm()

const reports = ref([])
const currentPage = ref(1)
const totalPages = ref(1)
const detailDialog = ref(false)
const detailData = ref(null)
const loading = ref(true)
const error = ref('')

const fetchReports = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await adminApi.getPendingReports({
      pageNum: currentPage.value,
      pageSize: 20
    })
    reports.value = response.data?.records || []
    totalPages.value = response.data?.pages || 1
  } catch (err) {
    logger.error('Failed to fetch reports', { error: err.message })
    error.value = '加载失败，请重试'
    reports.value = []
  } finally {
    loading.value = false
  }
}

const handleReport = async (report, status) => {
  // status: 1 = 驳回(rejected), 2 = 处理(resolved)
  const action = status === 2 ? '处理' : '驳回'
  const ok = await confirm(`确定${action}该举报吗？`, `${action}举报`)
  if (!ok) return
  
  try {
    await adminApi.handleReport(report.id, { status })
    report.status = status
    toast.success(`已${action}`)
  } catch (err) {
    logger.error('Failed to handle report', { error: err.message })
    toast.error('操作失败')
  }
}

const viewDetail = async (report) => {
  try {
    const response = await adminApi.getReportById(report.id)
    detailData.value = response.data || report
    detailDialog.value = true
  } catch (err) {
    logger.error('Failed to fetch report detail', { error: err.message })
    toast.error('获取详情失败')
  }
}

const changePage = (page) => {
  currentPage.value = page
  fetchReports()
}

onMounted(() => {
  fetchReports()
})
</script>

<style scoped>
.admin-reports {
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

.status {
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  border-radius: var(--radius);
}

.status.pending {
  background: rgba(245, 158, 11, 0.1);
  color: var(--warning);
}

.status.handled {
  background: rgba(16, 185, 129, 0.1);
  color: var(--success);
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

.dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog {
  width: 500px;
  max-width: 90vw;
  max-height: 80vh;
  overflow-y: auto;
  padding: var(--spacing-lg);
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
}

.dialog-header h3 {
  font-size: 1.125rem;
  font-weight: 600;
}

.dialog-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: var(--text-muted);
}

.dialog-close:hover {
  color: var(--text-primary);
}

.detail-row {
  display: flex;
  padding: var(--spacing-sm) 0;
  border-bottom: 1px solid var(--border);
}

.detail-label {
  width: 120px;
  font-weight: 500;
  color: var(--text-secondary);
  flex-shrink: 0;
}

.detail-value {
  flex: 1;
  color: var(--text-primary);
}

.dialog-footer {
  margin-top: var(--spacing-lg);
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--border);
  text-align: right;
}

.loading-skeleton {
  padding: var(--spacing-lg) 0;
}

.sk-line {
  height: 14px;
  border-radius: 4px;
  background: linear-gradient(90deg, #eee 25%, #f5f5f5 50%, #eee 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.sk-line.w-30 { width: 30%; }
.sk-line.w-40 { width: 40%; }
.sk-line.w-50 { width: 50%; }
.sk-line.w-60 { width: 60%; }
.sk-line.w-70 { width: 70%; }

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
