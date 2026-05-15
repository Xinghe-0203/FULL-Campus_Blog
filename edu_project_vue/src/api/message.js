import api from './index'

/**
 * 私信接口
 */
export const messageApi = {
  /**
   * 获取会话列表
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getConversations() {
    return api.get('/message/conversations')
  },

  /**
   * 获取会话消息
   * @param {number|string} partnerUserId - 会话对方用户ID
   * @param {{pageNum?: number, pageSize?: number}} params - 分页参数
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getConversationMessages(partnerUserId, params) {
    return api.get(`/message/conversation/${partnerUserId}`, { params })
  },

  /**
   * 发送私信
   * @param {Object} data - 私信数据（含 receiverId, content 等）
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  sendMessage(data) {
    return api.post('/message/send', data)
  },

  /**
   * 标记会话中所有消息为已读
   * @param {number|string} partnerUserId - 会话对方用户ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  markConversationAsRead(partnerUserId) {
    return api.put(`/message/conversation/${partnerUserId}/read`)
  },

  /**
   * 获取未读数量
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getUnreadCount() {
    return api.get('/message/unread-count')
  },

}
