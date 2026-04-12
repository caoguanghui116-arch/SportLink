package com.mashang.registrationservice.domain.vo;

import lombok.Data;

import java.util.Date;

@Data
public class TeamMemberVo {

    private Long memberId;
    private Long teamEntryId;
    private Long userId;
    private String username;
    private Date createTime;
}
