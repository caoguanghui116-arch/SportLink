package com.mashang.eventservice.domain.query.create;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "项目信息添加参数")
public class EventItemQuery {

    @ApiModelProperty("所属运动会ID")
    @NotNull(message = "所属运动会不能为空")
    private Long meetingId;

    @ApiModelProperty("项目分类ID")
    @NotNull(message = "项目分类不能为空")
    private Long categoryId;

    @ApiModelProperty("项目名称")
    @NotBlank(message = "项目名称不能为空")
    private String itemName;

    @ApiModelProperty("项目类型（径赛 / 田赛 / 团体）")
    @NotBlank(message = "项目类型不能为空")
    private String itemType;

    @ApiModelProperty("项目规则")
    @NotBlank(message = "项目规则不能为空")
    private String itemRule;

    @ApiModelProperty("性别限制（男子/女子/不限）")
    @NotNull(message = "性别限制不能为空")
    private Long sexLimit;

    @ApiModelProperty("限报人数")
    @NotNull(message = "限报人数不能为空")
    @Range(min = 1, message = "限报人数不能小于1")
    private Long maxEntry;
}
