package com.mashang.socialservice.domain.query.create;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 评论创建参数对象
 *
 * <p>用于用户在动态下方发表评论或回复他人的评论时接收前端数据。
 * 支持两级评论结构：一级评论（parentId 为空/null）和子回复（parentId 指向父评论ID）。
 *
 * <p>设计要点：
 * <ul>
 *   <li>{@code postId} 为 Long 类型，使用 {@code @NotNull} 校验（Long 类型不支持 @NotBlank），
 *       用于关联评论到目标动态</li>
 *   <li>{@code content} 为评论内容，使用 {@code @NotBlank} 校验确保不为空，
 *       使用 {@code @Size(max = 500)} 限制最大长度，评论通常比动态内容短</li>
 *   <li>{@code parentId} 为非必填字段，用于实现评论的嵌套回复：
 *       null 表示一级评论，非 null 表示对某条评论的回复</li>
 * </ul>
 *
 * @author mashang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "评论添加参数")
public class CommentQuery {

    /**
     * 动态ID
     * <p>外键，关联 post 表，标识该评论属于哪条动态。
     * 使用 @NotNull 校验（Long 类型无法使用 @NotBlank）。
     */
    @ApiModelProperty(value = "动态id", required = true)
    @NotNull(message = "动态ID不能为空")
    private Long postId;

    /**
     * 评论内容
     * <p>用户发表的评论文字。使用 @NotBlank 确保不为空，
     * @Size(max = 500) 限制最大 500 个字符。
     */
    @ApiModelProperty(value = "评论内容", required = true)
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过500个字符")
    private String content;

    /**
     * 父评论ID（回复评论时使用）
     * <p>非必填字段。为 null 时表示这是一条一级评论（直接评论动态）；
     * 有值时表示这是对某条评论的回复（二级评论），值指向被回复的评论ID。
     */
    @ApiModelProperty(value = "父评论id(回复评论时使用)")
    private Long parentId;
}
