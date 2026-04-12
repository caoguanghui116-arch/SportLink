package com.mashang.registrationservice.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonalEntry {

    @TableId(type = IdType.AUTO)
    private Long entryId;
    private Long userId;
    private Long itemId;
    private Long meetingId;
    private Long status;
    private Date createTime;
    private Date updateTime;
    private Long delFlag;
}
