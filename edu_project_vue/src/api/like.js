import api from './index'

export const likeApi = {
  // 切换点赞状态
  toggleLike(postId) {
    return api.post(`/like/${postId}`)
  },

  // 检查是否已点赞
  checkLikeStatus(postId) {
    return api.get(`/like/check/${postId}`)
  },

  // 批量检查点赞状态
  checkLikeStatusBatch(postIds) {
    return api.post('/like/check/batch', postIds)
  },

  // 获取我的点赞
  getMyLikes(params) {
    return api.get('/like/my', { params })
  }
}
