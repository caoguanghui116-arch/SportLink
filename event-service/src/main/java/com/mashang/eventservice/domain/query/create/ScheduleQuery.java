package com.mashang.eventservice.domain.query.create;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 赛程信息创建/编辑参数对象
 *
 * <p>用于管理员创建或编辑比赛赛程时接收前端表单数据，定义一场比赛的关键要素：
 * 哪个项目（itemId）、在哪个场地（venueId）、由哪位裁判执裁（refereeId）、
 * 何时比赛（gameTime）、属于哪个组别（group）、当前状态（status）。
 *
 * <p>设计要点：
 * <ul>
 *   <li>{@code itemId}、{@code venueId}、{@code refereeId}、{@code status}
 *       均为 Long 类型的外键ID 或 状态码，使用 {@code @NotNull} 校验
 *       （Long 类型不支持 @NotBlank）</li>
 *   <li>{@code group} 为字符串类型的赛程组别（如"预赛"、"决赛"、"男子组"、"女子组"等），
 *       使用 {@code @NotBlank} 校验，确保不为 null 且不为空字符串</li>
 *   <li>{@code gameTime} 为 Date 类型，使用 {@code @NotNull} 校验</li>
 *   <li>status 字段：通常 0=未开始，1=进行中，2=已结束，具体语义由业务约定</li>
 * </ul>
 *
 * @author mashang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "赛程信息添加参数")
public class ScheduleQuery {

  /**
   * 项目ID
   * <p>外键，关联 event_item 表，标识该赛程属于哪个比赛项目。
   * 使用 @NotNull 校验（Long 类型无法使用 @NotBlank）。
   */
  @ApiModelProperty(value = "项目id",required = true)
  @NotNull(message = "项目id不能为空")
  private Long itemId;

  /**
   * 场地ID
   * <p>外键，关联 venue 表，标识比赛在哪个场地进行。
   * 使用 @NotNull 校验（Long 类型无法使用 @NotBlank）。
   */
  @ApiModelProperty(value = "场地id",required = true)
  @NotNull(message = "场地id不能为空")
  private Long venueId;

  /**
   * 裁判ID
   * <p>外键，关联 referee 表（或 user 表中的裁判角色），标识本场比赛的执裁裁判。
   * 使用 @NotNull 校验（Long 类型无法使用 @NotBlank）。
   */
  @ApiModelProperty(value = "裁判id",required = true)
  @NotNull(message = "裁判id不能为空")
  private Long refereeId;

  /**
   * 比赛时间
   * <p>使用 @NotNull 校验 —— Date 类型必须用 @NotNull 而不能用 @NotBlank。
   */
  @ApiModelProperty(value = "比赛时间",required = true)
  @NotNull(message = "比赛时间不能为空")
  private Date gameTime;

  /**
   * 赛程组别
   * <p>用于区分不同轮次或组别，如"预赛"、"复赛"、"决赛"、"男子组"、"女子组"等。
   * 使用 @NotBlank 校验，不允许为 null 或空字符串。
   */
  @ApiModelProperty(value = "赛程组别",required = true)
  @NotBlank(message = "赛程组别不能为空")
  private String group;

  /**
   * 赛程状态
   * <p>Long 类型的状态码（使用 @NotNull 校验），
   * 典型取值：0=未开始，1=进行中，2=已结束。
   */
  @ApiModelProperty(value = "状态",required = true)
  @NotNull(message = "状态不能为空")
  private Long status;

}
