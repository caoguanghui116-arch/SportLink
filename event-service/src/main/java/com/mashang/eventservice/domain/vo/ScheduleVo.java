package com.mashang.eventservice.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Date;

@Data
@ApiModel("赛程信息分页响应参数")
public class ScheduleVo {

    @ApiModelProperty("项目id")
    private Long itemId;

    @ApiModelProperty("项目名称")
    private String itemName;

    @ApiModelProperty(value = "比赛时间")
    private Date gameTime;

    @ApiModelProperty("场地")
    private String mainVenue;

    @ApiModelProperty("场地id")
    private Long venueId;

    @ApiModelProperty("组别")
    private String group;

    @ApiModelProperty("项目类型")
    private String itemType;

    @ApiModelProperty("裁判id")
    private Long refereeId;

}
