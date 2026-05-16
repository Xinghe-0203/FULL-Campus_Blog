<template>
  <div class="report-page">
    <div class="report-container">
      <div class="report-card card">
        <h2 class="card-title">举报内容</h2>
        
        <form class="report-form" @submit.prevent="handleSubmit">
          <div class="form-group">
            <label class="form-label">举报原因</label>
            <select v-model="form.reason" class="form-input" required>
              <option value="">请选择举报原因</option>
              <option value="spam">垃圾广告</option>
              <option value="harassment">骚扰或霸凌</option>
              <option value="hate">仇恨言论</option>
              <option value="violence">暴力内容</option>
              <option value="adult">成人内容</option>
              <option value="copyright">版权侵犯</option>
              <option value="other">其他原因</option>
            </select>
          </div>
          
          <div class="form-group">
            <label class="form-label">详细说明（可选）</label>
            <textarea 
              v-model="form.description" 
              class="form-input"
              placeholder="请详细描述举报原因..."
              rows="4"
            ></textarea>
          </div>
          
          <div class="form-actions">
            <button type="button" class="btn btn-ghost" @click="goBack">取消</button>
            <button type="submit" class="btn btn-primary" :disabled="loading || !form.reason">
              {{ loading ? '提交中...' : '提交举报' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { reportApi } from '../../api/report'
import { useLogger } from '../../utils/logger'
import { toast } from '../../utils/toast'

const route = useRoute()
const router = useRouter()
const logger = useLogger('Report')

const loading = ref(false)
const form = reactive({
  reason: '',
  description: ''
})

const handleSubmit = async () => {
  if (!form.reason) return

  const validTypes = ['post', 'comment', 'user']
  if (!validTypes.includes(route.params.type)) {
    toast.error('无效的举报类型')
    return
  }
  const targetId = parseInt(route.params.id)
  if (!targetId || targetId <= 0) {
    toast.error('无效的举报目标')
    return
  }
  
  loading.value = true
  try {
    await reportApi.createReport({
      targetType: route.params.type,
      targetId,
      reason: form.reason,
      description: form.description
    })
    
    toast.success('举报已提交，感谢您的反馈')
    goBack()
  } catch (error) {
    logger.error('Failed to submit report', { error: error.message })
    toast.error('提交失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.go(-1)
}
</script>

<style scoped>
.report-page {
  max-width: 500px;
  margin: 0 auto;
  padding: var(--spacing-lg);
}

.report-card {
  padding: var(--spacing-xl);
}

.card-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: var(--spacing-xl);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-xl);
  padding-top: var(--spacing-lg);
  border-top: 1px solid var(--border);
}
</style>
