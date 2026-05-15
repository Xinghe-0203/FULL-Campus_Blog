import api from './index'

export const trendingApi = {
  // 获取热门文章
  getHotPosts(params) {
    return api.get('/trending/posts', {
      params: {
        pageNum: params.pageNum || 1,
        pageSize: params.pageSize || 10
      }
    })
  },

  // 获取热门标签
  getHotTags() {
    return api.get('/trending/hot-tags')
  },

  // 更新热门状态
  updateTrending(postId) {
    return api.put(`/trending/update/${postId}`)
  }
}
