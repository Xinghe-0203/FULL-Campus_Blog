<template>
  <footer class="footer">
    <div class="footer-container">
      <div class="footer-top">
        <div class="footer-brand">
          <span class="brand-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="var(--primary)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
              <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
              <path d="M10 2v8l3-2 3 2V2"/>
            </svg>
          </span>
          <span class="brand-text">校园博客</span>
          <span class="brand-tagline">分享你的校园故事</span>
        </div>
        <div class="footer-links">
          <div class="footer-link-group">
            <h4 class="footer-link-title">发现</h4>
            <router-link to="/" class="footer-link">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
                <polyline points="9 22 9 12 15 12 15 22"/>
              </svg>
              首页
            </router-link>
            <router-link to="/trending" class="footer-link">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="22 7 13.5 15.5 8.5 10.5 2 17"/>
                <polyline points="16 7 22 7 22 13"/>
              </svg>
              热搜
            </router-link>
          </div>
          <div class="footer-link-group">
            <h4 class="footer-link-title">社区</h4>
            <router-link to="/circle" class="footer-link">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
              </svg>
              校友圈
            </router-link>
            <router-link to="/post-edit" class="footer-link">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="12" y1="5" x2="12" y2="19"/>
                <line x1="5" y1="12" x2="19" y2="12"/>
              </svg>
              写文章
            </router-link>
          </div>
          <div class="footer-link-group">
            <h4 class="footer-link-title">账户</h4>
            <router-link to="/profile" class="footer-link">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              个人主页
            </router-link>
            <router-link to="/collections" class="footer-link">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/>
              </svg>
              我的收藏
            </router-link>
          </div>
        </div>
      </div>
      <div class="footer-bottom">
        <p class="footer-copyright">&copy; {{ currentYear }} 校园博客论坛. All rights reserved.</p>
        <div class="footer-meta">
          <span class="footer-version">v2.0</span>
        </div>
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
  backdrop-filter: blur(16px) saturate(1.6);
  -webkit-backdrop-filter: blur(16px) saturate(1.6);
  border-top: 1px solid var(--glass-border);
  padding: 0;
  margin-top: var(--spacing-3xl);
}

.footer-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 var(--spacing-lg);
}

/* ==================== Footer Top ==================== */
.footer-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--spacing-2xl);
  padding: var(--spacing-2xl) 0;
}

.footer-brand {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
  flex-shrink: 0;
}

.footer-brand .brand-icon {
  display: flex;
  align-items: center;
  transition: transform var(--transition-spring);
}

.footer-brand:hover .brand-icon {
  transform: rotate(-5deg) scale(1.05);
}

.footer-brand .brand-text {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
  background: linear-gradient(135deg, var(--primary-start), var(--primary-end));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.footer-brand .brand-tagline {
  font-size: 0.8125rem;
  color: var(--text-muted);
  margin-top: var(--spacing-xs);
}

/* ==================== Footer Links ==================== */
.footer-links {
  display: flex;
  gap: var(--spacing-3xl);
}

.footer-link-group {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.footer-link-title {
  font-size: 0.6875rem;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  margin-bottom: var(--spacing-xs);
  padding: 0 var(--spacing-sm);
}

.footer-link {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--text-secondary);
  text-decoration: none;
  padding: 0.375rem var(--spacing-sm);
  border-radius: var(--radius-sm);
  transition: all var(--transition);
}

.footer-link svg {
  opacity: 0.5;
  transition: opacity var(--transition);
}

.footer-link:hover {
  color: var(--primary);
  background: var(--primary-light);
}

.footer-link:hover svg {
  opacity: 1;
}

/* ==================== Footer Bottom ==================== */
.footer-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md) 0;
  border-top: 1px solid var(--glass-border);
}

.footer-copyright {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.footer-meta {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.footer-version {
  font-size: 0.6875rem;
  font-weight: 600;
  color: var(--text-muted);
  background: var(--glass-bg);
  padding: 2px 8px;
  border-radius: var(--radius-full);
  border: 1px solid var(--glass-border);
  letter-spacing: 0.02em;
}

/* ==================== Responsive ==================== */
@media (max-width: 768px) {
  .footer {
    margin-top: var(--spacing-2xl);
  }

  .footer-top {
    flex-direction: column;
    gap: var(--spacing-xl);
    padding: var(--spacing-xl) 0;
  }

  .footer-brand {
    align-items: center;
    text-align: center;
  }

  .footer-links {
    flex-wrap: wrap;
    justify-content: center;
    gap: var(--spacing-xl);
  }

  .footer-link-group {
    align-items: center;
  }

  .footer-link-title {
    text-align: center;
  }
}

@media (max-width: 480px) {
  .footer-container {
    padding: 0 var(--spacing-md);
  }

  .footer-brand .brand-text {
    font-size: 1.125rem;
  }

  .footer-links {
    gap: var(--spacing-lg);
  }

  .footer-bottom {
    flex-direction: column;
    gap: var(--spacing-sm);
    text-align: center;
  }
}
</style>
