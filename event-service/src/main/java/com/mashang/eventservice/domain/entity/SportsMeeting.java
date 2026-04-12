package com.mashang.eventservice.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SportsMeeting {

  @TableId(type = IdType.AUTO)
  private Long meetingId;
  private Long meetingSession;
  private String meetingName;
  private Date startTime;
  private Date endTime;
  private Date signStartTime;
  private Date signEndTime;
  private String mainVenue;
  private String posterUrl;
  private Long status;
  @TableField(fill = FieldFill.INSERT)
  private Date createTime;
  @TableField(fill = FieldFill.INSERT_UPDATE)
  private Date updateTime;
  private Long delFlag;

}
