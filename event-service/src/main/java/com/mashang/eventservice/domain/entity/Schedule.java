package com.mashang.eventservice.domain.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Schedule {

  @TableId(type = IdType.AUTO)
  private Long scheduleId;
  private Long itemId;
  private Long venueId;
  private Long refereeId;
  private Date gameTime;
  private String group;
  private Long status;

}
