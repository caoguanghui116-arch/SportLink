package com.mashang.userservice.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor

/**
 * 公告实体
 */
public class Announcement {

    @TableId(type = IdType.AUTO)
    private Long annoId;
    private String title;
    private String content;
    private Long meetingId;
    private Long publishUserId;
    private Long status;
    private Date createTime;
    private Date updateTime;
    private Long delFlag;
}
