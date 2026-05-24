import api from './index'

/**
 * 话题接口
 */
export const topicApi = {
  /**
   * 获取话题列表
   * @param {{pageNum?: number, pageSize?: number}} params - 分页参数
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getTopicList(params) {
    return api.get('/topic/list', { params })
  },

  /**
   * 获取热门话题
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getHotTopics() {
    return api.get('/topic/hot')
  },

  /**
   * 获取话题详情
   * @param {number|string} topicId - 话题ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getTopicById(topicId) {
    return api.get(`/topic/${topicId}`)
  },

  /**
   * 获取话题下的文章
   * @param {number|string} topicId - 话题ID
   * @param {{pageNum?: number, pageSize?: number}} params - 分页参数
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getTopicPosts(topicId, params) {
    return api.get(`/topic/${topicId}/posts`, { params })
  },

  /**
   * 创建话题
   * @param {Object} data - 话题数据
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  createTopic(data) {
    return api.post('/topic', data)
  },

  /**
   * 搜索话题
   * @param {string} keyword - 关键词
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  searchTopics(keyword) {
    return api.get('/topic/search', { params: { keyword } })
  }
}
