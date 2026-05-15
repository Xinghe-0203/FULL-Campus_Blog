import api from './index'

export const reportApi = {
  // 提交举报
  createReport(data) {
    return api.post('/report', data)
  },

  // 获取我的举报
  getMyReports(params) {
    return api.get('/report/my', { params })
  }
}
