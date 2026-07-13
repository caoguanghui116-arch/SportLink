package com.mashang.notificationservice.domain.query.create;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 消息通知发送参数对象
 *
 * <p>用于系统内部服务或管理员后台发送消息通知时接收数据。
 * 通知可以定向发送给指定用户（userId），指定通知类型（type），
 * 并可关联业务对象（relatedId），实现"点击通知跳转到相关页面"的效果。
 *
 * <p>设计要点：
 * <ul>
 *   <li>{@code userId} 为接收通知的目标用户ID（Long 类型），
 *       使用 {@code @NotNull} 校验（Long 类型不支持 @NotBlank）</li>
 *   <li>{@code title} 和 {@code content} 为字符串类型，使用 {@code @NotBlank} 校验，
 *       title 用于消息列表摘要展示，content 为通知正文</li>
 *   <li>{@code type} 为 Long 类型的通知类型标识，
 *       使用 {@code @NotNull} 校验（Long 类型不支持 @NotBlank），
 *       取值约定：1=系统通知，2=比赛提醒，3=成绩通知。
 *       使用数字类型而非字符串枚举，便于扩展和数据库索引</li>
 *   <li>{@code relatedId} 为可选的关联业务ID（如赛程ID、成绩ID），
 *       前端点击通知后根据 type + relatedId 跳转到对应页面，
 *       非必填字段，无校验注解</li>
 * </ul>
 *
 * @author mashang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "消息通知发送参数")
public class NotificationQuery {

    /**
     * 接收用户ID
     * <p>通知的目标用户，系统将向该用户推送此通知。
     * 使用 @NotNull 校验（Long 类型无法使用 @NotBlank）。
     */
    @ApiModelProperty("接收用户ID")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 通知标题
     * <p>在消息列表中作为摘要展示，建议控制在 20 字以内。
     * 使用 @NotBlank 校验，不允许为 null 或空字符串。
     */
    @ApiModelProperty("通知标题")
    @NotBlank(message = "通知标题不能为空")
    private String title;

    /**
     * 通知内容
     * <p>通知的正文详情，在用户点击进入通知详情页时展示。
     * 使用 @NotBlank 校验，不允许为 null 或空字符串。
     */
    @ApiModelProperty("通知内容")
    @NotBlank(message = "通知内容不能为空")
    private String content;

    /**
     * 通知类型
     * <p>Long 类型标识（使用 @NotNull 校验，Long 不支持 @NotBlank）。
     * 取值约定：
     * 1 = 系统通知（管理员群发、系统维护等），
     * 2 = 比赛提醒（赛前通知运动员），
     * 3 = 成绩通知（成绩录入后通知相关运动员）。
     * 前端可根据 type 决定展示图标和跳转行为。
     */
    @ApiModelProperty("通知类型（1=系统通知, 2=比赛提醒, 3=成绩通知）")
    @NotNull(message = "通知类型不能为空")
    private Long type;

    /**
     * 关联业务ID（可选）
     * <p>指向通知关联的业务实体ID，如赛程ID、报名ID、成绩ID等。
     * 前端点击通知时，根据 type + relatedId 组合跳转到对应的业务页面。
     * 非必填字段，无校验注解。
     */
    @ApiModelProperty("关联ID（可为空，如赛程ID）")
    private Long relatedId;

}
