import api from './index'

export const commentApi = {
  // 发表评论
  createComment(data) {
    return api.post('/comment', data)
  },

  // 获取文章评论
  getCommentsByPostId(postId, params) {
    return api.get(`/comment/post/${postId}`, { params })
  },

  // 删除评论
  deleteComment(commentId) {
    return api.delete(`/comment/${commentId}`)
  },

  // 获取单个评论详情
  getCommentById(commentId) {
    return api.get(`/comment/${commentId}`)
  },

  // 获取我的评论
  getMyComments(params) {
    return api.get('/comment/my', { params })
  }
}
