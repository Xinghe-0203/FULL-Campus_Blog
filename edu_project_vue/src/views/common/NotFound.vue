<template>
  <div class="not-found-page">
    <div class="not-found-container">
      <div class="not-found-content">
        <h1 class="error-code">404</h1>
        <h2 class="error-title">页面不存在</h2>
        <p class="error-text">抱歉，您访问的页面不存在或已被移除</p>
        <div class="error-actions">
          <router-link to="/" class="btn btn-primary">
            返回首页
          </router-link>
          <button class="btn btn-secondary" @click="goBack">
            返回上一页
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.go(-1)
}
</script>

<style scoped>
.not-found-page {
  min-height: calc(100vh - 60px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-lg);
}

.not-found-container {
  text-align: center;
}

.error-code {
  font-size: 8rem;
  font-weight: 900;
  background: linear-gradient(135deg, var(--primary), var(--accent));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
  margin-bottom: var(--spacing-md);
}

.error-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-sm);
}

.error-text {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-bottom: var(--spacing-xl);
}

.error-actions {
  display: flex;
  gap: var(--spacing-md);
  justify-content: center;
}
</style>
