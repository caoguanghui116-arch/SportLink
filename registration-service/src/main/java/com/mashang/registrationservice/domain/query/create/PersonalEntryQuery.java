package com.mashang.registrationservice.domain.query.create;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PersonalEntryQuery {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "项目ID不能为空")
    private Long itemId;

    @NotNull(message = "运动会ID不能为空")
    private Long meetingId;
}
