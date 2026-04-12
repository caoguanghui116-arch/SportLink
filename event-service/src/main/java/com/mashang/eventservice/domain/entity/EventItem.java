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
public class EventItem {

  @TableId(type = IdType.AUTO)
  private Long itemId;
  private Long meetingId;
  private Long categoryId;
  private String itemName;
  private String itemType;
  private String itemRule;
  private Long sexLimit;
  private Long maxEntry;
  private Long status;
  @TableField(fill = FieldFill.INSERT)
  private Date createTime;
  @TableField(fill = FieldFill.INSERT_UPDATE)
  private Date updateTime;
  private Long delFlag;



}
