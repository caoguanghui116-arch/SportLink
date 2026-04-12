package com.mashang.scoreservice.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel("个人成绩响应参数")
public class PersonalResultVo {

    @ApiModelProperty("成绩id")
    private Long resultId;

    @ApiModelProperty("报名记录id")
    private Long entryId;

    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("项目id")
    private Long itemId;

    @ApiModelProperty("项目名称")
    private String itemName;

    @ApiModelProperty("成绩")
    private BigDecimal score;

    @ApiModelProperty("排名")
    private Long rank;

    @ApiModelProperty("创建时间")
    private Date createTime;

}
