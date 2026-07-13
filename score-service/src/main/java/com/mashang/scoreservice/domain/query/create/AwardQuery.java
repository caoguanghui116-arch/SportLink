package com.mashang.scoreservice.domain.query.create;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 奖项创建/编辑参数对象
 *
 * <p>用于管理员在运动会中创建或编辑奖项时接收前端表单数据。
 * 一个奖项可以颁发给个人运动员（通过 userId 关联）或团队（通过 teamEntryId 关联），
 * 两个用户关联字段至少填写其一（具体业务校验在 Service 层）。
 *
 * <p>设计要点：
 * <ul>
 *   <li>{@code meetingId}、{@code itemId}、{@code userId}、{@code teamEntryId}
 *       均为 Long 类型，使用 {@code @NotNull} 校验（Long 类型不支持 @NotBlank）</li>
 *   <li>{@code awardName}、{@code awardLevel} 为字符串类型，使用 {@code @NotBlank} 校验</li>
 *   <li>{@code userId} 和 {@code teamEntryId} 为非必填字段，
 *       个人奖项填写 userId，团队奖项填写 teamEntryId，两者互斥</li>
 *   <li>{@code awardLevel} 典型取值如"一等奖"、"二等奖"、"金牌"、"银牌"等</li>
 * </ul>
 *
 * @author mashang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "奖项添加参数")
public class AwardQuery {

    /**
     * 运动会ID
     * <p>外键，标识该奖项属于哪一届运动会。
     * 使用 @NotNull 校验（Long 类型无法使用 @NotBlank）。
     */
    @ApiModelProperty(value = "运动会id", required = true)
    @NotNull(message = "运动会id不能为空")
    private Long meetingId;

    /**
     * 项目ID
     * <p>外键，标识该奖项属于哪个比赛项目。
     * 使用 @NotNull 校验（Long 类型无法使用 @NotBlank）。
     */
    @ApiModelProperty(value = "项目id", required = true)
    @NotNull(message = "项目id不能为空")
    private Long itemId;

    /**
     * 用户ID（获奖运动员）
     * <p>个人奖项时填写，关联 user 表。与 teamEntryId 互斥，至少填写其一。
     * 使用 @NotNull 校验（Long 类型无法使用 @NotBlank）。
     */
    @ApiModelProperty(value = "用户id")
    private Long userId;

    /**
     * 团体报名ID（获奖团队）
     * <p>团体奖项时填写，关联 team_entry 表。与 userId 互斥，至少填写其一。
     */
    @ApiModelProperty(value = "团体报名id")
    private Long teamEntryId;

    /**
     * 奖项名称
     * <p>如"男子100米冠军"、"最佳运动员"、"精神文明奖"等。
     * 使用 @NotBlank 校验，不允许为 null 或空字符串。
     */
    @ApiModelProperty(value = "奖项名称", required = true)
    @NotBlank(message = "奖项名称不能为空")
    private String awardName;

    /**
     * 奖项等级
     * <p>如"一等奖"、"二等奖"、"三等奖"、"金牌"、"银牌"、"铜牌"等。
     * 使用 @NotBlank 校验，不允许为 null 或空字符串。
     */
    @ApiModelProperty(value = "奖项等级", required = true)
    @NotBlank(message = "奖项等级不能为空")
    private String awardLevel;

}
