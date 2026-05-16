import { ref, reactive } from 'vue'

const toasts = ref([])
let toastId = 0
const timers = new Map()

export function addToast(message, type = 'info', duration = 3000, action = null) {
  const id = ++toastId
  const item = reactive({
    id,
    message,
    type,
    duration,
    remaining: duration,
    paused: false,
    action,
    createdAt: Date.now(),
    _originalDuration: duration,
    _activeMs: 0
  })

  toasts.value.push(item)

  if (duration > 0) {
    const timer = setTimeout(() => removeToast(id), duration)
    timers.set(id, timer)
  }

  return id
}

export function removeToast(id) {
  const timer = timers.get(id)
  if (timer) {
    clearTimeout(timer)
    timers.delete(id)
  }
  const index = toasts.value.findIndex(t => t.id === id)
  if (index > -1) {
    toasts.value.splice(index, 1)
  }
}

export function pauseToast(id) {
  const item = toasts.value.find(t => t.id === id)
  if (!item || item.paused) return
  item.paused = true
  const timer = timers.get(id)
  if (timer) {
    clearTimeout(timer)
    timers.delete(id)
  }
  const elapsed = Date.now() - item.createdAt
  item._activeMs += elapsed
  item.remaining = Math.max(0, item.remaining - elapsed)
}

export function resumeToast(id) {
  const item = toasts.value.find(t => t.id === id)
  if (!item || !item.paused) return
  item.paused = false
  if (item.remaining <= 0) {
    removeToast(id)
    return
  }
  item.createdAt = Date.now()
  const timer = setTimeout(() => removeToast(id), item.remaining)
  timers.set(id, timer)
}

export const toast = {
  success: (message, duration, action) => addToast(message, 'success', duration, action),
  error: (message, duration, action) => addToast(message, 'error', duration, action),
  warning: (message, duration, action) => addToast(message, 'warning', duration, action),
  info: (message, duration, action) => addToast(message, 'info', duration, action)
}

export { toasts }
