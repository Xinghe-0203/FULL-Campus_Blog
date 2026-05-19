import api from './index'

/**
 * 关注接口
 */
export const followApi = {
  /**
   * 关注/取消关注
   * @param {number|string} targetUserId - 目标用户ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  toggleFollow(targetUserId) {
    return api.post('/follow', { targetUserId })
  },

  /**
   * 取消关注
   * @param {number|string} targetUserId - 目标用户ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  unfollow(targetUserId) {
    return api.delete(`/follow/${targetUserId}`)
  },

  /**
   * 检查关注状态
   * @param {number|string} targetUserId - 目标用户ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  checkFollowStatus(targetUserId) {
    return api.get(`/follow/${targetUserId}/status`)
  },

  /**
   * 获取粉丝列表
   * @param {number|string} userId - 用户ID
   * @param {{pageNum?: number, pageSize?: number}} params - 分页参数
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getFollowers(userId, params) {
    return api.get(`/follow/followers/${userId}`, { params })
  },

  /**
   * 获取关注列表
   * @param {number|string} userId - 用户ID
   * @param {{pageNum?: number, pageSize?: number}} params - 分页参数
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getFollowing(userId, params) {
    return api.get(`/follow/following/${userId}`, { params })
  },

  /**
   * 获取关注/粉丝数量
   * @param {number|string} userId - 用户ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getFollowCounts(userId) {
    return api.get(`/follow/counts/${userId}`)
  }
}
