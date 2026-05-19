import api from './index'

/**
 * 点赞接口
 */
export const likeApi = {
  /**
   * 切换点赞状态
   * @param {number|string} postId - 文章ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  toggleLike(postId) {
    return api.post(`/like/${postId}`)
  },

  /**
   * 检查是否已点赞
   * @param {number|string} postId - 文章ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  checkLikeStatus(postId) {
    return api.get(`/like/${postId}/status`)
  },

  /**
   * 批量检查点赞状态
   * @param {number[]|string[]} postIds - 文章ID数组
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  checkLikeStatusBatch(postIds) {
    return api.post('/like/check/batch', postIds)
  },

  /**
   * 获取我的点赞
   * @param {{pageNum?: number, pageSize?: number}} params - 分页参数
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getMyLikes(params) {
    return api.get('/like/my', { params })
  }
}
