package com.mashang.eventservice.domain.query.select;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 赛程分页查询参数 —— 支持按运动会ID和时间范围筛选。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "赛程信息条件查询参数")
public class SchedulePageQuery {

    /** 运动会ID：用于按运动会维度缓存赛程数据 */
    @ApiModelProperty(value = "运动会ID")
    private Long meetingId;

    /** 比赛日期范围 —— 开始时间（格式：yyyy-MM-dd HH:mm:ss） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "日期范围--开始时间")
    private Date startGameTime;

    /** 比赛日期范围 —— 结束时间（格式：yyyy-MM-dd HH:mm:ss） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "日期范围-结束时间")
    private Date endGameTime;
}
