package com.mashang.userservice.domain.query;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
//
//@Schema(description = "登陆参数")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserQuery {

//    @NotBlank(message = "账号信息不能为空")
//    @Schema(description = "登陆账号",
//            example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;
//
//    @NotBlank(message = "登陆密码不能为空")
//    @Schema(description = "登陆密码",
//            example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
