import api from './index'

export const tagApi = {
  // 获取标签列表
  getTagList() {
    return api.get('/tag/list')
  },

  // 获取单个标签
  getTagById(tagId) {
    return api.get(`/tag/${tagId}`)
  },

  // 创建标签
  createTag(data) {
    return api.post('/tag', data)
  },

  // 删除标签
  deleteTag(tagId) {
    return api.delete(`/tag/${tagId}`)
  }
}
