import api from './index'

export const shareApi = {
  // 记录分享
  recordShare(postId, platform = 'web') {
    return api.post(`/share/${postId}`, null, { params: { platform } })
  },

  // 获取分享数
  getShareCount(postId) {
    return api.get(`/share/count/${postId}`)
  }
}

export default shareApi