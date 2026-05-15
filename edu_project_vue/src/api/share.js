import api from './index'

/**
 * 分享接口
 */
export const shareApi = {
  /**
   * 记录分享
   * @param {number|string} postId - 文章ID
   * @param {string} [platform='web'] - 分享平台
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  recordShare(postId, platform = 'web') {
    return api.post(`/share/${postId}`, null, { params: { platform } })
  },

  /**
   * 获取分享数
   * @param {number|string} postId - 文章ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getShareCount(postId) {
    return api.get(`/share/count/${postId}`)
  }
}

export default shareApi