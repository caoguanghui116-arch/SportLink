package com.mashang.userservice.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("裁判响应参数")
public class RefereeVo {

    @ApiModelProperty("裁判id")
    private Long userId;

    @ApiModelProperty("裁判名称")
    private String username;

}
