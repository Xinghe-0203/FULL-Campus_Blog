<template>
  <div class="notifications-page">
    <div class="notifications-container">
      <button class="back-btn glass" @click="router.back()">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
        返回
      </button>
      <div class="page-header">
        <div class="header-left">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="header-icon"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
          <div>
            <h1>消息通知</h1>
            <p class="header-subtitle">{{ unreadCount > 0 ? `你有 ${unreadCount} 条未读通知` : '所有通知已读' }}</p>
          </div>
        </div>
        <div class="header-actions">
          <button v-if="notifications.length > 0" class="btn btn-sm btn-secondary" @click="markAllAsRead">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>
            全部已读
          </button>
        </div>
      </div>

      <div v-if="error" class="error-card glass">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="error-icon"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
        <p>{{ error }}</p>
        <button class="btn btn-primary" @click="fetchNotifications">重新加载</button>
      </div>

      <div v-if="loading && notifications.length === 0" class="skeleton-list">
        <div v-for="n in 4" :key="n" class="skeleton-card-item glass">
          <div class="skeleton-avatar"></div>
          <div class="skeleton-body">
            <div class="skeleton skeleton-line w-60"></div>
            <div class="skeleton skeleton-line w-40"></div>
          </div>
        </div>
      </div>

      <div v-else-if="notifications.length > 0" class="notification-list">
        <div
          v-for="notification in notifications"
          :key="notification.id"
          class="notification-card glass"
          :class="{ unread: !notification.isRead }"
          @click="handleNotification(notification)"
        >
          <div class="notification-icon" :class="getNotificationIconClass(notification.type)">
            <svg v-if="notification.type === 'LIKE'" width="18" height="18" viewBox="0 0 24 24" fill="currentColor" stroke="none"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
            <svg v-else-if="notification.type === 'COMMENT'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
            <svg v-else-if="notification.type === 'REPLY'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 17 4 12 9 7"/><path d="M20 18v-2a4 4 0 0 0-4-4H4"/></svg>
            <svg v-else-if="notification.type === 'FOLLOW'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/></svg>
            <svg v-else-if="notification.type === 'MENTION'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M16 8h-6a2 2 0 1 0 0 4h4a2 2 0 1 1 0 4H8"/><path d="M12 18V6"/></svg>
            <svg v-else-if="notification.type === 'SYSTEM'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
            <svg v-else-if="notification.type === 'MESSAGE'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
            <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
          </div>
          <img :src="notification.fromUser?.avatar || '/default-avatar.png'" :alt="notification.fromUser?.nickname || notification.fromUser?.username" class="sender-avatar" @error="onAvatarError" />
          <div class="notification-body">
            <p class="notification-content">
              <strong>{{ notification.fromUser?.nickname || notification.fromUser?.username }}</strong>
              {{ getNotificationText(notification.type) }}
              <span v-if="notification.content" class="target-title">「{{ notification.content }}」</span>
            </p>
            <span class="notification-time">{{ notification.timeAgo || formatRelativeTime(notification.createTime) }}</span>
          </div>
          <button class="delete-btn" @click.stop="deleteNotification(notification.id)" title="删除">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="3 6 5 6 21 6"/>
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
            </svg>
          </button>
          <div class="unread-indicator" v-if="!notification.isRead"></div>
        </div>
        <div v-if="hasMore" class="load-more">
          <button class="btn btn-secondary" @click="loadMore" :disabled="loading">
            <svg v-if="loading" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="spin"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>
            {{ loading ? '加载中...' : '加载更多' }}
          </button>
        </div>
      </div>

      <div v-else class="empty-state glass">
        <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" class="empty-icon"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
        <p class="empty-title">暂无通知</p>
        <p class="empty-text">当有人与你互动时，通知会出现在这里</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { notificationApi } from '../../api/notification'
import { commentApi } from '../../api/comment'
import { formatRelativeTime } from '../../utils'
import { useLogger } from '../../utils/logger'
import { toast } from '../../utils/toast'

const router = useRouter()
const logger = useLogger('Notifications')
const notifications = ref([])
const loading = ref(false)
const error = ref('')
const currentPage = ref(1)
const totalPages = ref(1)
const hasMore = ref(false)
const pageSize = 20
let pollingInterval = null

const defaultAvatar = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="44" height="44" viewBox="0 0 44 44"><rect width="44" height="44" rx="22" fill="#e0e0e0"/><text x="22" y="28" text-anchor="middle" fill="#999" font-size="18" font-family="sans-serif">?</text></svg>')

const onAvatarError = (e) => {
  if (e.target.src !== defaultAvatar) {
    e.target.src = defaultAvatar
  }
}

