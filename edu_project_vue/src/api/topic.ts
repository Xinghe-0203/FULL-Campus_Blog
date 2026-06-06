/**
 * 话题接口
 */

import api from './index'
import type { ApiResponse, PaginatedResponse, Topic, CreateTopicRequest } from '@/types'

export const topicApi = {
  /**
   * 获取话题列表
   */
  getTopicList(params: { pageNum?: number; pageSize?: number }): Promise<PaginatedResponse<Topic>> {
    return api.get('/topic/list', { params })
  },

  /**
   * 获取热门话题
   */
  getHotTopics(): Promise<ApiResponse<Topic[]>> {
    return api.get('/topic/hot')
  },

  /**
   * 获取话题详情
   */
  getTopicById(topicId: number | string): Promise<ApiResponse<Topic>> {
    return api.get(`/topic/${topicId}`)
  },

  /**
   * 获取话题下的文章
   */
  getTopicPosts(
    topicId: number | string,
    params: { pageNum?: number; pageSize?: number }
  ): Promise<PaginatedResponse<unknown>> {
    return api.get(`/topic/${topicId}/posts`, { params })
  },

  /**
   * 创建话题
   */
  createTopic(data: CreateTopicRequest): Promise<ApiResponse<Topic>> {
    return api.post('/topic', data)
  },

  /**
   * 搜索话题
   */
  searchTopics(keyword: string): Promise<ApiResponse<Topic[]>> {
    return api.get('/topic/search', { params: { keyword } })
  }
}
