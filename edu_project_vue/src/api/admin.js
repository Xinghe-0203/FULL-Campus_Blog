import api from './index'
import { useUserStore } from '../stores/user'
import { useLogger } from '../utils/logger'

const logger = useLogger('AdminApi')

export const statsApi = {
  // 获取社区统计
  getCommunityStats() {
    return api.get('/statistics/community')
  }
}

export const adminApi = {
  // 获取待处理举报
  getPendingReports(params) {
    return api.get('/admin/reports/pending', { params })
  },

  // 获取举报详情
  getReportById(reportId) {
    return api.get(`/admin/reports/${reportId}`)
  },

  // 处理举报
  handleReport(reportId, data) {
    return api.put(`/admin/reports/${reportId}`, data)
  },

  // 处理用户状态
  handleUserStatus(userId, data) {
    return api.put(`/admin/user/${userId}/status`, data)
  },

  // 获取用户列表
  getUserList(params) {
    return api.get('/admin/user/list', { params })
  },

  // 重置用户密码
  resetUserPassword(userId) {
    const userStore = useUserStore()
    logger.info('Admin password reset initiated', { 
      targetUserId: userId, 
      adminId: userStore.userId 
    })
    return api.put(`/admin/user/${userId}/reset-password`)
  },

  // 获取文章列表
  getPostList(params) {
    return api.get('/admin/post/list', { params })
  },

  // 删除文章
  deletePost(postId) {
    return api.delete(`/admin/post/${postId}`)
  },

  // 获取待审核文章
  getReviewList(params) {
    return api.get('/admin/post/review-list', { params })
  },

  // 审核通过
  approvePost(postId) {
    return api.put(`/admin/post/${postId}/approve`)
  },

  // 审核拒绝
  rejectPost(postId, reason) {
    return api.put(`/admin/post/${postId}/reject`, { reason })
  },

  // 获取统计数据
  getStatistics() {
    return api.get('/admin/statistics')
  },

}
