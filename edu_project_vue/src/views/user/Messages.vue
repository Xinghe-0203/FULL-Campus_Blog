<template>
  <div class="messages-page">
    <div class="messages-container">
      <button class="back-btn" @click="router.back()">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
        返回
      </button>
      <button class="mobile-back-btn" @click="showMobileList = !showMobileList" v-if="activeConversation && !showMobileList">
        ← 会话列表
      </button>
      <!-- 会话列表 -->
      <div class="conversation-list card" :class="{ show: showMobileList }">
        <div class="list-header">
          <h2>私信</h2>
        </div>
        <div class="list-content">
          <div v-if="loadingConversations" class="empty-list">
            <p>加载中...</p>
          </div>
          <div v-else-if="conversationsError" class="empty-list">
            <p class="error-text">{{ conversationsError }}</p>
          </div>
          <div v-else-if="conversations.length > 0">
            <div 
              v-for="conv in conversations" 
              :key="conv.conversationId"
              class="conversation-item"
              :class="{ active: activeConversation?.conversationId === conv.conversationId }"
              @click="selectConversation(conv)"
            >
              <img :src="conv.user?.avatar || '/default-avatar.png'" :alt="conv.user?.nickname || conv.user?.username" class="conv-avatar" />
              <div class="conv-info">
                <span class="conv-name">{{ conv.user?.nickname || conv.user?.username }}</span>
                <span class="conv-last-message">{{ conv.lastMessage }}</span>
              </div>
              <div class="conv-meta">
                <span class="conv-time">{{ formatRelativeTime(conv.lastMessageTime) }}</span>
                <span v-if="conv.unreadCount > 0" class="conv-badge">{{ conv.unreadCount }}</span>
              </div>
            </div>
          </div>
          <div v-else class="empty-list">
            <p>暂无私信</p>
          </div>
        </div>
      </div>
      
      <!-- 消息区域 -->
      <div class="message-area card">
        <template v-if="activeConversation">
          <div class="area-header">
            <h3>{{ activeConversation.user?.nickname || activeConversation.user?.username }}</h3>
          </div>
          <div v-if="messagesError" class="empty-list">
            <p class="error-text">{{ messagesError }}</p>
          </div>
          <div v-else-if="loadingMessages" class="empty-list">
            <p>加载中...</p>
          </div>
          <div v-else class="message-list" ref="messageList">
            <div 
              v-for="msg in messages" 
              :key="msg.id"
              class="message-item"
              :class="{ mine: msg.sender?.id === userStore.userId }"
            >
              <img :src="msg.sender?.avatar || '/default-avatar.png'" :alt="msg.sender?.nickname" class="msg-avatar" />
              <div class="msg-content">
                <p class="msg-text" v-html="sanitizeText(msg.content)"></p>
                <span class="msg-time">{{ formatRelativeTime(msg.createTime) }}</span>
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
            />
            <button class="btn btn-primary btn-sm" @click="sendMessage" :disabled="!newMessage.trim()">
              发送
            </button>
          </div>
        </template>
        <div v-else class="no-conversation">
          <p>选择一个会话开始聊天</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import DOMPurify from 'dompurify'
import { useUserStore } from '../../stores/user'
import { messageApi } from '../../api/message'
import { formatRelativeTime } from '../../utils'
import { useLogger } from '../../utils/logger'
import { toast } from '../../utils/toast'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const logger = useLogger('Messages')

