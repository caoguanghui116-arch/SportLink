package com.mashang.userservice.domain.query.create;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 用户注册请求参数对象
 *
 * <p>用于用户注册时接收前端表单数据，包含用户名、密码、真实姓名、手机号、角色ID。
 * 与登录参数 {@link com.mashang.userservice.domain.query.LoginUserQuery} 相比，
 * 增加了手机号格式校验、真实姓名等注册独有的字段。
 *
 * <p>设计要点：
 * <ul>
 *   <li>{@code username}：使用 {@code @NotBlank} + {@code @Size(min = 3, max = 30)} 校验，
 *       与登录接口一致</li>
 *   <li>{@code password}：使用 {@code @NotBlank} + {@code @Size(min = 6, max = 20)} 校验，
 *       最短 6 位符合基本安全要求</li>
 *   <li>{@code realName}：使用 {@code @NotBlank} + {@code @Size(max = 30)} 校验，
 *       实名制要求的真实姓名</li>
 *   <li>{@code phone}：使用 {@code @Pattern} 注解校验手机号格式，
 *       正则表达式 {@code ^1[3-9]\d{9}$} 含义：
 *       <ul>
 *         <li>{@code ^} —— 字符串开始</li>
 *         <li>{@code 1} —— 手机号以 1 开头（中国大陆手机号首位为 1）</li>
 *         <li>{@code [3-9]} —— 第二位为 3-9（匹配中国移动/联通/电信号段）</li>
 *         <li>{@code \d{9}} —— 后续 9 位为任意数字（d 是数字简写，需转义为 \\d）</li>
 *         <li>{@code $} —— 字符串结束，共 11 位数字</li>
 *       </ul>
 *       注意：phone 字段没有加 @NotBlank，说明手机号为可选字段。</li>
 *   <li>{@code roleId}：非必填，默认角色通常为普通用户（roleId=2），
 *       管理员账户由后台手动创建</li>
 * </ul>
 *
 * @author mashang
 */
@Data
@ApiModel(description = "用户注册参数")
public class RegisterUserQuery {

    /**
     * 用户名/登录账号
     * <p>使用 @NotBlank 校验（不允许 null、空字符串、纯空白），
     * @Size(min = 3, max = 30) 限制长度 3-30 个字符。
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 30, message = "用户名长度需在3-30个字符之间")
    @ApiModelProperty(value = "用户名", required = true)
    private String username;

    /**
     * 登录密码
     * <p>使用 @NotBlank 校验，@Size(min = 6, max = 20) 限制长度 6-20 个字符。
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度需在6-20个字符之间")
    @ApiModelProperty(value = "密码", required = true)
    private String password;

    /**
     * 真实姓名
     * <p>运动员/用户的真实姓名，用于报名表、成绩单等正式场景。
     * 使用 @NotBlank 校验，@Size(max = 30) 限制最长 30 个字符。
     */
    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 30, message = "姓名不能超过30个字符")
    @ApiModelProperty(value = "真实姓名", required = true)
    private String realName;

    /**
     * 手机号（可选）
     * <p>使用 {@code @Pattern} 注解校验中国大陆手机号格式：
     * 正则 {@code ^1[3-9]\d{9}$} 匹配 1 开头的 11 位数字，
     * 第二位必须为 3-9（覆盖中国移动/联通/电信现有号段）。
     * 该字段未加 @NotBlank，因此手机号为可选填写。
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @ApiModelProperty(value = "手机号", example = "13800138000")
    private String phone;

    /**
     * 角色ID（非必填）
     * <p>Long 类型，用于指定用户注册时的角色。
     * 不传时默认为普通用户角色（通常 roleId=2），管理员角色（roleId=1）由后台单独分配。
     */
    @ApiModelProperty(value = "角色ID")
    private Long roleId;
}
