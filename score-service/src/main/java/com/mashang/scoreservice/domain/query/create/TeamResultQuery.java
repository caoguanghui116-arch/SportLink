package com.mashang.scoreservice.domain.query.create;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 团体成绩录入参数对象
 *
 * <p>用于裁判或管理员录入团队比赛成绩时接收前端表单数据。
 * 与 {@link PersonalResultQuery} 的区别在于使用 teamEntryId 而非 entryId + userId，
 * 因为团体成绩以团队为单位记录，而非个人。
 *
 * <p>设计要点：
 * <ul>
 *   <li>所有字段均为必填项，使用 {@code @NotNull} 校验</li>
 *   <li>{@code teamEntryId}、{@code itemId}、{@code meetingId}
 *       均为 Long 类型，必须用 @NotNull（不能用 @NotBlank，@NotBlank 仅适用于 CharSequence）</li>
 *   <li>{@code score} 使用 {@link BigDecimal} 类型，保证小数精度，
 *       适用于接力赛时间、团体总分等需要精确小数的场景</li>
 * </ul>
 *
 * @author mashang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "团体成绩录入参数")
public class TeamResultQuery {

    /**
     * 团体报名记录ID
     * <p>外键，关联 team_entry 表，用于唯一定位一条团队报名记录。
     * 使用 @NotNull 校验（Long 类型无法使用 @NotBlank）。
     */
    @ApiModelProperty(value = "团体报名id", required = true)
    @NotNull(message = "团体报名id不能为空")
    private Long teamEntryId;

    /**
     * 项目ID
     * <p>外键，关联 event_item 表，标识成绩属于哪个比赛项目。
     * 使用 @NotNull 校验（Long 类型无法使用 @NotBlank）。
     */
    @ApiModelProperty(value = "项目id", required = true)
    @NotNull(message = "项目id不能为空")
    private Long itemId;

    /**
     * 运动会ID
     * <p>外键，关联 basic_setup 表，标识成绩属于哪一届运动会。
     * 使用 @NotNull 校验（Long 类型无法使用 @NotBlank）。
     */
    @ApiModelProperty(value = "运动会id", required = true)
    @NotNull(message = "运动会id不能为空")
    private Long meetingId;

    /**
     * 团体成绩/分数
     * <p>使用 {@link BigDecimal} 类型存储，确保小数精度。
     * BigDecimal 不是字符串类型，故使用 @NotNull 而非 @NotBlank。
     */
    @ApiModelProperty(value = "团体成绩/分数", required = true)
    @NotNull(message = "团体成绩不能为空")
    private BigDecimal score;

}
