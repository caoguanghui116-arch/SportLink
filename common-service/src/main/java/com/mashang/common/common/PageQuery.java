package com.mashang.common.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel("分页参数")
public class PageQuery {

    @ApiModelProperty(value = "当前记录起始索引",required = true)
    @NotNull(message = "当前记录起始索引不能为空")
    private Integer pageNum;

    @ApiModelProperty(value = "每页显示记录数",required = true)
    @NotNull(message = "每页显示记录数不能为空")
    private Integer pageSize;


}