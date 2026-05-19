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

    @Select("SELECT COUNT(*) FROM blog_circle_post WHERE JSON_SEARCH(topic_ids, 'one', #{topicId}) IS NOT NULL AND status = 1 AND is_deleted = 0")
    Long countByTopicId(@Param("topicId") Long topicId);

    @Select("<script>" +
            "SELECT jt.topicId, COUNT(*) as cnt FROM blog_circle_post " +
            "CROSS JOIN JSON_TABLE(topic_ids, '$[*]' COLUMNS(topicId BIGINT PATH '$')) AS jt " +
            "WHERE jt.topicId IN (<foreach collection='topicIds' item='id' separator=','>#{id}</foreach>) " +
            "AND status = 1 AND is_deleted = 0 GROUP BY jt.topicId" +
            "</script>")
    java.util.Map<Long, Long> countByTopicIds(@Param("topicIds") java.util.List<Long> topicIds);
}