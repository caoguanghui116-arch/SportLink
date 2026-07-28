package com.mashang.userservice.domain.query.update;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("添加团队请求参数")
public class TeamUpdateQuery {

    @ApiModelProperty("团队id")
    @NotNull(message = "团队id不能为空")
    private Long teamId;

    @ApiModelProperty("团队名称")
    @NotBlank(message = "团队名称不能为空")
    private String teamName;

    @ApiModelProperty("院系id")
    @NotNull(message = "院系id不能为空")
    private Long deptId;

    @ApiModelProperty("团队队长id")
    @NotNull(message = "团队队长id不能为空")
    private Long captainId;
}
