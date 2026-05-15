package com.example.edu_project.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章/帖子实体类 (blog_post)
 */
@Data
@TableName("blog_post")
@Schema(description = "文章信息")
public class BlogPost implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "文章ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "作者用户ID")
    private Long userId;

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "文章摘要")
    private String summary;

    @Schema(description = "文章内容")
    private String content;

    @Schema(description = "文章分类")
    private String category;

    @Schema(description = "封面图URL")
    // NOTE: 字段名为 coverUrl，但 DTO/VO 中使用 coverImage。重命名会影响 SQL 列名，暂时保持不一致。
    private String coverUrl;

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

    @Schema(description = "文章状态：0=待审核，1=已发布，2=已驳回")
    private Integer status;

    @Schema(description = "审核人ID")
    private Long reviewerId;

    @Schema(description = "审核时间")
    private LocalDateTime reviewTime;

    @Schema(description = "驳回原因")
    private String rejectReason;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "逻辑删除：0=正常，1=删除")
    @TableLogic
    private Integer isDeleted;
}
