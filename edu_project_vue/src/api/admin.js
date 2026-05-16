import api from './index'
import { useUserStore } from '../stores/user'
import { useLogger } from '../utils/logger'

const logger = useLogger('AdminApi')

/**
 * 社区统计接口
 */
export const statsApi = {
  /**
   * 获取社区统计
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getCommunityStats() {
    return api.get('/statistics/community')
  }
}

/**
 * 管理后台接口
 */
export const adminApi = {
  /**
   * 获取待处理举报
   * @param {{pageNum?: number, pageSize?: number}} params - 分页参数
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getPendingReports(params) {
    return api.get('/admin/reports/pending', { params })
  },

  /**
   * 获取举报详情
   * @param {number|string} reportId - 举报ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getReportById(reportId) {
    return api.get(`/admin/reports/${reportId}`)
  },

  /**
   * 处理举报
   * @param {number|string} reportId - 举报ID
   * @param {Object} data - 处理数据
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  handleReport(reportId, data) {
    return api.put(`/admin/reports/${reportId}`, data)
  },

  /**
   * 处理用户状态
   * @param {number|string} userId - 用户ID
   * @param {Object} data - 状态数据
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  handleUserStatus(userId, data) {
    return api.put(`/admin/user/${userId}/status`, data)
  },

  /**
   * 获取用户列表
   * @param {{pageNum?: number, pageSize?: number, keyword?: string}} params - 查询参数
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getUserList(params) {
    return api.get('/admin/user/list', { params })
  },

  /**
   * 重置用户密码
   * @param {number|string} userId - 目标用户ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  resetUserPassword(userId) {
    const userStore = useUserStore()
    logger.info('Admin password reset initiated', { 
      targetUserId: userId, 
      adminId: userStore.userId 
    })
    return api.put(`/admin/user/${userId}/reset-password`)
  },

  /**
   * 封禁/解封用户
   * @param {number|string} userId - 用户ID
   * @param {boolean} ban - true=封禁, false=解封
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  banUser(userId, ban) {
    return api.put(`/admin/user/${userId}/ban`, { ban })
  },

  /**
   * 获取文章列表
   * @param {{pageNum?: number, pageSize?: number, status?: string}} params - 查询参数
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getPostList(params) {
    return api.get('/admin/post/list', { params })
  },

  /**
   * 删除文章
   * @param {number|string} postId - 文章ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  deletePost(postId) {
    return api.delete(`/admin/post/${postId}`)
  },

  /**
   * 获取待审核文章
   * @param {{pageNum?: number, pageSize?: number}} params - 分页参数
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getReviewList(params) {
    return api.get('/admin/post/review-list', { params })
  },

  /**
   * 审核通过文章
   * @param {number|string} postId - 文章ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  approvePost(postId) {
    return api.put(`/admin/post/${postId}/approve`)
  },

  /**
   * 审核拒绝文章
   * @param {number|string} postId - 文章ID
   * @param {string} reason - 拒绝原因
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  rejectPost(postId, reason) {
    return api.put(`/admin/post/${postId}/reject`, { reason })
  },

  /**
   * 获取统计数据
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getStatistics() {
    return api.get('/admin/statistics')
  },

}
