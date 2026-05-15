<template>
  <footer class="footer">
    <div class="footer-container">
      <div class="footer-content">
        <div class="footer-brand">
          <span class="brand-icon">📚</span>
          <span class="brand-text">校园博客</span>
        </div>
        <div class="footer-links">
          <router-link to="/">首页</router-link>
          <router-link to="/circle">校友圈</router-link>
          <router-link to="/trending">热搜</router-link>
        </div>
      </div>
      <div class="footer-bottom">
        <p>&copy; {{ currentYear }} 校园博客论坛. All rights reserved.</p>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { computed } from 'vue'

const currentYear = computed(() => new Date().getFullYear())
</script>

<style scoped>
.footer {
  background: var(--surface);
  border-top: 1px solid var(--border);
  padding: var(--spacing-lg) 0;
  margin-top: var(--spacing-2xl);
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--spacing-lg);
}

.footer-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-md);
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--text-primary);
}

.brand-icon {
  font-size: 1.25rem;
}

.footer-links {
  display: flex;
  gap: var(--spacing-lg);
}

.footer-links a {
  font-size: 0.875rem;
  color: var(--text-secondary);
  text-decoration: none;
  transition: color var(--transition);
}

.footer-links a:hover {
  color: var(--primary);
}

.footer-bottom {
  text-align: center;
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--border);
}

.footer-bottom p {
  font-size: 0.75rem;
  color: var(--text-muted);
}

@media (max-width: 768px) {
  .footer-content {
    flex-direction: column;
    gap: var(--spacing-md);
  }
  
  .footer-links {
    flex-wrap: wrap;
    justify-content: center;
    gap: var(--spacing-md);
  }
}
</style>
