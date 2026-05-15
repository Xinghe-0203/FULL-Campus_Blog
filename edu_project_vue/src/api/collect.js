import api from './index'

export const collectApi = {
  // 切换收藏状态
  toggleCollect(postId) {
    return api.post(`/collect/${postId}`)
  },

  // 检查是否已收藏
  checkCollectStatus(postId) {
    return api.get(`/collect/check/${postId}`)
  },

  // 批量检查收藏状态
  checkCollectStatusBatch(postIds) {
    return api.post('/collect/check/batch', postIds)
  },

  // 获取我的收藏
  getMyCollections(params) {
    return api.get('/collect/my', { params })
  }
}
