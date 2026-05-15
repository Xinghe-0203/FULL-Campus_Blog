import api from './index'

export const followApi = {
  // 关注/取消关注
  toggleFollow(targetUserId) {
    return api.post('/follow', { targetUserId })
  },

  // 取消关注
  unfollow(targetUserId) {
    return api.delete(`/follow/${targetUserId}`)
  },

  // 检查关注状态
  checkFollowStatus(targetUserId) {
    return api.get(`/follow/check/${targetUserId}`)
  },

  // 获取粉丝列表
  getFollowers(userId, params) {
    return api.get(`/follow/followers/${userId}`, { params })
  },

  // 获取关注列表
  getFollowing(userId, params) {
    return api.get(`/follow/following/${userId}`, { params })
  },

  // 获取关注/粉丝数量
  getFollowCounts(userId) {
    return api.get(`/follow/counts/${userId}`)
  }
}
