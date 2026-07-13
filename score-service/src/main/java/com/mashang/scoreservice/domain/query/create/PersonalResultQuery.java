package com.mashang.scoreservice.domain.query.create;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 个人成绩录入参数对象
 *
 * <p>用于裁判或管理员录入运动员个人比赛成绩时接收前端表单数据。
 * 一次录入需要明确：哪条报名记录（entryId）、哪位运动员（userId）、
 * 哪个项目（itemId）、哪届运动会（meetingId）、成绩是多少（score）。
 *
 * <p>设计要点：
 * <ul>
 *   <li>所有字段均为必填项，使用 {@code @NotNull} 校验</li>
 *   <li>{@code entryId}、{@code userId}、{@code itemId}、{@code meetingId}
 *       均为 Long 类型，必须用 @NotNull（不能用 @NotBlank，@NotBlank 仅适用于字符串）</li>
 *   <li>{@code score} 使用 {@link BigDecimal} 类型，保证小数精度，
 *       避免浮点数运算误差（如 9.58 秒、6.17 米等成绩值）</li>
 *   <li>{@code score} 同样是 BigDecimal 非字符串，故用 @NotNull 而非 @NotBlank</li>
 * </ul>
 *
 * @author mashang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "个人成绩录入参数")
public class PersonalResultQuery {

    /**
     * 报名记录ID
     * <p>外键，关联 personal_entry 表，用于唯一定位一条个人报名记录。
     * 使用 @NotNull 校验（Long 类型无法使用 @NotBlank）。
     */
    @ApiModelProperty(value = "报名记录id", required = true)
    @NotNull(message = "报名记录id不能为空")
    private Long entryId;

    /**
     * 用户ID（运动员ID）
     * <p>外键，关联 user 表，标识取得该成绩的运动员。
     * 使用 @NotNull 校验（Long 类型无法使用 @NotBlank）。
     */
    @ApiModelProperty(value = "用户id", required = true)
    @NotNull(message = "用户id不能为空")
    private Long userId;

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
     * 成绩/分数
     * <p>使用 {@link BigDecimal} 类型存储，确保小数精度。
     * 例如短跑成绩 9.58（秒）、跳远成绩 6.17（米），避免 float/double 精度丢失。
     * BigDecimal 不是字符串类型，故使用 @NotNull 而非 @NotBlank。
     */
    @ApiModelProperty(value = "成绩/分数", required = true)
    @NotNull(message = "成绩不能为空")
    private BigDecimal score;

}
