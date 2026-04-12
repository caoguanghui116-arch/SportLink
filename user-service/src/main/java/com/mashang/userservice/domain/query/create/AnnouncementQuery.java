package com.mashang.userservice.domain.query.create;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AnnouncementQuery {

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private Long meetingId;
}
