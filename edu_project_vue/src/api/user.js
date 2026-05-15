import api, { getToken } from './index'

/**
 * 用户认证与资料接口
 */
export const userApi = {
  /**
   * 注册
   * @param {{username: string, password: string, email?: string}} data - 注册信息
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  register(data) {
    return api.post('/user/register', data)
  },

  /**
   * 登录
   * @param {{username: string, password: string, remember?: boolean}} data - 登录凭证
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  login(data) {
    return api.post('/user/login', data)
  },

  /**
   * 刷新Token
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  refreshToken() {
    const refreshToken = getToken('edu_refreshToken')
    return api.post('/user/refresh', null, {
      headers: { 'Authorization': 'Bearer ' + refreshToken }
    })
  },

  /**
   * 登出
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  logout() {
    return api.post('/user/logout')
  },

  /**
   * 获取用户信息
   * @param {number|string} id - 用户ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getUserById(id) {
    return api.get(`/user/${id}`)
  },

  /**
   * 修改密码
   * @param {{oldPassword: string, newPassword: string}} data - 密码数据
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  changePassword(data) {
    return api.put('/user/password', data)
  },

  /**
   * 搜索用户
   * @param {{keyword: string, pageNum?: number, pageSize?: number}} params - 搜索参数
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  searchUsers(params) {
    return api.get('/user/search', { params })
  },

  /**
   * 发送验证码
   * @param {string} email - 邮箱地址
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  sendCode(email) {
    return api.post('/auth/password/send-code', { email })
  },

  /**
   * 重置密码
   * @param {{email: string, code: string, password: string}} data - 重置密码数据
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  resetPassword(data) {
    return api.put('/auth/password/reset-password', data)
  },

  /**
   * 更新个人资料
   * @param {Object} data - 个人资料更新数据
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  updateProfile(data) {
    return api.put('/user/profile', data)
  },

  /**
   * 更新头像
   * @param {string} avatarUrl - 新头像URL
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  updateAvatar(avatarUrl) {
    return api.put('/user/avatar', null, { params: { avatar: avatarUrl } })
  },

  /**
   * 更新封面图
   * @param {string} coverImageUrl - 新封面图URL
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  updateCoverImage(coverImageUrl) {
    return api.put('/user/cover-image', null, { params: { coverImage: coverImageUrl } })
  },

  /**
   * 发送注册验证码
   * @param {string} email - 邮箱地址
   * @param {string} username - 用户名
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  sendRegisterCode(email, username) {
    return api.post('/auth/register/send-code', { email, username })
  },

  /**
   * 验证注册验证码并完成注册
   * @param {Object} data - 验证数据
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  verifyRegisterCode(data) {
    return api.post('/auth/register/verify', data)
  },

  /**
   * 登出其他设备
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  logoutOtherDevices() {
    return api.post('/user/logout-other-devices')
  },

  /**
   * 获取设备数量
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getDevices() {
    return api.get('/user/devices')
  }
}