const unreadCount = computed(() => notifications.value.filter(n => !n.isRead).length)

const fetchNotifications = async (silent = false) => {
  if (!silent) loading.value = true
  error.value = ''
  try {
    const response = await notificationApi.getNotifications({ pageNum: currentPage.value, pageSize: pageSize })
    const pageData = response.data || {}
    if (currentPage.value === 1) {
      notifications.value = pageData.records || []
    } else {
      notifications.value = [...notifications.value, ...(pageData.records || [])]
    }
    totalPages.value = pageData.pages || 1
    hasMore.value = currentPage.value < totalPages.value
  } catch (err) {
    logger.error('Failed to fetch notifications', { error: err.message })
    if (!silent) error.value = '加载通知失败'
  } finally {
    if (!silent) loading.value = false
  }
}

const loadMore = () => {
  if (currentPage.value < totalPages.value && !loading.value) {
    currentPage.value++
    fetchNotifications()
  }
}

const startPolling = () => {
  pollingInterval = setInterval(async () => {
    try {
      const response = await notificationApi.getNotifications({ pageNum: 1, pageSize: pageSize })
      const pageData = response.data || {}
      const newRecords = pageData.records || []

      const existingIds = new Set(notifications.value.map(n => n.id))
      const trulyNew = newRecords.filter(n => !existingIds.has(n.id))
      if (trulyNew.length > 0) {
        notifications.value = [...trulyNew, ...notifications.value]
      }
      totalPages.value = pageData.pages || 1
      hasMore.value = currentPage.value < totalPages.value
    } catch (err) {
      // silent
    }
  }, 30000)
}

const getNotificationText = (type) => {
  const texts = {
    LIKE: '赞了你的文章',
    COMMENT: '评论了你的文章',
    REPLY: '回复了你的评论',
    FOLLOW: '关注了你',
    MENTION: '在评论中提到了你',
    SYSTEM: '系统通知',
    MESSAGE: '给你发了私信'
  }
  return texts[type] || '有新通知'
}

const getNotificationIconClass = (type) => {
  const classes = {
    LIKE: 'icon-like',
    COMMENT: 'icon-comment',
    REPLY: 'icon-reply',
    FOLLOW: 'icon-follow',
    MENTION: 'icon-mention',
    SYSTEM: 'icon-system',
    MESSAGE: 'icon-message'
  }
  return classes[type] || 'icon-default'
}

const stopPolling = () => {
  if (pollingInterval) {
    clearInterval(pollingInterval)
    pollingInterval = null
  }
}

const handleNotification = async (notification) => {
  if (!notification.isRead) {
    try {
      await notificationApi.markAsRead(notification.id)
      notification.isRead = true
    } catch (error) {
      logger.error('Failed to mark as read', { error: error.message })
    }
  }

  if (notification.type === 'LIKE' || notification.type === 'COMMENT') {
    router.push(`/post/${notification.targetId}`)
  } else if (notification.type === 'FOLLOW' && notification.fromUser?.id) {
    router.push(`/user/${notification.fromUser.id}`)
  } else if (notification.type === 'MESSAGE') {
    router.push('/messages?userId=' + notification.fromUser?.id)
  } else if (notification.type === 'REPLY' || notification.type === 'MENTION') {
    if (notification.targetType === 'COMMENT' && notification.targetId) {
      try {
        const res = await commentApi.getCommentById(notification.targetId)
        const postId = res.data?.postId
        if (postId) router.push(`/post/${postId}`)
        else router.push('/')
      } catch {
        toast.info('该评论已被删除')
        router.push('/')
      }
    } else if (notification.targetId) {
      router.push(`/post/${notification.targetId}`)
    }
  }
}

const markAllAsRead = async () => {
  try {
    await notificationApi.markAllAsRead()
    notifications.value.forEach(n => n.isRead = true)
    toast.success('已全部标记为已读')
  } catch (error) {
    logger.error('Failed to mark all as read', { error: error.message })
    toast.error('操作失败')
  }
}

const deleteNotification = async (id) => {
  try {
    await notificationApi.deleteNotification(id)
    notifications.value = notifications.value.filter(n => n.id !== id)
    toast.success('通知已删除')
  } catch (error) {
    logger.error('Failed to delete notification', { error: error.message })
    toast.error('删除失败')
  }
}

