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
public class TeamMember {

    @TableId(type = IdType.AUTO)
    private Long memberId;
    private Long teamEntryId;
    private Long userId;
    private Date createTime;
    private Long delFlag;
}
