package com.mashang.eventservice.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

@Data
@ApiModel("场地响应参数")
public class VenueVo {

    @ApiModelProperty("场地id")
    private Long venueId;

    @ApiModelProperty("场地名称")
    private String venueName;

    @ApiModelProperty("场地类型")
    private String venueType;

    @ApiModelProperty("场地状态")
    private Long status;
}
