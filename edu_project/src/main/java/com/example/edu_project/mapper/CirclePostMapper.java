package com.example.edu_project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.edu_project.entity.CirclePost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface CirclePostMapper extends BaseMapper<CirclePost> {

    @Select("SELECT COUNT(DISTINCT user_id) FROM blog_circle_post WHERE create_time >= #{since} AND is_deleted = 0")
    Long countDistinctAuthorsSince(@Param("since") LocalDateTime since);

    @Update("UPDATE blog_circle_post SET view_count = view_count + 1 WHERE id = #{id} AND is_deleted = 0")
    void incrementViewCount(@Param("id") Long id);

    @Update("UPDATE blog_circle_post SET like_count = like_count + 1 WHERE id = #{id} AND is_deleted = 0")
    void incrementLikeCount(@Param("id") Long id);

    @Update("UPDATE blog_circle_post SET like_count = like_count - 1 WHERE id = #{id} AND is_deleted = 0 AND like_count > 0")
    void decrementLikeCount(@Param("id") Long id);

    @Update("UPDATE blog_circle_post SET comment_count = comment_count + 1 WHERE id = #{id} AND is_deleted = 0")
    void incrementCommentCount(@Param("id") Long id);

    @Update("UPDATE blog_circle_post SET comment_count = comment_count - #{count} WHERE id = #{id} AND is_deleted = 0 AND comment_count >= #{count}")
    void decrementCommentCount(@Param("id") Long id, @Param("count") int count);

    @Update("UPDATE blog_circle_post SET repost_count = repost_count + 1 WHERE id = #{id} AND is_deleted = 0")
    void incrementRepostCount(@Param("id") Long id);

    @Update("UPDATE blog_circle_post SET repost_count = repost_count - 1 WHERE id = #{id} AND is_deleted = 0 AND repost_count > 0")
    void decrementRepostCount(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM blog_circle_post WHERE (topic_ids LIKE '%' || #{topicId} || '%' OR topic_ids LIKE '%\"' || #{topicId} || \"%') AND status = 1 AND is_deleted = 0")
    Long countByTopicId(@Param("topicId") Long topicId);

    @Select("<script>" +
            "SELECT id, topic_ids FROM blog_circle_post " +
            "WHERE topic_ids IS NOT NULL AND topic_ids != '' AND topic_ids != '[]' " +
            "AND status = 1 AND is_deleted = 0" +
            "</script>")
    java.util.List<java.util.Map<String, Object>> selectTopicIdsRaw();

    default java.util.Map<Long, Long> countByTopicIds(java.util.List<Long> topicIds) {
        if (topicIds == null || topicIds.isEmpty()) return new java.util.HashMap<>();
        java.util.Map<Long, Long> result = new java.util.HashMap<>();
        for (Long id : topicIds) result.put(id, 0L);
        java.util.List<java.util.Map<String, Object>> rows = selectTopicIdsRaw();
        for (java.util.Map<String, Object> row : rows) {
            String json = (String) row.get("topic_ids");
            if (json == null || json.isEmpty()) continue;
            try {
                json = json.trim();
                if (json.startsWith("[") && json.endsWith("]")) {
                    String inner = json.substring(1, json.length() - 1);
                    if (inner.isEmpty()) continue;
                    for (String s : inner.split(",")) {
                        s = s.trim();
                        if (s.isEmpty()) continue;
                        if (s.startsWith("\"") && s.endsWith("\"")) s = s.substring(1, s.length() - 1);
                        try {
                            Long tid = Long.parseLong(s);
                            if (result.containsKey(tid)) result.put(tid, result.get(tid) + 1);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            } catch (Exception ignored) {}
        }
        return result;
    }
}