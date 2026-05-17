<template>
  <div class="empty-state glass">
    <svg v-if="!icon" class="empty-state-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
      <circle cx="12" cy="7" r="4"/>
    </svg>
    <img v-else :src="icon" class="empty-state-icon" alt="" />
    <h3 class="empty-state-title">{{ title }}</h3>
    <p v-if="message" class="empty-state-text">{{ message }}</p>
    <button v-if="actionText" class="btn btn-primary empty-state-action" @click="$emit('action')">
      {{ actionText }}
    </button>
    <slot />
  </div>
</template>

<script setup>
defineProps({
  icon: { type: String, default: '' },
  title: { type: String, default: '暂无数据' },
  message: { type: String, default: '' },
  actionText: { type: String, default: '' }
})
defineEmits(['action'])
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-3xl) var(--spacing-xl);
  text-align: center;
  border-radius: var(--radius-xl);
  transition: all var(--transition-slow);
}

.empty-state-icon {
  width: 80px;
  height: 80px;
  margin-bottom: var(--spacing-md);
  color: var(--text-muted);
  opacity: 0.5;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.empty-state-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-sm);
}

.empty-state-text {
  font-size: 0.875rem;
  color: var(--text-secondary);
  max-width: 320px;
  line-height: 1.6;
  margin-bottom: var(--spacing-md);
}

.empty-state-action {
  margin-top: var(--spacing-sm);
  transition: all var(--transition-spring);
}

.empty-state-action:hover {
  transform: translateY(-2px) scale(1.02);
}

@media (max-width: 640px) {
  .empty-state {
    padding: var(--spacing-2xl) var(--spacing-md);
  }

  .empty-state-icon {
    width: 64px;
    height: 64px;
  }

  .empty-state-title {
    font-size: 1rem;
  }
}
</style>
