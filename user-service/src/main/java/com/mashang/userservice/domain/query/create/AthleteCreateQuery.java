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
import javax.validation.constraints.Pattern;
import java.util.Date;
@Data
@ApiModel("添加运动员请求参数")

public class AthleteCreateQuery {

    @ApiModelProperty("所属上级部门id")
    @NotNull(message = "上级部门不能为空")
    private Long deptId;

    @ApiModelProperty("用户id")
    @NotNull(message = "用户id不能为空")
    private Long userId;

    @NotBlank(message = "学号不能为空")
    @ApiModelProperty("学号")
    private String stuNo;

    @NotBlank(message = "姓名不能为空")
    @ApiModelProperty("姓名")
    private String name;

    @NotBlank(message = "性别不能为空")
    @ApiModelProperty("性别  M/F")
    private String gender;

    @Pattern(regexp = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(17[013678])|(18[0,5-9]))\\\\d{8}$",message = "手机号码格式有误")
    @ApiModelProperty("性别  M/F")
    private String phone;
}
