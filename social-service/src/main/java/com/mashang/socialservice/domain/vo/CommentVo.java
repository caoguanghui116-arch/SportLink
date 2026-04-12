package com.mashang.socialservice.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@ApiModel("评论响应参数")
public class CommentVo {

    @ApiModelProperty("评论id")
    private Long commentId;

    @ApiModelProperty("动态id")
    private Long postId;

    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("父评论id")
    private Long parentId;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("点赞数")
    private Long likeCount;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("子回复列表")
    private List<CommentVo> replies;

}
