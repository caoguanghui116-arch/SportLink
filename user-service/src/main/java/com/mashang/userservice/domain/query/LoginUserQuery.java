package com.mashang.userservice.domain.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 用户登录请求参数对象
 *
 * <p>用于用户登录时接收前端表单数据，包含用户名和密码两个字段。
 * 该对象仅用于接收登录请求，不包含 Token 或会话信息，登录成功后由服务端生成 JWT Token 返回。
 *
 * <p>设计要点：
 *username和password均为字符串类型，使用@NotBlank} 校验，
 *       确保不为 null、不为空字符串、不为纯空白字符
 *  {@code username} 使用 {@code @Size(min = 3, max = 30)} 限制长度范围，
 *       最短 3 个字符防止过短用户名，最长 30 个字符防止恶意超长输入</li>
 * {@code password} 使用 {@code @Size(min = 6, max = 20)} 限制长度范围，
 *       最短 6 个字符符合基本安全要求，最长 20 个字符是常见的密码上限</li>
 *两个字段均使用 {@code @ApiModelProperty} 标注 example 示例值，
 *       方便 Swagger UI 在线调试
 *
 * @author caoguanghui
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "用户登录参数")
public class LoginUserQuery {

    /**
     * 登录账号/用户名
     * 使用 @NotBlank 校验，确保不为 null、不为空字符串、不为纯空白。
     * @Size(min = 3, max = 30) 限制用户名长度在 3-30 个字符之间。
     */
    @NotBlank(message = "账号不能为空")
    @Size(min = 3, max = 30, message = "账号长度需在3-30个字符之间")
    @ApiModelProperty(value = "登录账号", example = "admin", required = true)
    private String username;

    /**
     * 登录密码
     * 使用 @NotBlank 校验，确保不为 null、不为空字符串、不为纯空白。
     * @Size(min = 6, max = 20) 限制密码长度在 6-20 个字符之间，
     * 实际密码以密文形式传输（前端 MD5/SHA 加密后发送）。
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度需在6-20个字符之间")
    @ApiModelProperty(value = "登录密码", example = "123456", required = true)
    private String password;
}
