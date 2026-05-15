import api from './index'

/**
 * 通知接口
 */
export const notificationApi = {
  /**
   * 获取通知列表
   * @param {{pageNum?: number, pageSize?: number}} params - 分页参数
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getNotifications(params) {
    return api.get('/notification/list', { params })
  },

  /**
   * 获取未读数量
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getUnreadCount() {
    return api.get('/notification/unread-count')
  },

  /**
   * 标记为已读
   * @param {number|string} id - 通知ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  markAsRead(id) {
    return api.put(`/notification/${id}/read`)
  },

  /**
   * 全部标记为已读
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  markAllAsRead() {
    return api.put('/notification/read-all')
  },

  /**
   * 删除通知
   * @param {number|string} id - 通知ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  deleteNotification(id) {
    return api.delete(`/notification/${id}`)
  }
}
