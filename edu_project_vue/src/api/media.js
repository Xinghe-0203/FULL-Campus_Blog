import api from './index'

export const mediaApi = {
  // 上传单个文件
  // type: article=文章(所有文件), circle=校友圈(图片+视频), avatar=头像(仅图片)
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
