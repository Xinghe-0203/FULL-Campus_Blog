<template>
  <Teleport to="body">
    <div class="toast-container">
      <TransitionGroup name="toast" tag="div">
        <div
          v-for="toast in toasts"
          :key="toast.id"
          class="toast"
          :class="[`toast-${toast.type}`]"
          @mouseenter="pauseToast(toast.id)"
          @mouseleave="resumeToast(toast.id)"
        >
          <svg v-if="toast.type === 'success'" class="toast-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
            <polyline points="22 4 12 14.01 9 11.01"/>
          </svg>
          <svg v-else-if="toast.type === 'error'" class="toast-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <line x1="15" y1="9" x2="9" y2="15"/>
            <line x1="9" y1="9" x2="15" y2="15"/>
          </svg>
          <svg v-else-if="toast.type === 'warning'" class="toast-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
            <line x1="12" y1="9" x2="12" y2="13"/>
            <line x1="12" y1="17" x2="12.01" y2="17"/>
          </svg>
          <svg v-else class="toast-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="16" x2="12" y2="12"/>
            <line x1="12" y1="8" x2="12.01" y2="8"/>
          </svg>
          <span class="toast-message">{{ toast.message }}</span>
          <button v-if="toast.action" class="toast-action" @click.stop="handleAction(toast)">{{ toast.action.text }}</button>
          <button class="toast-close" @click="removeToast(toast.id)">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
          <div class="toast-progress" :style="{ width: (progressStyles[toast.id] ?? 100) + '%' }"></div>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { toasts, removeToast, pauseToast, resumeToast } from '../../utils/toast'

const progressStyles = ref({})

const handleAction = (toast) => {
  toast.action?.handler?.()
  removeToast(toast.id)
}

let animFrame = null
function updateProgress() {
  const styles = {}
  const now = Date.now()
  toasts.value.forEach(t => {
    if (t.paused) {
      styles[t.id] = t._progress ?? 100
    } else {
      const activeMs = t._activeMs + (now - t.createdAt)
      const total = t._originalDuration || t.duration || 3000
      const pct = total > 0 ? Math.max(0, (1 - activeMs / total) * 100) : 100
      styles[t.id] = pct
      t._progress = pct
    }
  })
  progressStyles.value = styles
  if (toasts.value.length > 0) {
    animFrame = requestAnimationFrame(updateProgress)
  }
}

watch(() => toasts.value.length, (len) => {
  if (len > 0 && !animFrame) {
    animFrame = requestAnimationFrame(updateProgress)
  }
})

onMounted(() => {
  animFrame = requestAnimationFrame(updateProgress)
})

onBeforeUnmount(() => {
  if (animFrame) cancelAnimationFrame(animFrame)
})
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 80px;
  right: 20px;
  z-index: 11000;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.toast {
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: var(--surface);
  border-radius: var(--radius);
  box-shadow: var(--shadow-lg);
  min-width: 280px;
  max-width: 400px;
}

.toast-success {
  border-left: 4px solid var(--success);
}

.toast-error {
  border-left: 4px solid var(--error);
}

.toast-warning {
  border-left: 4px solid var(--warning);
}

.toast-info {
  border-left: 4px solid var(--info);
}

.toast-icon {
  flex-shrink: 0;
}

.toast-success .toast-icon {
  color: var(--success);
}

.toast-error .toast-icon {
  color: var(--error);
}

.toast-warning .toast-icon {
  color: var(--warning);
}

.toast-info .toast-icon {
  color: var(--info);
}

.toast-message {
  flex: 1;
  font-size: 0.875rem;
  color: var(--text-primary);
}

.toast-action {
  flex-shrink: 0;
  padding: 4px 12px;
  font-size: 0.8rem;
  font-weight: 500;
  background: var(--primary-light);
  color: var(--primary);
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all var(--transition);
}

.toast-action:hover {
  background: var(--primary);
  color: white;
}

.toast-close {
  flex-shrink: 0;
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 4px;
  border-radius: var(--radius);
  transition: all var(--transition);
}

.toast-close:hover {
  background: var(--background);
  color: var(--text-primary);
}

.toast-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 3px;
  background: currentColor;
  opacity: 0.25;
  transition: width 0.1s linear;
}

.toast-success .toast-progress {
  background: var(--success);
}

.toast-error .toast-progress {
  background: var(--error);
}

.toast-warning .toast-progress {
  background: var(--warning);
}

.toast-info .toast-progress {
  background: var(--info);
}

.toast:hover .toast-progress {
  opacity: 0.4;
}

/* 过渡动画 */
.toast-enter-active {
  animation: slideIn 0.3s ease;
}

.toast-leave-active {
  animation: slideOut 0.3s ease;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@keyframes slideOut {
  from {
    transform: translateX(0);
    opacity: 1;
  }
  to {
    transform: translateX(100%);
    opacity: 0;
  }
}

@media (max-width: 768px) {
  .toast-container {
    top: auto;
    bottom: 20px;
    left: 20px;
    right: 20px;
  }

  .toast {
    min-width: auto;
    max-width: none;
  }
}
</style>
