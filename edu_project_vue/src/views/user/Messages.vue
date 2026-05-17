<template>
  <div class="messages-page">
    <button class="floating-back-btn" @click="goBack">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
    </button>
    <div class="messages-container">
      <div class="conversation-list glass-rain water-drops" :class="{ show: showMobileList }">
        <div class="list-header">
          <div class="list-header-left">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="list-icon"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
            <h2>私信</h2>
          </div>
          <button class="list-close-btn" @click="showMobileList = false" v-if="activeConversation">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
        <div class="list-content">
          <div v-if="loadingConversations" class="empty-list">
            <div class="loading-spinner"></div>
            <p>加载中...</p>
          </div>
          <div v-else-if="conversationsError" class="empty-list">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="empty-icon"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            <p class="error-text">{{ conversationsError }}</p>
            <button class="btn btn-sm btn-secondary" @click="fetchConversations()">重试</button>
          </div>
          <div v-else-if="conversations.length > 0">
            <div
              v-for="conv in conversations"
              :key="conv.conversationId || `virtual-${conv.user?.id}`"
              class="conversation-item"
              :class="{ active: activeConversation?.conversationId != null ? activeConversation.conversationId === conv.conversationId : activeConversation?.user?.id === conv.user?.id }"
              @click="selectConversation(conv)"
            >
              <div class="avatar-wrapper">
                <img :src="conv.user?.avatar || defaultAvatar" :alt="conv.user?.nickname || conv.user?.username" class="conv-avatar" @error="onAvatarError" />
                <span class="online-dot" :class="{ online: conv.user?.online }"></span>
              </div>
              <div class="conv-info">
                <div class="conv-name-row">
                  <span class="conv-name">{{ conv.user?.nickname || conv.user?.username }}</span>
                  <span v-if="conv.unreadCount > 0" class="conv-badge">{{ conv.unreadCount > 99 ? '99+' : conv.unreadCount }}</span>
                </div>
                <span class="conv-last-message">{{ conv.lastMessage || '暂无消息' }}</span>
              </div>
              <span class="conv-time">{{ formatRelativeTime(conv.lastMessageTime) }}</span>
            </div>
          </div>
          <div v-else class="empty-list">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" class="empty-icon"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
            <p class="empty-title">暂无私信</p>
            <p class="empty-text">开始与校园好友对话吧</p>
          </div>
        </div>
      </div>

      <div class="message-area glass-rain water-drops">
        <template v-if="activeConversation">
          <div class="area-header">
            <div class="header-user">
              <div class="avatar-wrapper">
                <img :src="activeConversation.user?.avatar || defaultAvatar" :alt="activeConversation.user?.nickname" class="header-avatar" @error="onAvatarError" />
                <span class="online-dot" :class="{ online: activeConversation.user?.online }"></span>
              </div>
              <div class="header-info">
                <h3>{{ activeConversation.user?.nickname || activeConversation.user?.username }}</h3>
                <span class="header-status">{{ activeConversation.user?.online ? '在线' : '离线' }}</span>
              </div>
            </div>
          </div>
          <div v-if="messagesError" class="empty-list">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="empty-icon"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            <p class="error-text">{{ messagesError }}</p>
            <button class="btn btn-sm btn-secondary" @click="selectConversation(activeConversation)">重试</button>
          </div>
          <div v-else-if="loadingMessages" class="empty-list">
            <div class="loading-spinner"></div>
            <p>加载消息中...</p>
          </div>
          <div v-else class="message-list" ref="messageList">
            <div
              v-for="(msg, idx) in messages"
              :key="msg.id"
              class="message-item"
              :class="{ mine: String(msg.sender?.id) === String(userStore.userId) }"
            >
              <img :src="msg.sender?.avatar || defaultAvatar" :alt="msg.sender?.nickname" class="msg-avatar" @error="onAvatarError" />
              <div class="msg-bubble">
                <p class="msg-text">{{ msg.content }}</p>
                <span class="msg-time">{{ formatMessageTime(msg.createTime) }}</span>
              </div>
            </div>
          </div>
          <div class="message-input">
            <input
              v-model="newMessage"
              type="text"
              maxlength="1000"
              placeholder="输入消息..."
              @keyup.enter="sendMessage"
              class="form-input"
            />
            <button class="btn btn-primary btn-sm send-btn" @click="sendMessage" :disabled="!newMessage.trim()">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
              发送
            </button>
          </div>
        </template>
        <div v-else class="no-conversation">
          <div class="no-conversation-inner">
            <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" class="empty-icon"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
            <p class="empty-title">选择一个会话</p>
            <p class="empty-text">从左侧列表选择好友开始聊天</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { messageApi } from '../../api/message'
