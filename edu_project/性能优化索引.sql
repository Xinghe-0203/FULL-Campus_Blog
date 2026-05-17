-- =============================================================================
-- 性能优化索引脚本 v2.0
-- 解决 "VERY SLOW" 接口问题（15秒+ 响应时间）
-- 执行时间：2026-05-17
-- =============================================================================

USE campus_blog;

-- 1. 消息未读计数优化（修复 /api/message/unread-count 15248ms）
-- 原查询：WHERE receiver_id = ? AND is_read = 0
CREATE INDEX IF NOT EXISTS idx_message_receiver_read ON blog_message (receiver_id, is_read);

-- 2. 消息会话查询优化（修复 /api/message/conversations）
-- 原查询：WHERE sender_id = ? OR receiver_id = ? ORDER BY create_time DESC
CREATE INDEX IF NOT EXISTS idx_message_sender_time ON blog_message (sender_id, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_message_receiver_time ON blog_message (receiver_id, create_time DESC);

-- 3. 热门内容查询优化（修复 /api/trending/* 15976ms）
-- 原查询：WHERE date >= ? AND date < ? ORDER BY score DESC
CREATE INDEX IF NOT EXISTS idx_trending_date_score ON blog_trending (date, score DESC);

-- 4. 校友圈热门内容优化
-- 原查询：WHERE status = 1 AND visibility = 0 ORDER BY like_count DESC
CREATE INDEX IF NOT EXISTS idx_circle_status_visibility_like ON blog_circle_post (status, visibility, like_count DESC);

-- 5. 举报处理时间查询优化
-- 原查询：WHERE handle_time >= ? AND status != 0
CREATE INDEX IF NOT EXISTS idx_report_handle_time ON blog_report (handle_time, status);

-- 6. 文章列表查询优化（修复 /api/post/list 18117ms）
-- 原查询：WHERE status = 1 AND is_deleted = 0 ORDER BY create_time DESC
CREATE INDEX IF NOT EXISTS idx_post_status_deleted_create ON blog_post (status, is_deleted, create_time DESC);

-- 7. 文章热门排序优化
-- 原查询：WHERE status = 1 AND is_deleted = 0 ORDER BY view_count DESC
CREATE INDEX IF NOT EXISTS idx_post_status_deleted_view ON blog_post (status, is_deleted, view_count DESC);

-- 8. 文章精华排序优化
-- 原查询：WHERE status = 1 AND is_deleted = 0 ORDER BY like_count DESC
CREATE INDEX IF NOT EXISTS idx_post_status_deleted_like ON blog_post (status, is_deleted, like_count DESC);

-- 9. 通知列表查询优化
-- 原查询：WHERE user_id = ? AND is_read = 0 ORDER BY create_time DESC
CREATE INDEX IF NOT EXISTS idx_notification_user_read_create ON blog_notification (user_id, is_read, create_time DESC);

-- 10. 用户创建时间索引（统计增长趋势）
CREATE INDEX IF NOT EXISTS idx_user_create_time ON sys_user (create_time DESC);

-- 11. 评论文章ID索引（统计评论数）
CREATE INDEX IF NOT EXISTS idx_comment_post_id ON blog_comment (post_id, create_time DESC);

-- 12. 点赞文章ID索引（统计点赞数）
CREATE INDEX IF NOT EXISTS idx_like_post_id ON blog_like (post_id, create_time DESC);

-- 13. 收藏文章ID索引（统计收藏数）
CREATE INDEX IF NOT EXISTS idx_collect_post_id ON blog_collect (post_id, create_time DESC);

-- 14. 标签文章计数优化
CREATE INDEX IF NOT EXISTS idx_post_tag_tag_id ON blog_post_tag (tag_id, post_id);

-- 15. 校友圈评论优化
CREATE INDEX IF NOT EXISTS idx_circle_comment_post ON blog_circle_comment (post_id, create_time DESC);

-- 16. 关注关系创建时间索引
CREATE INDEX IF NOT EXISTS idx_follow_create_time ON blog_follow (create_time DESC);

-- =============================================================================
-- 验证索引创建结果
-- =============================================================================
SHOW INDEX FROM blog_message WHERE Key_name LIKE 'idx_message%';
SHOW INDEX FROM blog_trending WHERE Key_name LIKE 'idx_trending%';
SHOW INDEX FROM blog_circle_post WHERE Key_name LIKE 'idx_circle%';
SHOW INDEX FROM blog_post WHERE Key_name LIKE 'idx_post%';
SHOW INDEX FROM blog_notification WHERE Key_name LIKE 'idx_notification%';
SHOW INDEX FROM blog_report WHERE Key_name LIKE 'idx_report%';
SHOW INDEX FROM sys_user WHERE Key_name LIKE 'idx_user%';

-- =============================================================================
-- 性能预期提升
-- =============================================================================
-- /api/message/unread-count:     15248ms -> <50ms   (300x 提升)
-- /api/trending/*:               15976ms -> <200ms  (80x 提升)
-- /api/post/list:                18117ms -> <300ms  (60x 提升)
-- /api/statistics/community:     7855ms  -> <500ms  (15x 提升)
-- /api/notification/unread-count: 未知   -> <50ms   (缓存命中)
