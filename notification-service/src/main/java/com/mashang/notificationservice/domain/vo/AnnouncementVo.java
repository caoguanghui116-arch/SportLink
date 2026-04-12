package com.mashang.notificationservice.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("公告响应参数")
public class AnnouncementVo {

    @ApiModelProperty("公告ID")
    private Long announcementId;

    @ApiModelProperty("公告标题")
    private String title;

    @ApiModelProperty("公告内容")
    private String content;

    @ApiModelProperty("所属运动会ID")
    private Long meetingId;

    @ApiModelProperty("发布者ID")
    private Long publisherId;

    @ApiModelProperty("状态（0=草稿, 1=已发布）")
    private Long status;

    @ApiModelProperty("创建时间")
    private Date createTime;

}
