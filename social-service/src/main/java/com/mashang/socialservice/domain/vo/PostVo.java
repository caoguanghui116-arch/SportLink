package com.mashang.socialservice.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@ApiModel("动态响应参数")
public class PostVo {

    @ApiModelProperty("动态id")
    private Long postId;

    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("运动会id")
    private Long meetingId;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("图片链接")
    private String imageUrl;

    @ApiModelProperty("点赞数")
    private Long likeCount;

    @ApiModelProperty("评论数")
    private Long commentCount;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("评论列表")
    private List<CommentVo> comments;

}
