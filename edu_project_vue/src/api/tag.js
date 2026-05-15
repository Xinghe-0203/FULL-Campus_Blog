import api from './index'

/**
 * 标签接口
 */
export const tagApi = {
  /**
   * 获取标签列表
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getTagList() {
    return api.get('/tag/list')
  },

  /**
   * 获取单个标签
   * @param {number|string} tagId - 标签ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getTagById(tagId) {
    return api.get(`/tag/${tagId}`)
  },

  /**
   * 创建标签
   * @param {Object} data - 标签数据
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  createTag(data) {
    return api.post('/tag', data)
  },

  /**
   * 删除标签
   * @param {number|string} tagId - 标签ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  deleteTag(tagId) {
    return api.delete(`/tag/${tagId}`)
  }
}
