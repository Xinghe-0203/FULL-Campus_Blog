import api from './index'

/**
 * 校友圈接口
 */
export const circleApi = {
  /**
   * 发布动态
   * @param {Object} data - 动态数据
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  createPost(data) {
    return api.post('/circle/post', data)
  },

  /**
   * 更新动态
   * @param {number|string} postId - 动态ID
   * @param {Object} data - 动态数据
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  updatePost(postId, data) {
    return api.put(`/circle/post/${postId}`, data)
  },

  /**
   * 删除动态
   * @param {number|string} postId - 动态ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  deletePost(postId) {
    return api.delete(`/circle/post/${postId}`)
  },

  /**
   * 搜索动态
   * @param {{keyword: string, pageNum?: number, pageSize?: number}} params - 搜索参数
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  searchPosts(params) {
    return api.get('/circle/search', { params })
  },

  /**
   * 获取推荐动态
   * @param {{pageNum?: number, pageSize?: number}} params - 分页参数
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getRecommendFeed(params) {
    return api.get('/circle/feed/recommend', { params })
  },

  /**
   * 获取关注动态
   * @param {{pageNum?: number, pageSize?: number}} params - 分页参数
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getFollowingFeed(params) {
    return api.get('/circle/feed/following', { params })
  },

  /**
   * 获取动态详情
   * @param {number|string} postId - 动态ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getPostById(postId) {
    return api.get(`/circle/post/${postId}`)
  },

  /**
   * 点赞/取消点赞
   * @param {number|string} postId - 动态ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  toggleLike(postId) {
    return api.post(`/circle/like/${postId}`)
  },

  /**
   * 检查点赞状态
   * @param {number|string} postId - 动态ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  checkLikeStatus(postId) {
    return api.get(`/circle/like/check/${postId}`)
  },

  /**
   * 获取评论
   * @param {number|string} postId - 动态ID
   * @param {{pageNum?: number, pageSize?: number}} params - 分页参数
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getComments(postId, params) {
    return api.get(`/circle/comment/${postId}`, { params })
  },

  /**
   * 发表评论
   * @param {Object} data - 评论数据
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  createComment(data) {
    return api.post('/circle/comment', data)
  },

  /**
   * 删除评论
   * @param {number|string} commentId - 评论ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  deleteComment(commentId) {
    return api.delete(`/circle/comment/${commentId}`)
  },

  /**
   * 转发
   * @param {number|string} postId - 动态ID
   * @param {string} content - 转发内容
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  repost(postId, content) {
    return api.post(`/circle/repost/${postId}`, null, { params: { content } })
  },

  /**
   * 获取用户动态列表
   * @param {number|string} userId - 用户ID
   * @param {{pageNum?: number, pageSize?: number}} params - 分页参数
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getUserPosts(userId, params) {
    return api.get(`/circle/user/${userId}`, { params })
  }
}
