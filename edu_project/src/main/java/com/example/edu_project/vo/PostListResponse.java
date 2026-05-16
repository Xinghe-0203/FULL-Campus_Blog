package com.example.edu_project.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章列表响应 VO
 */
@Data
@Schema(description = "文章列表响应")
public class PostListResponse {

    @Schema(description = "文章ID")
    private Long id;

    @Schema(description = "作者ID")
    private Long userId;

    @Schema(description = "作者昵称")
    private String nickname;

    @Schema(description = "作者头像")
    private String avatar;

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "文章摘要")
    private String summary;

    @Schema(description = "话题ID")
    private Long topicId;

    @Schema(description = "话题名称")
    private String topicName;

    @Schema(description = "文章分类")
    private String category;

    @Schema(description = "阅读量")
    private Long viewCount;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "收藏数")
    private Integer collectCount;

    @Schema(description = "分享数")
    private Integer shareCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "标签列表")
    private List<TagVO> tags;

    @Schema(description = "文章封面图")
    private String coverImage;

    @Schema(description = "作者用户名")
    private String username;

    @Schema(description = "标签信息（同 PostDetailResponse.TagVO）")
    public static class TagVO {
        private Long id;
        private String name;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
