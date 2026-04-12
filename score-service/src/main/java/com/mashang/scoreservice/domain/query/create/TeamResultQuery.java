package com.mashang.scoreservice.domain.query.create;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "团体成绩录入参数")
public class TeamResultQuery {

    @ApiModelProperty(value = "团体报名id", required = true)
    @NotNull(message = "团体报名id不能为空")
    private Long teamEntryId;

    @ApiModelProperty(value = "项目id", required = true)
    @NotNull(message = "项目id不能为空")
    private Long itemId;

    @ApiModelProperty(value = "运动会id", required = true)
    @NotNull(message = "运动会id不能为空")
    private Long meetingId;

    @ApiModelProperty(value = "团体成绩/分数", required = true)
    @NotNull(message = "团体成绩不能为空")
    private BigDecimal score;

}
