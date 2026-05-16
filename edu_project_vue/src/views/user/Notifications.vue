<template>
  <div class="notifications-page">
    <div class="notifications-container">
      <button class="back-btn" @click="router.back()">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
        返回
      </button>
      <div class="page-header">
        <h1>消息通知</h1>
        <button v-if="notifications.length > 0" class="btn btn-sm btn-ghost" @click="markAllAsRead">
          全部已读
        </button>
      </div>
      
      <div v-if="error" class="error-card card">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="error-icon"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
        <p>{{ error }}</p>
        <button class="btn btn-primary" @click="fetchNotifications">重新加载</button>
      </div>
      
      <div v-if="loading && notifications.length === 0" class="skeleton-list">
        <div v-for="n in 3" :key="n" class="skeleton-card-item">
          <div class="skeleton skeleton-card-title"></div>
          <div class="skeleton skeleton-card-text"></div>
          <div class="skeleton skeleton-card-meta"></div>
        </div>
      </div>
      
      <div v-else-if="notifications.length > 0" class="notification-list">
        <div 
          v-for="notification in notifications" 
          :key="notification.id"
          class="notification-item card"
          :class="{ unread: !notification.isRead }"
          @click="handleNotification(notification)"
        >
          <img :src="notification.fromUser?.avatar || '/default-avatar.png'" :alt="notification.fromUser?.nickname || notification.fromUser?.username" class="sender-avatar" />
          <div class="notification-body">
            <p class="notification-content">
              <strong>{{ notification.fromUser?.nickname || notification.fromUser?.username }}</strong>
              {{ getNotificationText(notification.type) }}
              <span v-if="notification.content" class="target-title">「{{ notification.content }}」</span>
            </p>
            <span class="notification-time">{{ notification.timeAgo || formatRelativeTime(notification.createTime) }}</span>
          </div>
          <button class="delete-btn" @click.stop="deleteNotification(notification.id)">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
        <div v-if="hasMore" class="load-more">
          <button class="btn btn-ghost" @click="loadMore" :disabled="loading">
            {{ loading ? '加载中...' : '加载更多' }}
          </button>
        </div>
      </div>
      
      <div v-else class="empty-state card">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" class="empty-icon"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
        <p class="empty-title">暂无通知</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { notificationApi } from '../../api/notification'
import { commentApi } from '../../api/comment'
import { formatRelativeTime } from '../../utils'
import { useLogger } from '../../utils/logger'

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

const fetchNotifications = async (silent = false) => {
  if (!silent) loading.value = true
  error.value = ''
  try {
    const response = await notificationApi.getNotifications({ pageNum: currentPage.value, pageSize: pageSize })
    const pageData = response.data || {}
    if (currentPage.value === 1) {
      notifications.value = pageData.records || []
    } else {
      // 加载更多时追加
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

      // 只追加真正的新通知，不替换已有的（避免丢失已加载的历史分页）
      const existingIds = new Set(notifications.value.map(n => n.id))
      const trulyNew = newRecords.filter(n => !existingIds.has(n.id))
      if (trulyNew.length > 0) {
        notifications.value = [...trulyNew, ...notifications.value]
      }
      // 不重置 currentPage —— 保持用户当前翻页位置
      totalPages.value = pageData.pages || 1
      hasMore.value = currentPage.value < totalPages.value
    } catch (err) {
      // 静默失败
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
  
  // 跳转到相关页面
  if (notification.type === 'LIKE' || notification.type === 'COMMENT') {
    router.push(`/post/${notification.targetId}`)
  } else if (notification.type === 'FOLLOW' && notification.fromUser?.id) {
    router.push(`/user/${notification.fromUser.id}`)
  } else if (notification.type === 'MESSAGE') {
    router.push('/messages')
  } else if (notification.type === 'REPLY' || notification.type === 'MENTION') {
    // 回复和提及跳转到评论所在文章
    if (notification.targetType === 'COMMENT' && notification.targetId) {
      try {
        const res = await commentApi.getCommentById(notification.targetId)
        const postId = res.data?.postId
        if (postId) router.push(`/post/${postId}`)
        else router.push('/')
      } catch {
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
  } catch (error) {
    logger.error('Failed to mark all as read', { error: error.message })
  }
}

const deleteNotification = async (id) => {
  try {
    await notificationApi.deleteNotification(id)
    notifications.value = notifications.value.filter(n => n.id !== id)
  } catch (error) {
    logger.error('Failed to delete notification', { error: error.message })
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
  max-width: 700px;
  margin: 0 auto;
  padding: var(--spacing-lg);
}
.back-btn { display: flex; align-items: center; gap: 4px; padding: 8px 12px; background: transparent; border: 1px solid var(--border); border-radius: 8px; color: var(--text-secondary); cursor: pointer; font-size: 0.875rem; transition: all 0.2s; width: fit-content; margin-bottom: 16px; }
.back-btn:hover { background: var(--border); color: var(--text-primary); }

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
}

.page-header h1 {
  font-size: 1.5rem;
  font-weight: 700;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.notification-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
  cursor: pointer;
  transition: background var(--transition);
}

.notification-item:hover {
  background: var(--background);
}

.notification-item.unread {
  background: var(--primary-light);
}

.sender-avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  object-fit: cover;
}

.notification-body {
  flex: 1;
}

.notification-content {
  font-size: 0.875rem;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.target-title {
  color: var(--primary);
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
  padding: 4px;
  border-radius: var(--radius);
  opacity: 0;
  transition: all var(--transition);
}

.notification-item:hover .delete-btn {
  opacity: 1;
}

.delete-btn:hover {
  color: var(--error);
  background: rgba(239, 68, 68, 0.1);
}

.error-card {
  text-align: center;
  padding: 60px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}
.error-card p { color: var(--text-secondary); font-size: 0.875rem; }
.error-icon { color: var(--text-muted); opacity: 0.5; }

.empty-state {
  text-align: center;
  padding: var(--spacing-2xl);
}
.empty-icon { color: var(--text-muted); opacity: 0.3; margin-bottom: 16px; }
.empty-title { font-size: 1rem; font-weight: 600; color: var(--text-primary); margin-bottom: 4px; }

.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.skeleton-card-item {
  padding: 16px;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
}
.skeleton-card-title { width: 60%; height: 18px; margin-bottom: 8px; }
.skeleton-card-text { width: 90%; height: 14px; margin-bottom: 6px; }
.skeleton-card-meta { width: 180px; height: 14px; margin-top: 4px; }

.load-more {
  text-align: center;
  padding: var(--spacing-lg);
}

@media (max-width: 768px) {
  .notifications-page { padding: 16px; }
  .notification-item { padding: 12px; gap: 10px; }
  .sender-avatar { width: 36px; height: 36px; }
}
</style>
