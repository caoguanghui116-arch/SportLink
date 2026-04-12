package com.mashang.scoreservice.domain.query.create;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "奖项添加参数")
public class AwardQuery {

    @ApiModelProperty(value = "运动会id", required = true)
    @NotNull(message = "运动会id不能为空")
    private Long meetingId;

    @ApiModelProperty(value = "项目id", required = true)
    @NotNull(message = "项目id不能为空")
    private Long itemId;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "团体报名id")
    private Long teamEntryId;

    @ApiModelProperty(value = "奖项名称", required = true)
    @NotBlank(message = "奖项名称不能为空")
    private String awardName;

    @ApiModelProperty(value = "奖项等级", required = true)
    @NotBlank(message = "奖项等级不能为空")
    private String awardLevel;

}
