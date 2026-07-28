package com.mashang.userservice.domain.query.update;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("修改公告请求参数")
public class AnnouncementUpdateQuery {

    @ApiModelProperty("公告id")
    @NotNull(message = "公告id不能为空")
    private Long annoId;

    @NotBlank(message = "标题不能为空")
    @ApiModelProperty("标题")
    private String title;

    @NotBlank(message = "内容不能为空")
    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("运动会届数")
    private Long meetingId;

}