import { userApi } from '../../api/user'
import { formatRelativeTime } from '../../utils'
import { useLogger } from '../../utils/logger'
import { toast } from '../../utils/toast'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const logger = useLogger('Messages')

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}

const defaultAvatar = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="44" height="44" viewBox="0 0 44 44"><rect width="44" height="44" rx="22" fill="#e0e0e0"/><text x="22" y="28" text-anchor="middle" fill="#999" font-size="18" font-family="sans-serif">?</text></svg>')

const onAvatarError = (e) => {
  if (e.target.src !== defaultAvatar) {
    e.target.src = defaultAvatar
  }
}

const formatMessageTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const isToday = date.toDateString() === now.toDateString()
  if (isToday) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' }) + ' ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const showMobileList = ref(true)
const conversations = ref([])
const activeConversation = ref(null)
const messages = ref([])
const newMessage = ref('')
const messageList = ref(null)
const loadingConversations = ref(false)
const loadingMessages = ref(false)
const conversationsError = ref('')
const messagesError = ref('')
let pollingInterval = null

const fetchConversations = async (silent = false) => {
  if (!silent) loadingConversations.value = true
  conversationsError.value = ''
  try {
    const response = await messageApi.getConversations()
    const newConversations = response.data || []

    conversations.value = newConversations

    if (!silent && route.query.userId && !activeConversation.value) {
      const targetUserId = parseInt(route.query.userId)
      const conv = newConversations.find(c => c.user?.id === targetUserId)
      if (conv) {
        await selectConversation(conv)
      } else {
        try {
          const userRes = await userApi.getUserById(targetUserId)
          const targetUser = userRes.data || userRes
          if (targetUser) {
            const virtualConv = {
              conversationId: null,
              user: {
                id: targetUser.id,
                username: targetUser.username,
                nickname: targetUser.nickname,
                avatar: targetUser.avatar
              },
              lastMessage: '',
              lastMessageTime: null,
              unreadCount: 0
            }
            await selectConversation(virtualConv)
          }
        } catch (e) {
          logger.error('Failed to start conversation with user', { error: e.message, userId: targetUserId })
          toast.error('无法开始对话，用户不存在')
        }
      }
    }
  } catch (error) {
    logger.error('Failed to fetch conversations', { error: error.message })
    if (!silent) conversationsError.value = '加载会话失败，请稍后重试'
  } finally {
    if (!silent) loadingConversations.value = false
  }
}

const selectConversation = async (conv) => {
  showMobileList.value = false
  activeConversation.value = conv
  messagesError.value = ''
  messages.value = []

  if (conv.conversationId == null) return

  loadingMessages.value = true
  try {
    const response = await messageApi.getConversationMessages(conv.conversationId)
    messages.value = response.data?.records || []

    if (conv.unreadCount > 0) {
      conv.unreadCount = 0
      try {
        await messageApi.markConversationAsRead(conv.conversationId)
      } catch (e) {
        logger.warn('Failed to mark conversation as read', { error: e.message })
      }
    }

    await scrollToBottom()
  } catch (error) {
    logger.error('Failed to fetch messages', { error: error.message })
    messagesError.value = '加载消息失败'
  } finally {
    loadingMessages.value = false
  }
}

