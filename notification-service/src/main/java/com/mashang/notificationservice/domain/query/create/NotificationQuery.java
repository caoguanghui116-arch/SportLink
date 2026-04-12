package com.mashang.notificationservice.domain.query.create;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "消息通知发送参数")
public class NotificationQuery {

    @ApiModelProperty("接收用户ID")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @ApiModelProperty("通知标题")
    @NotBlank(message = "通知标题不能为空")
    private String title;

    @ApiModelProperty("通知内容")
    @NotBlank(message = "通知内容不能为空")
    private String content;

    @ApiModelProperty("通知类型（1=系统通知, 2=比赛提醒, 3=成绩通知）")
    @NotNull(message = "通知类型不能为空")
    private Long type;

    @ApiModelProperty("关联ID（可为空，如赛程ID）")
    private Long relatedId;

}
