import api from './index'

export const messageApi = {
  // 获取会话列表
  getConversations() {
    return api.get('/message/conversations')
  },

  // 获取会话消息
  getConversationMessages(partnerUserId, params) {
    return api.get(`/message/conversation/${partnerUserId}`, { params })
  },

  // 发送私信
  sendMessage(data) {
    return api.post('/message/send', data)
  },

  // 标记会话中所有消息为已读
  markConversationAsRead(partnerUserId) {
    return api.put(`/message/conversation/${partnerUserId}/read`)
  },

  // 获取未读数量
  getUnreadCount() {
    return api.get('/message/unread-count')
  },

}
