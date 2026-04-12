package com.mashang.socialservice.domain.entity;

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
public class BulletChat {

  @TableId(type = IdType.AUTO)
  private Long chatId;
  private Long meetingId;
  private Long userId;
  private String content;
  @TableField(fill = FieldFill.INSERT)
  private Date createTime;

}
