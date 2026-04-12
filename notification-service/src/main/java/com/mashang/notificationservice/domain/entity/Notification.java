package com.mashang.notificationservice.domain.entity;

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
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long notificationId;
    private Long userId;
    private String title;
    private String content;
    private Long type;
    private Long relatedId;
    private Long isRead;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    private Long delFlag;

}
