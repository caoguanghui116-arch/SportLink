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
@TableName("`comment`")
public class Comment {

  @TableId(type = IdType.AUTO)
  private Long commentId;
  private Long postId;
  private Long userId;
  private Long parentId;
  private String content;
  private Long likeCount;
  @TableField(fill = FieldFill.INSERT)
  private Date createTime;
  private Long delFlag;

}
