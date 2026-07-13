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
 * 动态发布/编辑参数对象
 *
 * <p>用于用户在运动会社交圈发布或编辑动态（帖子）时接收前端数据。
 * 用户可以在动态中分享文字内容和图片链接，与运动会关联。
 *
 * <p>设计要点：
 * <ul>
 *   <li>{@code meetingId} 为 Long 类型，使用 {@code @NotNull} 校验（Long 类型不支持 @NotBlank），
 *       用于将动态关联到指定运动会，便于按运动会筛选动态</li>
 *   <li>{@code content} 为动态正文内容，使用 {@code @NotBlank} 校验确保不为空，
 *       同时使用 {@code @Size(max = 2000)} 限制最大长度，防止恶意超长输入占用存储</li>
 *   <li>{@code imageUrl} 为可选的图片链接字段，使用 {@code @Size(max = 500)}
 *       限制 URL 最大长度，防止恶意长 URL 攻击</li>
 * </ul>
 *
 * @author mashang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "动态发布参数")
public class PostQuery {

    /**
     * 运动会ID
     * <p>外键，标识该动态属于哪届运动会。前端按运动会筛选动态时需要。
     * 使用 @NotNull 校验（Long 类型无法使用 @NotBlank）。
     */
    @ApiModelProperty(value = "运动会id", required = true)
    @NotNull(message = "运动会ID不能为空")
    private Long meetingId;

    /**
     * 动态内容
     * <p>用户发布的文字内容，支持纯文本（如需富文本由前端处理）。
     * 使用 @NotBlank 确保内容不为空，@Size(max = 2000) 限制最大 2000 个字符。
     */
    @ApiModelProperty(value = "动态内容", required = true)
    @NotBlank(message = "动态内容不能为空")
    @Size(max = 2000, message = "动态内容不能超过2000个字符")
    private String content;

    /**
     * 图片链接
     * <p>可选的图片URL，用户上传的配图链接。
     * 非必填字段，但若有值则限制 @Size(max = 500) 防止 URL 过长。
     */
    @ApiModelProperty(value = "图片链接")
    @Size(max = 500, message = "图片链接不能超过500个字符")
    private String imageUrl;
}
