import api from './index'

/**
 * 文章接口
 */
export const postApi = {
  /**
   * 创建文章
   * @param {Object} data - 文章数据
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  createPost(data) {
    return api.post('/post', data)
  },

  /**
   * 更新文章
   * @param {number|string} id - 文章ID
   * @param {Object} data - 文章更新数据
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  updatePost(id, data) {
    return api.put(`/post/${id}`, data)
  },

  /**
   * 删除文章
   * @param {number|string} id - 文章ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  deletePost(id) {
    return api.delete(`/post/${id}`)
  },

  /**
   * 获取文章详情
   * @param {number|string} id - 文章ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getPostById(id) {
    return api.get(`/post/${id}`)
  },

  /**
   * 获取文章列表
   * @param {{pageNum?: number, pageSize?: number, tagId?: number, category?: string}} params - 查询参数
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getPostList(params) {
    return api.get('/post/list', { params })
  },

  /**
   * 增加浏览量
   * @param {number|string} id - 文章ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  incrementViewCount(id) {
    return api.put(`/post/${id}/view`)
  },

  /**
   * 保存草稿
   * @param {Object} data - 草稿数据
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  saveDraft(data) {
    return api.post('/post/draft', data)
  },

  /**
   * 获取最新草稿
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getLatestDraft() {
    return api.get('/post/draft/latest')
  },

  /**
   * 获取指定草稿
   * @param {number|string} draftId - 草稿ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getDraft(draftId) {
    return api.get(`/post/draft/${draftId}`)
  },

  /**
   * 删除草稿
   * @param {number|string} draftId - 草稿ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  deleteDraft(draftId) {
    return api.delete(`/post/draft/${draftId}`)
  },

  /**
   * 高级搜索
   * @param {{keyword?: string, tagId?: number}} params - 搜索参数
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  advancedSearch(params) {
    return api.get('/post/search/advanced', { params })
  },

  /**
   * 搜索建议
   * @param {string} keyword - 搜索关键词
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  searchSuggest(keyword) {
    return api.get('/post/search/suggest', { params: { keyword } })
  },

  /**
   * 获取我的文章
   * @param {{pageNum?: number, pageSize?: number}} params - 分页参数
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getMyPosts(params) {
    return api.get('/post/my', { params })
  },

  /**
   * 获取我的草稿
   * @param {{pageNum?: number, pageSize?: number}} params - 分页参数
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getMyDrafts(params) {
    return api.get('/post/draft/my', { params })
  }
}
