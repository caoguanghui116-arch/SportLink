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
public class TeamEntry {

    @TableId(type = IdType.AUTO)
    private Long teamEntryId;
    private String teamName;
    private Long itemId;
    private Long meetingId;
    private Long captainId;
    private Long status;
    private Integer maxMembers;
    private Date createTime;
    private Date updateTime;
    private Long delFlag;
}
