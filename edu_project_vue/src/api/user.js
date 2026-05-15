import api, { getToken } from './index'

export const userApi = {
  // 注册
  register(data) {
    return api.post('/user/register', data)
  },

  // 登录
  login(data) {
    return api.post('/user/login', data)
  },

  // 刷新Token
  refreshToken() {
    const refreshToken = getToken('edu_refreshToken')
    return api.post('/user/refresh', null, {
      headers: { 'Authorization': 'Bearer ' + refreshToken }
    })
  },

  // 登出
  logout() {
    return api.post('/user/logout')
  },

  // 获取用户信息
  getUserById(id) {
    return api.get(`/user/${id}`)
  },

  // 修改密码
  changePassword(data) {
    return api.put('/user/password', data)
  },

  // 搜索用户
  searchUsers(params) {
    return api.get('/user/search', { params })
  },

  // 发送验证码
  sendCode(email) {
    return api.post('/auth/password/send-code', { email })
  },

  // 重置密码
  resetPassword(data) {
    return api.put('/auth/password/reset-password', data)
  },

  // 更新个人资料
  updateProfile(data) {
    return api.put('/user/profile', data)
  },

  // 更新头像
  updateAvatar(avatarUrl) {
    return api.put('/user/avatar', null, { params: { avatar: avatarUrl } })
  },

  // 更新封面图
  updateCoverImage(coverImageUrl) {
    return api.put('/user/cover-image', null, { params: { coverImage: coverImageUrl } })
  },

  // 发送注册验证码
  sendRegisterCode(email, username) {
    return api.post('/auth/register/send-code', { email, username })
  },

  // 验证注册验证码并完成注册
  verifyRegisterCode(data) {
    return api.post('/auth/register/verify', data)
  },

  // 登出其他设备
  logoutOtherDevices() {
    return api.post('/user/logout-other-devices')
  },

  // 获取设备数量
  getDevices() {
    return api.get('/user/devices')
  }
}
