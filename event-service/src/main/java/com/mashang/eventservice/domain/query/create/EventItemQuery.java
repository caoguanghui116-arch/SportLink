package com.mashang.eventservice.domain.query.create;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 比赛项目创建/编辑参数对象
 *
 * <p>用于管理员在运动会中创建或编辑比赛项目时接收前端表单数据。
 * 一个项目隶属于某个运动会（meetingId），属于某个分类（categoryId），
 * 并定义了项目名称、类型、规则、性别限制、限报人数等属性。
 *
 * <p>设计要点：
 * <ul>
 *   <li>{@code itemName}、{@code itemType}、{@code itemRule} 为字符串字段，
 *       使用 {@code @NotBlank} 校验，确保不为 null、不为空字符串</li>
 *   <li>{@code meetingId}、{@code categoryId}、{@code sexLimit}、{@code maxEntry}
 *       为 Long 类型，使用 {@code @NotNull} 校验（Long 类型不支持 @NotBlank）</li>
 *   <li>{@code maxEntry} 额外使用 {@code @Range} 限制最小值为 1，
 *       即每个项目至少允许 1 人报名，避免业务异常</li>
 *   <li>{@code itemType} 取值如"径赛"、"田赛"、"团体"等，约定由前端下拉框约束</li>
 *   <li>{@code sexLimit} 取值如 0=不限、1=男子、2=女子等，具体语义由业务约定</li>
 * </ul>
 *
 * @author mashang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "项目信息添加参数")
public class EventItemQuery {

    /**
     * 所属运动会ID
     * <p>外键，关联 basic_setup 表，标识该项目属于哪一届运动会。
     * 使用 @NotNull 校验（Long 类型无法使用 @NotBlank）。
     */
    @ApiModelProperty("所属运动会ID")
    @NotNull(message = "所属运动会不能为空")
    private Long meetingId;

    /**
     * 项目分类ID
     * <p>外键，关联 category 表，用于对项目进行归类（如"田径类"、"球类"等）。
     * 使用 @NotNull 校验（Long 类型无法使用 @NotBlank）。
     */
    @ApiModelProperty("项目分类ID")
    @NotNull(message = "项目分类不能为空")
    private Long categoryId;

    /**
     * 项目名称
     * <p>如"男子100米"、"女子跳远"、"4x100米接力"等，前端直接展示。
     * 使用 @NotBlank 校验，不允许为 null 或空字符串。
     */
    @ApiModelProperty("项目名称")
    @NotBlank(message = "项目名称不能为空")
    private String itemName;

    /**
     * 项目类型
     * <p>标识比赛性质，如"径赛"、"田赛"、"团体"等。
     * 使用 @NotBlank 校验，不允许为 null 或空字符串。
     */
    @ApiModelProperty("项目类型（径赛 / 田赛 / 团体）")
    @NotBlank(message = "项目类型不能为空")
    private String itemType;

    /**
     * 项目规则
     * <p>描述该项目的竞赛规则文本，前端在项目详情中展示。
     * 使用 @NotBlank 校验，不允许为 null 或空字符串。
     */
    @ApiModelProperty("项目规则")
    @NotBlank(message = "项目规则不能为空")
    private String itemRule;

    /**
     * 性别限制
     * <p>Long 类型（使用 @NotNull 校验），
     * 取值约定：0=不限性别，1=仅限男子，2=仅限女子。
     */
    @ApiModelProperty("性别限制（男子/女子/不限）")
    @NotNull(message = "性别限制不能为空")
    private Long sexLimit;

    /**
     * 限报人数（该项目最大允许报名人数）
     * <p>使用 @NotNull 校验确保不为空，同时使用 @Range(min = 1) 限制最小值 >= 1，
     * 防止管理员误设为 0 或负数导致无人可报名。
     */
    @ApiModelProperty("限报人数")
    @NotNull(message = "限报人数不能为空")
    @Range(min = 1, message = "限报人数不能小于1")
    private Long maxEntry;
}