const sendMessage = async () => {
  const content = newMessage.value.trim()
  if (!content || !activeConversation.value) return
  if (content.length > 1000) {
    toast.warning('消息内容不能超过1000个字符')
    return
  }

  const receiverId = activeConversation.value.user?.id
  if (!receiverId) return

  try {
    await messageApi.sendMessage({ receiverId, content })

    if (activeConversation.value.conversationId == null) {
      await fetchConversations(true)
      const realConv = conversations.value.find(c => c.user?.id === receiverId)
      if (realConv) activeConversation.value = realConv
    }

    messages.value.push({
      id: Date.now(),
      sender: { id: userStore.userId, avatar: userStore.avatar, nickname: userStore.nickname || userStore.username },
      content,
      createTime: new Date().toISOString().slice(0, 19),
      isRead: 1
    })

    await scrollToBottom()
  } catch (error) {
    logger.error('Failed to send message', { error: error.message })
    toast.error('发送失败')
  } finally {
    newMessage.value = ''
  }
}

const scrollToBottom = async () => {
  await nextTick()
  if (messageList.value) {
    messageList.value.scrollTop = messageList.value.scrollHeight
  }
}

const startPolling = () => {
  pollingInterval = setInterval(async () => {
    await fetchConversations(true)
    if (activeConversation.value && activeConversation.value.conversationId != null) {
      try {
        const response = await messageApi.getConversationMessages(activeConversation.value.conversationId)
        const records = response.data?.records || []
        const wasAtBottom = messageList.value && (messageList.value.scrollTop + messageList.value.clientHeight >= messageList.value.scrollHeight - 50)
        messages.value = records
        if (wasAtBottom) {
          await scrollToBottom()
        }
      } catch (e) {
        logger.warn('Failed to refresh messages', { error: e.message })
      }
    }
  }, 30000)
}

const stopPolling = () => {
  if (pollingInterval) {
    clearInterval(pollingInterval)
    pollingInterval = null
  }
}

onMounted(() => {
  fetchConversations()
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.messages-page {
  height: calc(100vh - 80px);
  padding: var(--spacing-lg);
  display: flex;
  flex-direction: column;
  position: relative;
}

.floating-back-btn {
  position: fixed;
  left: 20px;
  top: 100px;
  z-index: 100;
  display: none;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border-wet);
  border-radius: var(--radius-full);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition);
  box-shadow: var(--glass-shadow-wet);
}

.floating-back-btn:hover {
  background: var(--glass-hover);
  color: var(--primary);
  border-color: var(--primary);
  transform: translateY(-2px);
  box-shadow: var(--glass-shadow-wet), var(--shadow-glow-primary);
}

.messages-container {
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: var(--spacing-md);
}

.conversation-list {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: var(--radius-lg);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border-wet);
  box-shadow: var(--glass-shadow-wet);
  transition: all var(--transition);
}

.list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md) var(--spacing-lg);
  border-bottom: 1px solid var(--glass-border);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
}

.list-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.list-icon {
  color: var(--primary);
}

.list-header h2 {
  font-size: 1.125rem;
  font-weight: 600;
}

.list-close-btn {
  display: none;
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 4px;
  border-radius: var(--radius-sm);
  transition: all var(--transition);
}

.list-close-btn:hover {
  background: var(--primary-light);
  color: var(--primary);
}

.list-content {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md) var(--spacing-lg);
  cursor: pointer;
  transition: all var(--transition);
  border-bottom: 1px solid rgba(226, 232, 240, 0.3);
}

.conversation-item:last-child {
  border-bottom: none;
}

.conversation-item:hover {
  background: var(--primary-light);
}

.conversation-item.active {
  background: var(--primary-light);
  border-left: 3px solid var(--primary);
}

.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.conv-avatar {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-full);
  object-fit: cover;
  border: 2px solid var(--surface-solid);
  box-shadow: var(--shadow-sm);
}

.online-dot {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 10px;
  height: 10px;
  border-radius: var(--radius-full);
  background: var(--text-muted);
  border: 2px solid var(--surface-solid);
  transition: all var(--transition);
}

.online-dot.online {
  background: var(--success);
  box-shadow: 0 0 6px var(--success-glow);
}

.conv-info {
  flex: 1;
  min-width: 0;
}

.conv-name-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 2px;
}

