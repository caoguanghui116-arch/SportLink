package com.mashang.registrationservice.domain.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TeamEntryVo {

    private Long teamEntryId;
    private String teamName;
    private Long itemId;
    private String itemName;
    private Long meetingId;
    private String meetingName;
    private Long captainId;
    private String captainName;
    private Long status;
    private Integer maxMembers;
    private Integer currentMembers;
    private Date createTime;
    private List<TeamMemberVo> members;
}
