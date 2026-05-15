import api from './index'

export const postApi = {
  // 创建文章
  createPost(data) {
    return api.post('/post', data)
  },

  // 更新文章
  updatePost(id, data) {
    return api.put(`/post/${id}`, data)
  },

  // 删除文章
  deletePost(id) {
    return api.delete(`/post/${id}`)
  },

  // 获取文章详情
  getPostById(id) {
    return api.get(`/post/${id}`)
  },

  // 获取文章列表
  getPostList(params) {
    return api.get('/post/list', { params })
  },

  // 增加浏览量
  incrementViewCount(id) {
    return api.put(`/post/${id}/view`)
  },

  // 保存草稿
  saveDraft(data) {
    return api.post('/post/draft', data)
  },

  // 获取最新草稿
  getLatestDraft() {
    return api.get('/post/draft/latest')
  },

  // 获取指定草稿
  getDraft(draftId) {
    return api.get(`/post/draft/${draftId}`)
  },

  // 删除草稿
  deleteDraft(draftId) {
    return api.delete(`/post/draft/${draftId}`)
  },

  // 高级搜索
  advancedSearch(params) {
    return api.get('/post/search/advanced', { params })
  },

  // 搜索建议
  searchSuggest(keyword) {
    return api.get('/post/search/suggest', { params: { keyword } })
  },

  // 获取我的文章
  getMyPosts(params) {
    return api.get('/post/my', { params })
  },

  // 获取我的草稿
  getMyDrafts(params) {
    return api.get('/post/draft/my', { params })
  }
}