onMounted(() => {
  fetchNotifications()
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.notifications-page {
  max-width: 900px;
  margin: 0 auto;
  padding: var(--spacing-lg);
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius);
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 0.875rem;
  transition: all var(--transition);
  box-shadow: var(--glass-shadow);
  margin-bottom: var(--spacing-md);
}

.back-btn:hover {
  background: var(--glass-hover);
  color: var(--primary);
  border-color: var(--primary);
  transform: translateY(-1px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
  gap: var(--spacing-md);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.header-icon {
  color: var(--primary);
  flex-shrink: 0;
}

.page-header h1 {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
}

.header-subtitle {
  font-size: 0.875rem;
  color: var(--text-muted);
  margin: 2px 0 0;
}

.header-actions {
  flex-shrink: 0;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.notification-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md) var(--spacing-lg);
  cursor: pointer;
  transition: all var(--transition);
  position: relative;
  overflow: hidden;
  border-radius: var(--radius-lg);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border-wet);
  box-shadow: var(--glass-shadow-wet);
}

.notification-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.15), transparent);
  pointer-events: none;
}

.notification-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md), var(--glass-shadow-wet);
}

.notification-card.unread {
  background: linear-gradient(135deg, var(--primary-light), rgba(139, 92, 246, 0.05));
  border-color: rgba(99, 102, 241, 0.2);
}

.notification-card.unread::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: linear-gradient(180deg, var(--primary-start), var(--primary-end));
}

.notification-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--radius);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all var(--transition);
}

.icon-like {
  background: var(--accent-light);
  color: var(--accent);
}

.icon-comment {
  background: var(--info-light);
  color: var(--info);
}

.icon-reply {
  background: var(--success-light);
  color: var(--success);
}

.icon-follow {
  background: var(--purple-light);
  color: var(--purple);
}

.icon-mention {
  background: var(--warning-light);
  color: var(--warning);
}

.icon-system {
  background: var(--primary-light);
  color: var(--primary);
}

.icon-message {
  background: var(--blue-light);
  color: var(--blue);
}

.icon-default {
  background: var(--bg-secondary);
  color: var(--text-muted);
}

.sender-avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  object-fit: cover;
  border: 2px solid var(--surface-solid);
  box-shadow: var(--shadow-sm);
  flex-shrink: 0;
}

.notification-body {
  flex: 1;
  min-width: 0;
}

.notification-content {
  font-size: 0.875rem;
  color: var(--text-primary);
  margin-bottom: 4px;
  line-height: 1.5;
}

.target-title {
  color: var(--primary);
  font-weight: 500;
}

.notification-time {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.delete-btn {
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 6px;
  border-radius: var(--radius-sm);
  opacity: 0;
  transition: all var(--transition);
  flex-shrink: 0;
}

.notification-card:hover .delete-btn {
  opacity: 1;
}

.delete-btn:hover {
  color: var(--error);
  background: var(--error-light);
}

.unread-indicator {
  width: 8px;
  height: 8px;
  border-radius: var(--radius-full);
  background: var(--primary);
  flex-shrink: 0;
  box-shadow: 0 0 6px var(--primary-glow);
}

.error-card {
  text-align: center;
  padding: var(--spacing-3xl) var(--spacing-xl);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-md);
  border-radius: var(--radius-lg);
}

.error-card p {
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.error-icon {
  color: var(--error);
  opacity: 0.6;
}

.empty-state {
  text-align: center;
  padding: var(--spacing-3xl) var(--spacing-xl);
  border-radius: var(--radius-lg);
}

.empty-icon {
  color: var(--text-muted);
  opacity: 0.3;
  margin-bottom: var(--spacing-md);
}

.empty-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.empty-text {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.skeleton-card-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md) var(--spacing-lg);
  border-radius: var(--radius-lg);
}

.skeleton-avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  background: linear-gradient(90deg, var(--skeleton-base) 25%, var(--skeleton-highlight) 50%, var(--skeleton-base) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  flex-shrink: 0;
}

.skeleton-body {
  flex: 1;
}

.skeleton-line {
  height: 14px;
  border-radius: var(--radius-xs);
  background: linear-gradient(90deg, var(--skeleton-base) 25%, var(--skeleton-highlight) 50%, var(--skeleton-base) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-line + .skeleton-line {
  margin-top: 8px;
}

.w-60 { width: 60%; }
.w-40 { width: 40%; }

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.load-more {
  text-align: center;
  padding: var(--spacing-lg);
}

@media (max-width: 768px) {
  .notifications-page { padding: var(--spacing-md); }
  .notification-card { padding: var(--spacing-md); gap: var(--spacing-sm); }
  .sender-avatar { width: 36px; height: 36px; }
  .notification-icon { width: 32px; height: 32px; }
  .page-header { flex-direction: column; align-items: flex-start; }
  .header-actions { align-self: flex-end; }
}
</style>
