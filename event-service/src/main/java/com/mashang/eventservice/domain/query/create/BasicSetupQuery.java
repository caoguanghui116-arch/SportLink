package com.mashang.eventservice.domain.query.create;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "赛事信息添加参数")
public class BasicSetupQuery {

    @ApiModelProperty("运动会届数")
    @NotNull(message = "运动会届数不能为空")
    private Long meetingSession;

    @ApiModelProperty("运动会名称")
    @NotBlank(message = "运动会名称不能为空")
    private String meetingName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty("运动会开始时间")
    @NotBlank(message = "运动会开始时间不能为空")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty("运动会结束时间")
    @NotBlank(message = "运动会结束时间不能为空")
    private Date endTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty("开始报名时间")
    @NotBlank(message = "开始报名时间不能为空")
    private Date signStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty("截至报名时间")
    @NotBlank(message = "截至报名时间不能为空")
    private Date signEndTime;

    @ApiModelProperty("主场地")
    @NotBlank(message = "主场地不能为空")
    private String mainVenue;

}
