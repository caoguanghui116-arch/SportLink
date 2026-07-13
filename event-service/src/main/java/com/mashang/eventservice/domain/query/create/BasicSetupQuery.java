package com.mashang.eventservice.domain.query.create;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * 赛事基本信息创建/编辑参数对象
 *
 * <p>用于运动会创建和编辑时接收前端表单数据，包含运动会的基本元信息：
 * 届数、名称、起止时间、报名时间窗口、主场地等。
 *
 * <p>设计要点：
 * <ul>
 *   <li>字符串类型字段（meetingName、mainVenue）使用 {@code @NotBlank} 校验，
 *       既不允许 null，也不允许空字符串和纯空白字符</li>
 *   <li>Long 类型字段（meetingSession）和 Date 类型字段（startTime、endTime 等）
 *       使用 {@code @NotNull} 校验 —— Date 和 Long 类型无法用 @NotBlank 校验，
 *       @NotBlank 仅适用于 CharSequence 类型</li>
 *   <li>日期字段使用 {@code @JsonFormat} 指定前端传入的日期格式为
 *       "yyyy-MM-dd HH:mm:ss"，时区为 GMT+8（北京时间）</li>
 *   <li>字符串字段添加 {@code @Size} 限制最大长度，防止恶意超长输入</li>
 * </ul>
 *
 * @author mashang
 * @see com.mashang.eventservice.domain.entity.BasicSetup 对应的数据库实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "赛事信息添加参数")
public class BasicSetupQuery {

    /**
     * 运动会届数
     * <p>使用 @NotNull 而非 @NotBlank，因为 Long 类型不支持 @NotBlank（@NotBlank 仅适用于 CharSequence）。
     * 该字段为数值类型，用于标识运动会的届次序号。
     */
    @ApiModelProperty("运动会届数")
    @NotNull(message = "运动会届数不能为空")
    private Long meetingSession;

    /**
     * 运动会名称
     * <p>使用 @NotBlank 校验，确保名称不为 null、不为空字符串、不为纯空白字符。
     * 前端在运动会列表和详情页中展示。
     */
    @ApiModelProperty("运动会名称")
    @NotBlank(message = "运动会名称不能为空")
    @Size(max = 100, message = "运动会名称不能超过100个字符")
    private String meetingName;

    /**
     * 运动会开始时间
     * <p>使用 @NotNull 校验（Date 类型必须用 @NotNull，不能用 @NotBlank）。
     * 前端通过 JsonFormat 以 "yyyy-MM-dd HH:mm:ss" 格式传入。时区为 GMT+8（北京时间）。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty("运动会开始时间")
    @NotNull(message = "运动会开始时间不能为空")
    private Date startTime;

    /**
     * 运动会结束时间
     * <p>必须晚于 startTime，具体业务校验在 Service 层实现。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty("运动会结束时间")
    @NotNull(message = "运动会结束时间不能为空")
    private Date endTime;

    /**
     * 开始报名时间
     * <p>用户可从此时间节点开始提交报名申请，必须早于 signEndTime。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty("开始报名时间")
    @NotNull(message = "开始报名时间不能为空")
    private Date signStartTime;

    /**
     * 报名截止时间
     * <p>此时间节点之后系统不再接受新的报名申请，必须晚于 signStartTime。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty("截至报名时间")
    @NotNull(message = "截至报名时间不能为空")
    private Date signEndTime;

    /**
     * 主场地名称/地址
     * <p>运动会的主要举办场地，用于前端展示和赛程排布参考。
     */
    @ApiModelProperty("主场地")
    @NotBlank(message = "主场地不能为空")
    @Size(max = 200, message = "主场地名称不能超过200个字符")
    private String mainVenue;

}