.conv-name {
  font-weight: 600;
  font-size: 0.875rem;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conv-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  font-size: 0.625rem;
  font-weight: 700;
  color: white;
  background: linear-gradient(135deg, var(--error), #DC2626);
  border-radius: var(--radius-full);
  box-shadow: 0 2px 4px rgba(239, 68, 68, 0.3);
}

.conv-last-message {
  display: block;
  font-size: 0.75rem;
  color: var(--text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conv-time {
  font-size: 0.625rem;
  color: var(--text-muted);
  flex-shrink: 0;
  align-self: flex-start;
  margin-top: 4px;
}

.empty-list {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: var(--spacing-3xl) var(--spacing-lg);
  color: var(--text-muted);
  gap: var(--spacing-sm);
}

.empty-icon {
  color: var(--text-muted);
  opacity: 0.4;
}

.empty-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
}

.empty-text {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.error-text {
  color: var(--error);
  font-size: 0.875rem;
}

.message-area {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: var(--radius-lg);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border-wet);
  box-shadow: var(--glass-shadow-wet);
  transition: all var(--transition);
}

.area-header {
  padding: var(--spacing-md) var(--spacing-lg);
  border-bottom: 1px solid var(--glass-border);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
}

.header-user {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.header-avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  object-fit: cover;
  border: 2px solid var(--surface-solid);
}

.header-info h3 {
  font-size: 1rem;
  font-weight: 600;
  margin: 0;
}

.header-status {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.header-status:has(+ .online-dot.online),
.header-status {
  color: var(--text-muted);
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-lg);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  min-height: 200px;
}

.message-item {
  display: flex;
  gap: var(--spacing-sm);
  max-width: 85%;
  animation: slideUp var(--duration-normal) ease;
}

.message-item.mine {
  margin-left: auto;
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  object-fit: cover;
  flex-shrink: 0;
  align-self: flex-end;
}

.msg-bubble {
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-md);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border-wet);
  box-shadow: var(--glass-shadow-wet);
  max-width: 100%;
}

.message-item.mine .msg-bubble {
  background: linear-gradient(135deg, var(--primary-start), var(--primary-end));
  border-color: transparent;
  color: var(--text-inverse);
  box-shadow: var(--glass-shadow-wet), var(--shadow-glow-primary);
}

.msg-text {
  font-size: 0.875rem;
  line-height: 1.5;
  word-break: break-word;
}

.msg-time {
  display: block;
  font-size: 0.625rem;
  color: var(--text-muted);
  margin-top: 4px;
  text-align: right;
}

.message-item.mine .msg-time {
  color: rgba(255, 255, 255, 0.7);
}

.message-input {
  display: flex;
  gap: var(--spacing-sm);
  padding: var(--spacing-md) var(--spacing-lg);
  border-top: 1px solid var(--glass-border-wet);
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
}

.message-input input {
  flex: 1;
  padding: 12px 16px;
  font-size: 0.875rem;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border-wet);
  border-radius: var(--radius);
  transition: all var(--transition);
}

.message-input input:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-light);
}

.send-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  flex-shrink: 0;
}

.no-conversation {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.no-conversation-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-sm);
  text-align: center;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .floating-back-btn {
    display: flex;
  }

  .messages-container {
    grid-template-columns: 1fr;
  }

  .conversation-list {
    display: none;
  }

  .conversation-list.show {
    display: flex;
    position: fixed;
    inset: 0;
    z-index: 1000;
    border-radius: 0;
  }

  .list-close-btn {
    display: block;
  }

  .mobile-back-btn {
    display: block;
    position: fixed;
    bottom: 16px;
    left: 16px;
    z-index: 1001;
    padding: 10px 18px;
    background: linear-gradient(135deg, var(--primary-start), var(--primary-end));
    color: #fff;
    border: none;
    border-radius: var(--radius-full);
    font-size: 14px;
    cursor: pointer;
    box-shadow: var(--shadow-lg), var(--shadow-glow-primary);
    transition: all var(--transition);
  }

  .mobile-back-btn:hover {
    transform: translateY(-2px);
  }

  .message-item {
    max-width: 90%;
  }
}

@media (max-width: 1024px) {
  .floating-back-btn {
    display: flex;
  }

  .messages-container {
    max-width: 1200px;
  }
}
</style>
