package com.mashang.registrationservice.domain.vo;

import lombok.Data;

import java.util.Date;

@Data
public class PersonalEntryVo {

    private Long entryId;
    private Long userId;
    private String username;
    private Long itemId;
    private String itemName;
    private Long meetingId;
    private String meetingName;
    private Long status;
    private Date createTime;
}
