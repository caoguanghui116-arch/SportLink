package com.mashang.eventservice.domain.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("项目响应参数")
public class EventItemVo {

    @ApiModelProperty("项目id")
    private Long itemId;

    @ApiModelProperty("项目名称")
    private String itemName;
}
