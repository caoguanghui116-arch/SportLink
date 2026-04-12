package com.mashang.notificationservice.domain.query.create;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "公告发布/修改参数")
public class AnnouncementQuery {

    @ApiModelProperty("公告标题")
    @NotBlank(message = "公告标题不能为空")
    private String title;

    @ApiModelProperty("公告内容")
    @NotBlank(message = "公告内容不能为空")
    private String content;

    @ApiModelProperty("所属运动会ID（可为空，null表示系统公告）")
    private Long meetingId;

}
