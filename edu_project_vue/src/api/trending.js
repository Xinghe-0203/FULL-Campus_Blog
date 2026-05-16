import api from './index'

/**
 * 热搜/热门接口
 */
export const trendingApi = {
  /**
   * 获取热门文章
   * @param {{pageNum?: number, pageSize?: number}} params - 分页参数
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getHotPosts(params) {
    return api.get('/trending/posts', {
      params: {
        pageNum: params.pageNum || 1,
        pageSize: params.pageSize || 10
      }
    })
  },

  /**
   * 获取热门标签
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getHotTags() {
    return api.get('/trending/hot-tags')
  },

  /**
   * 获取热门混排内容（文章+动态）
   * @param {{pageNum?: number, pageSize?: number}} params - 分页参数
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getHotContent(params) {
    return api.get('/trending/content', {
      params: {
        pageNum: params.pageNum || 1,
        pageSize: params.pageSize || 10
      }
    })
  },

  /**
   * 更新热门状态
   * @param {number|string} postId - 文章ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  updateTrending(postId) {
    return api.put(`/trending/update/${postId}`)
  }
}
