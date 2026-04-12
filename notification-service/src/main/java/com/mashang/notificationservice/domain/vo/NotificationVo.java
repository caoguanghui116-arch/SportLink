package com.mashang.notificationservice.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("消息通知响应参数")
public class NotificationVo {

    @ApiModelProperty("通知ID")
    private Long notificationId;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("通知标题")
    private String title;

    @ApiModelProperty("通知内容")
    private String content;

    @ApiModelProperty("通知类型（1=系统通知, 2=比赛提醒, 3=成绩通知）")
    private Long type;

    @ApiModelProperty("关联ID")
    private Long relatedId;

    @ApiModelProperty("是否已读（0=未读, 1=已读）")
    private Long isRead;

    @ApiModelProperty("创建时间")
    private Date createTime;

}
