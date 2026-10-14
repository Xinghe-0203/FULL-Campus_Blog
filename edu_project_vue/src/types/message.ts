/**
 * 消息/通知相关类型定义
 */

import type { ID } from './common'
import type { User } from './user'

/** 通知类型 */
export type NotificationType = 'like' | 'comment' | 'follow' | 'collect' | 'system' | 'mention'

/** 通知信息 */
export interface Notification {
  id: number
  userId: number
  type: NotificationType
  title: string
  content: string
  relatedId?: ID
  relatedType?: string
  isRead: boolean
  sender?: User
  createdAt: string
}

/** 私信信息 */
export interface Message {
  id: number | string
  senderId: number
  receiverId: number
  content: string
  isRead: boolean | number
  sender?: User
  receiver?: User
  createTime: string
  timeAgo?: string
}

/** 会话信息 */
export interface Conversation {
  conversationId: number | string | null
  user?: User
  lastMessage: string
  lastMessageTime: string | null
  unreadCount: number
  messageCount?: number
  timeAgo?: string
}

/** 私信用户在线状态 */
export interface MessagePresence {
  userId: number
  online: boolean
  lastOnlineAt?: string | null
}

/** 发送私信请求 */
export interface SendMessageRequest {
  receiverId: ID
  content: string
}

/** 举报类型 */
export type ReportTargetType = 'post' | 'comment' | 'user' | 'circle_post'

/** 举报信息 */
export interface Report {
  id: number
  reporterId: number
  targetId: ID
  targetType: ReportTargetType
  reason: string
  status: 'pending' | 'approved' | 'rejected'
  handleResult?: string
  createdAt: string
  handledAt?: string
}

/** 创建举报请求 */
export interface CreateReportRequest {
  targetId: ID
  targetType: ReportTargetType
  reason: string
  description?: string
}
