package com.mashang.socialservice.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("`like`")
public class Like {

  @TableId(type = IdType.AUTO)
  private Long likeId;
  private Long targetType;
  private Long targetId;
  private Long userId;
  @TableField(fill = FieldFill.INSERT)
  private Date createTime;

}
