package com.mashang.userservice.domain.query.update;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("编辑院系请求参数")
public class DepartmentUpdateQuerry {

    @ApiModelProperty("院系id")
    @NotNull(message = "院系id不能为空")
    private Long deptId;

    @ApiModelProperty("院系id")
    @NotNull(message = "上级部门不能为空")
    private Long parentId;

    @ApiModelProperty("院系名称")
    @NotBlank(message = "院系名称不能为空")
    private String deptName;

    @ApiModelProperty("排序")
    private Integer sort;
}
