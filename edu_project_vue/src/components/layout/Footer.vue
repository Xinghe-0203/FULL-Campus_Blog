<template>
  <footer class="footer">
    <div class="footer-container">
      <div class="footer-content">
        <div class="footer-brand">
          <span class="brand-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="var(--primary)" stroke-width="2">
              <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
              <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
              <path d="M10 2v8l3-2 3 2V2"/>
            </svg>
          </span>
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

<script setup lang="ts">
import { computed } from 'vue'

const currentYear = computed(() => new Date().getFullYear())
</script>

<style scoped>
.footer {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border-top: 1px solid var(--glass-border);
  padding: var(--spacing-xl) 0;
  margin-top: var(--spacing-2xl);
  box-shadow: var(--glass-shadow);
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
  display: flex;
  align-items: center;
  transition: transform var(--transition-spring);
}

.footer-brand:hover .brand-icon {
  transform: rotate(-5deg) scale(1.1);
}

.footer-links {
  display: flex;
  gap: var(--spacing-lg);
}

.footer-links a {
  font-size: 0.875rem;
  color: var(--text-secondary);
  text-decoration: none;
  transition: all var(--transition);
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--radius-sm);
}

.footer-links a:hover {
  color: var(--primary);
  background: var(--primary-light);
  transform: translateY(-1px);
}

.footer-bottom {
  text-align: center;
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--glass-border);
}

.footer-bottom p {
  font-size: 0.75rem;
  color: var(--text-muted);
}

@media (max-width: 768px) {
  .footer {
    padding: var(--spacing-lg) 0;
  }

  .footer-content {
    flex-direction: column;
    gap: var(--spacing-md);
    text-align: center;
  }
  
  .footer-links {
    flex-wrap: wrap;
    justify-content: center;
    gap: var(--spacing-sm);
  }

  .footer-links a {
    padding: var(--spacing-xs) var(--spacing-sm);
  }
}

@media (max-width: 480px) {
  .footer-container {
    padding: 0 var(--spacing-md);
  }

  .footer-brand {
    font-size: 1rem;
  }
}
</style>
