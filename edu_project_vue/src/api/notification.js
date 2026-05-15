import api from './index'

export const notificationApi = {
  // 获取通知列表
  getNotifications(params) {
    return api.get('/notification/list', { params })
  },

  // 获取未读数量
  getUnreadCount() {
    return api.get('/notification/unread-count')
  },

  // 标记为已读
  markAsRead(id) {
    return api.put(`/notification/${id}/read`)
  },

  // 全部标记为已读
  markAllAsRead() {
    return api.put('/notification/read-all')
  },

  // 删除通知
  deleteNotification(id) {
    return api.delete(`/notification/${id}`)
  }
}
