import api from './index'

/**
 * 媒体文件上传接口
 */
export const mediaApi = {
  /**
   * 上传单个文件
   * @param {File} file - 上传的文件
   * @param {'article'|'circle'|'avatar'} [type='article'] - 文件类型: article=文章(所有文件), circle=校友圈(图片+视频), avatar=头像(仅图片)
   * @param {(event: ProgressEvent) => void} [onProgress] - 上传进度回调
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  uploadFile(file, type = 'article', onProgress) {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('type', type)

    return api.post('/media/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: type === 'article' ? 300000 : 60000,
      onUploadProgress: onProgress
    })
  },

  /**
   * 批量上传文件（最多10个）
   * @param {File[]} files - 上传的文件数组
   * @param {'article'|'circle'|'avatar'} [type='article'] - 文件类型
   * @param {(event: ProgressEvent) => void} [onProgress] - 上传进度回调
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  uploadMultipleFiles(files, type = 'article', onProgress) {
    const formData = new FormData()
    files.forEach(file => formData.append('files', file))
    formData.append('type', type)

    return api.post('/media/upload/multiple', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 300000,
      onUploadProgress: onProgress
    })
  },

  /**
   * 删除媒体文件
   * @param {number|string} mediaId - 媒体ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  deleteMedia(mediaId) {
    return api.delete(`/media/${mediaId}`)
  },

  /**
   * 获取媒体文件信息
   * @param {number|string} mediaId - 媒体ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getMediaInfo(mediaId) {
    return api.get(`/media/${mediaId}`)
  },

  /**
   * 绑定媒体文件到文章
   * @param {number|string} postId - 文章ID
   * @param {number[]} mediaIds - 媒体ID数组
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  bindPostMedia(postId, mediaIds) {
    return api.put(`/media/bind/${postId}`, mediaIds)
  },

  /**
   * 获取文章的媒体列表
   * @param {number|string} postId - 文章ID
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getPostMedia(postId) {
    return api.get(`/media/post/${postId}`)
  },

  /**
   * 获取用户媒体列表（分页）
   * @param {Object} params - 查询参数
   * @returns {Promise<{code: number, message: string, data: Object}>}
   */
  getUserMediaList(params) {
    return api.get('/media/list', { params })
  }
}
