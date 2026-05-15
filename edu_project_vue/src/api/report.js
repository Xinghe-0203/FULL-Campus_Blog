import api from './index'

/**
 * 举报接口
 */
export const reportApi = {
  /**
   * 提交举报
   * @param {Object} data - 举报数据（含 targetId, targetType, reason 等）
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  createReport(data) {
    return api.post('/report', data)
  },

  /**
   * 获取我的举报
   * @param {{pageNum?: number, pageSize?: number}} params - 分页参数
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getMyReports(params) {
    return api.get('/report/my', { params })
  }
}
