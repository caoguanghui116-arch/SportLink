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
@ApiModel(description = "评论添加参数")
public class CommentQuery {

    @ApiModelProperty(value = "动态id", required = true)
    @NotNull(message = "动态id不能为空")
    private Long postId;

    @ApiModelProperty(value = "评论内容", required = true)
    @NotNull(message = "内容不能为空")
    private String content;

    @ApiModelProperty(value = "父评论id(回复评论)")
    private Long parentId;

}
