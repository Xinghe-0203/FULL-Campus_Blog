import api from './index'

/**
 * 收藏接口
 */
export const collectApi = {
  /**
   * 切换收藏状态
   * @param {number|string} postId - 文章ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  toggleCollect(postId) {
    return api.post(`/collect/${postId}`)
  },

  /**
   * 检查是否已收藏
   * @param {number|string} postId - 文章ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  checkCollectStatus(postId) {
    return api.get(`/collect/${postId}/status`)
  },

  /**
   * 批量检查收藏状态
   * @param {number[]|string[]} postIds - 文章ID数组
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  checkCollectStatusBatch(postIds) {
    return api.post('/collect/check/batch', postIds)
  },

  /**
   * 获取我的收藏
   * @param {{pageNum?: number, pageSize?: number}} params - 分页参数
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getMyCollections(params) {
    return api.get('/collect/my', { params })
  }
}
