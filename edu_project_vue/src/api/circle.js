import api from './index'

export const circleApi = {
  // 发布动态
  createPost(data) {
    return api.post('/circle/post', data)
  },

  // 获取推荐动态
  getRecommendFeed(params) {
    return api.get('/circle/feed/recommend', { params })
  },

  // 获取关注动态
  getFollowingFeed(params) {
    return api.get('/circle/feed/following', { params })
  },

  // 获取动态详情
  getPostById(postId) {
    return api.get(`/circle/post/${postId}`)
  },

  // 点赞/取消点赞
  toggleLike(postId) {
    return api.post(`/circle/like/${postId}`)
  },

  // 检查点赞状态
  checkLikeStatus(postId) {
    return api.get(`/circle/like/check/${postId}`)
  },

  // 获取评论
  getComments(postId, params) {
    return api.get(`/circle/comment/${postId}`, { params })
  },

  // 发表评论
  createComment(data) {
    return api.post('/circle/comment', data)
  },

  // 删除评论
  deleteComment(commentId) {
    return api.delete(`/circle/comment/${commentId}`)
  },

  // 转发
  repost(postId, content) {
    return api.post(`/circle/repost/${postId}`, null, { params: { content } })
  },

  // 获取用户动态列表
  getUserPosts(userId, params) {
    return api.get(`/circle/user/${userId}`, { params })
  }
}
