package com.mashang.socialservice.domain.query.create;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "动态发布参数")
public class PostQuery {

    @ApiModelProperty(value = "运动会id", required = true)
    @NotNull(message = "运动会id不能为空")
    private Long meetingId;

    @ApiModelProperty(value = "内容", required = true)
    @NotNull(message = "内容不能为空")
    private String content;

    @ApiModelProperty(value = "图片链接")
    private String imageUrl;

}
