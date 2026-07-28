package com.mashang.userservice.domain.query.create;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("添加院系请求参数")
public class DepartmentCreateQuerry {

    @ApiModelProperty("院系id")
    @NotNull(message = "上级部门不能为空")
    private Long parentId;

    @ApiModelProperty("院系名称")
    @NotBlank(message = "院系名称不能为空")
    private String deptName;

    @ApiModelProperty("排序")
    private Integer sort;
}
