package com.mashang.registrationservice.domain.query.create;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class TeamEntryQuery {

    @NotBlank(message = "团队名称不能为空")
    private String teamName;

    @NotNull(message = "项目ID不能为空")
    private Long itemId;

    @NotNull(message = "运动会ID不能为空")
    private Long meetingId;

    @NotNull(message = "队长ID不能为空")
    private Long captainId;

    private Integer maxMembers;
}
