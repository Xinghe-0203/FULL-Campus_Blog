package com.example.edu_project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.edu_project.entity.BlogPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface BlogPostMapper extends BaseMapper<BlogPost> {

    @Select("SELECT COUNT(DISTINCT user_id) FROM blog_post WHERE create_time >= #{since} AND is_deleted = 0")
    Long countDistinctAuthorsSince(@Param("since") LocalDateTime since);

    @Update("UPDATE blog_post SET view_count = CASE WHEN view_count < 100000000 THEN view_count + 1 ELSE view_count END WHERE id = #{id} AND is_deleted = 0")
    void incrementViewCount(@Param("id") Long id);

    @Update("UPDATE blog_post SET like_count = CASE WHEN like_count < 100000000 THEN like_count + 1 ELSE like_count END WHERE id = #{id} AND is_deleted = 0")
    void incrementLikeCount(@Param("id") Long id);

    @Update("UPDATE blog_post SET like_count = like_count - 1 WHERE id = #{id} AND is_deleted = 0 AND like_count > 0")
    void decrementLikeCount(@Param("id") Long id);

    @Update("UPDATE blog_post SET comment_count = CASE WHEN comment_count < 100000000 THEN comment_count + 1 ELSE comment_count END WHERE id = #{id} AND is_deleted = 0")
    void incrementCommentCount(@Param("id") Long id);

    @Update("UPDATE blog_post SET comment_count = comment_count - #{count} WHERE id = #{id} AND is_deleted = 0 AND comment_count >= #{count}")
    void decrementCommentCount(@Param("id") Long id, @Param("count") int count);

    @Update("UPDATE blog_post SET collect_count = CASE WHEN collect_count < 100000000 THEN collect_count + 1 ELSE collect_count END WHERE id = #{id} AND is_deleted = 0")
    void incrementCollectCount(@Param("id") Long id);

    @Update("UPDATE blog_post SET collect_count = collect_count - 1 WHERE id = #{id} AND is_deleted = 0 AND collect_count > 0")
    void decrementCollectCount(@Param("id") Long id);

    @Update("UPDATE blog_post SET share_count = CASE WHEN share_count < 100000000 THEN share_count + 1 ELSE share_count END WHERE id = #{id} AND is_deleted = 0")
    void incrementShareCount(@Param("id") Long id);

    @Update("UPDATE blog_post SET share_count = share_count - 1 WHERE id = #{id} AND is_deleted = 0 AND share_count > 0")
    void decrementShareCount(@Param("id") Long id);

    /**
     * 批量统计每日新增文章数（避免 N+1 查询）
     * @param since 起始时间
     * @return 每日文章数和日期的映射列表
     */
    @Select("SELECT DATE(create_time) as date, COUNT(*) as count FROM blog_post " +
            "WHERE create_time >= #{since} AND is_deleted = 0 GROUP BY DATE(create_time) ORDER BY date")
    List<Map<String, Object>> countPostsGroupByDate(@Param("since") LocalDateTime since);

    @Select("SELECT id FROM blog_post " +
            "WHERE is_deleted = #{isDeleted} AND status = #{status} " +
            "AND MATCH(title, content) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) " +
            "ORDER BY create_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<Long> fullTextSearch(@Param("keyword") String keyword,
                              @Param("status") Integer status,
                              @Param("isDeleted") Integer isDeleted,
                              @Param("offset") Integer offset,
                              @Param("limit") Integer limit);

    @Select("SELECT id FROM blog_post " +
            "WHERE is_deleted = #{isDeleted} AND status = #{status} " +
            "AND MATCH(title, content) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) " +
            "ORDER BY view_count DESC LIMIT #{limit} OFFSET #{offset}")
    List<Long> fullTextSearchByView(@Param("keyword") String keyword,
                                    @Param("status") Integer status,
                                    @Param("isDeleted") Integer isDeleted,
                                    @Param("offset") Integer offset,
                                    @Param("limit") Integer limit);

    @Select("SELECT id FROM blog_post " +
            "WHERE is_deleted = #{isDeleted} AND status = #{status} " +
            "AND MATCH(title, content) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) " +
            "ORDER BY like_count DESC LIMIT #{limit} OFFSET #{offset}")
    List<Long> fullTextSearchByLike(@Param("keyword") String keyword,
                                    @Param("status") Integer status,
                                    @Param("isDeleted") Integer isDeleted,
                                    @Param("offset") Integer offset,
                                    @Param("limit") Integer limit);

    @Select("SELECT COUNT(*) FROM blog_post " +
            "WHERE is_deleted = #{isDeleted} AND status = #{status} " +
            "AND MATCH(title, content) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE)")
    Long countFullTextSearch(@Param("keyword") String keyword,
                             @Param("status") Integer status,
                             @Param("isDeleted") Integer isDeleted);
}
