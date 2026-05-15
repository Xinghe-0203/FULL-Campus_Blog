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
  }
}
