package com.mashang.scoreservice.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("奖项响应参数")
public class AwardVo {

    @ApiModelProperty("奖项id")
    private Long awardId;

    @ApiModelProperty("运动会id")
    private Long meetingId;

    @ApiModelProperty("项目名称")
    private String itemName;

    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("团体报名id")
    private Long teamEntryId;

    @ApiModelProperty("团队名称")
    private String teamName;

    @ApiModelProperty("奖项名称")
    private String awardName;

    @ApiModelProperty("奖项等级")
    private String awardLevel;

}
