import api from './index'

export const topicApi = {
  // 获取话题列表
  getTopicList(params) {
    return api.get('/topic/list', { params })
  },

  // 获取热门话题
  getHotTopics() {
    return api.get('/topic/hot')
  },

  // 获取话题详情
  getTopicById(topicId) {
    return api.get(`/topic/${topicId}`)
  },

  // 获取话题下的文章
  getTopicPosts(topicId, params) {
    return api.get(`/topic/${topicId}/posts`, { params })
  },

  // 创建话题
  createTopic(data) {
    return api.post('/topic', data)
  }
}
