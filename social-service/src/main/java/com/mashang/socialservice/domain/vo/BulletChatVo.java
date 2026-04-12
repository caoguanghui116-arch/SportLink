package com.mashang.socialservice.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("弹幕响应参数")
public class BulletChatVo {

    @ApiModelProperty("弹幕id")
    private Long chatId;

    @ApiModelProperty("运动会id")
    private Long meetingId;

    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("创建时间")
    private Date createTime;

}
