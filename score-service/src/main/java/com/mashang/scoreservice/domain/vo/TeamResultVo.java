package com.mashang.scoreservice.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel("团体成绩响应参数")
public class TeamResultVo {

    @ApiModelProperty("团体成绩id")
    private Long teamResultId;

    @ApiModelProperty("团体报名id")
    private Long teamEntryId;

    @ApiModelProperty("团队名称")
    private String teamName;

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
