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
public class EventCategory {

    @TableId(type = IdType.AUTO)
    private Long categoryId;
    private Long meetingId;
    private String categoryName;
    private Integer sortOrder;
    private Long status;
    private Date createTime;
    private Date updateTime;
    private Long delFlag;
}