// XSS防护：对用户生成内容进行净化
const sanitizeText = (text) => {
  if (!text) return ''
  return DOMPurify.sanitize(text, { ALLOWED_TAGS: [] })
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

    // 更新会话列表，保留未读数状态
    conversations.value = newConversations

    // 如果有userId参数且是首次加载，自动选择对应会话
    if (!silent && route.query.userId && !activeConversation.value) {
      const conv = newConversations.find(c => c.conversationId === parseInt(route.query.userId))
      if (conv) selectConversation(conv)
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
  loadingMessages.value = true

  try {
    const response = await messageApi.getConversationMessages(conv.conversationId)
    messages.value = response.data?.records || []

    // 标记为已读
    if (conv.unreadCount > 0) {
      conv.unreadCount = 0
      try {
        await messageApi.markConversationAsRead(conv.conversationId)
      } catch (e) {
        logger.warn('Failed to mark conversation as read', { error: e.message })
      }
    }

    // 滚动到底部
    await nextTick()
    if (messageList.value) {
      messageList.value.scrollTop = messageList.value.scrollHeight
    }
  } catch (error) {
    logger.error('Failed to fetch messages', { error: error.message })
    messagesError.value = '加载消息失败'
  } finally {
    loadingMessages.value = false
  }
}

const sendMessage = async () => {
  if (!newMessage.value.trim() || !activeConversation.value) return
  if (newMessage.value.length > 1000) {
    toast.warning('消息内容不能超过1000个字符')
    return
  }

  try {
    await messageApi.sendMessage({
      receiverId: activeConversation.value.conversationId,
      content: newMessage.value
    })

    // 添加到本地消息列表（后端会推送通知，这里乐观更新）
    messages.value.push({
      id: Date.now(),
      sender: { id: userStore.userId, avatar: userStore.avatar, nickname: userStore.nickname || userStore.username },
      content: newMessage.value,
      createTime: new Date().toISOString(),
      isRead: 1
    })

    newMessage.value = ''

    // 滚动到底部
    await nextTick()
    if (messageList.value) {
      messageList.value.scrollTop = messageList.value.scrollHeight
    }
  } catch (error) {
    logger.error('Failed to send message', { error: error.message })
    toast.error('发送失败')
  }
}

const startPolling = () => {
  // 每30秒轮询一次会话列表
  pollingInterval = setInterval(() => {
    fetchConversations(true)
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
}

.messages-container {
  max-width: 1000px;
  margin: 0 auto;
  height: 100%;
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: var(--spacing-md);
}

.conversation-list {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.list-header {
  padding: var(--spacing-md);
  border-bottom: 1px solid var(--border);
}

.list-header h2 {
  font-size: 1.125rem;
  font-weight: 600;
}

.list-content {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md);
  cursor: pointer;
  transition: background var(--transition);
}

.conversation-item:hover {
  background: var(--background);
}

.conversation-item.active {
  background: var(--primary-light);
}

.conv-avatar {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-full);
  object-fit: cover;
}

.conv-info {
  flex: 1;
  min-width: 0;
}

.conv-name {
  display: block;
  font-weight: 600;
  font-size: 0.875rem;
  color: var(--text-primary);
}

.conv-last-message {
  display: block;
  font-size: 0.75rem;
  color: var(--text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conv-meta {
  text-align: right;
}

.conv-time {
  display: block;
  font-size: 0.625rem;
  color: var(--text-muted);
}

.conv-badge {
  display: inline-block;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  font-size: 0.625rem;
  font-weight: 600;
  color: white;
  background: var(--error);
  border-radius: var(--radius-full);
  margin-top: 4px;
}

.empty-list {
  text-align: center;
  padding: var(--spacing-2xl);
  color: var(--text-muted);
}

.message-area {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.area-header {
  padding: var(--spacing-md);
  border-bottom: 1px solid var(--border);
}

.area-header h3 {
  font-size: 1rem;
  font-weight: 600;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-md);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.message-item {
  display: flex;
  gap: var(--spacing-sm);
  max-width: 70%;
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
}

.msg-content {
  background: var(--background);
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius);
}

.message-item.mine .msg-content {
  background: var(--primary);
  color: white;
}

.msg-text {
  font-size: 0.875rem;
  line-height: 1.5;
}

.msg-time {
  display: block;
  font-size: 0.625rem;
  color: var(--text-muted);
  margin-top: 4px;
}

.message-item.mine .msg-time {
  color: rgba(255, 255, 255, 0.7);
}

.message-input {
  display: flex;
  gap: var(--spacing-sm);
  padding: var(--spacing-md);
  border-top: 1px solid var(--border);
}

.message-input input {
  flex: 1;
  padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  font-size: 0.875rem;
}

.message-input input:focus {
  outline: none;
  border-color: var(--primary);
}

.no-conversation {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-muted);
}

.back-btn { display: flex; align-items: center; gap: 4px; padding: 8px 12px; background: transparent; border: 1px solid var(--border); border-radius: 8px; color: var(--text-secondary); cursor: pointer; font-size: 0.875rem; transition: all 0.2s; width: fit-content; margin-bottom: 16px; }
.back-btn:hover { background: var(--border); color: var(--text-primary); }
.mobile-back-btn { display: none; }

@media (max-width: 768px) {
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
  }

  .mobile-back-btn {
    display: block;
    position: fixed;
    bottom: 16px;
    left: 16px;
    z-index: 1001;
    padding: 8px 16px;
    background: var(--primary);
    color: #fff;
    border: none;
    border-radius: 20px;
    font-size: 14px;
    cursor: pointer;
    box-shadow: 0 2px 12px rgba(0,0,0,0.2);
  }
}
</style>
