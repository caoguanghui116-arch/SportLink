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
 * 弹幕发送参数对象
 *
 * <p>用于用户在运动会直播/回放页面发送弹幕消息时接收前端数据。
 * 弹幕内容以滚动字幕形式在视频/页面中展示，是运动会互动的轻量级社交功能。
 *
 * <p>设计要点：
 * <ul>
 *   <li>{@code meetingId} 为 Long 类型，使用 {@code @NotNull} 校验（Long 类型不支持 @NotBlank），
 *       用于将弹幕关联到指定运动会，不同运动会的弹幕互相隔离</li>
 *   <li>{@code content} 为弹幕内容，使用 {@code @NotBlank} 确保不为空，
 *       使用 {@code @Size(max = 200)} 限制最大长度 —— 弹幕通常为短文本，
 *       200 字符的上限比动态（2000）和评论（500）更小，符合弹幕即时短小的特性</li>
 * </ul>
 *
 * @author mashang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "弹幕发送参数")
public class BulletChatQuery {

    /**
     * 运动会ID
     * <p>外键，标识该弹幕属于哪届运动会的直播/回放。
     * 使用 @NotNull 校验（Long 类型无法使用 @NotBlank）。
     */
    @ApiModelProperty(value = "运动会id", required = true)
    @NotNull(message = "运动会ID不能为空")
    private Long meetingId;

    /**
     * 弹幕内容
     * <p>用户发送的弹幕文字。使用 @NotBlank 确保不为空，
     * @Size(max = 200) 限制最大 200 个字符，符合弹幕短小精悍的特点。
     */
    @ApiModelProperty(value = "弹幕内容", required = true)
    @NotBlank(message = "弹幕内容不能为空")
    @Size(max = 200, message = "弹幕内容不能超过200个字符")
    private String content;
}
