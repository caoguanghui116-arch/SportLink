package com.mashang.userservice.domain.query.create;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@ApiModel("添加团队请求参数")
public class TeamCreateQuery {

    @ApiModelProperty("团队名称")
    @NotBlank(message = "团队名称不能为空")
    private String teamName;

    @ApiModelProperty("院系名称")
    @NotNull(message = "院系名称不能为空")
    private Long deptId;

    @ApiModelProperty("团队队长id")
    @NotNull(message = "团队队长id不能为空")
    private Long captainId;
}
