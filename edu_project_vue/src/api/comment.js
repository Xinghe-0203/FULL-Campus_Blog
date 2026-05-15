import api from './index'

/**
 * 评论接口
 */
export const commentApi = {
  /**
   * 发表评论
   * @param {Object} data - 评论数据（含 postId, content 等）
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  createComment(data) {
    return api.post('/comment', data)
  },

  /**
   * 获取文章评论
   * @param {number|string} postId - 文章ID
   * @param {{pageNum?: number, pageSize?: number}} params - 分页参数
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getCommentsByPostId(postId, params) {
    return api.get(`/comment/post/${postId}`, { params })
  },

  /**
   * 删除评论
   * @param {number|string} commentId - 评论ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  deleteComment(commentId) {
    return api.delete(`/comment/${commentId}`)
  },

  /**
   * 获取单个评论详情
   * @param {number|string} commentId - 评论ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getCommentById(commentId) {
    return api.get(`/comment/${commentId}`)
  },

  /**
   * 获取我的评论
   * @param {{pageNum?: number, pageSize?: number}} params - 分页参数
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getMyComments(params) {
    return api.get('/comment/my', { params })
  }
}
