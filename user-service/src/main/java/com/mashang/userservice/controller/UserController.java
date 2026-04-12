package com.mashang.userservice.controller;

import com.mashang.userservice.domain.entity.R;
import com.mashang.userservice.domain.entity.SysUser;
import com.mashang.userservice.domain.query.LoginUserQuery;
import com.mashang.userservice.domain.query.create.RegisterUserQuery;
import com.mashang.userservice.service.IUserService;
import com.mashang.userservice.utils.ApiResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Api(tags = "全平台-common")
public class UserController {

    @Autowired
    private IUserService userService;

    @ApiOperation("登录接口")
    @PostMapping("/login")
    public ApiResponse login(@RequestBody LoginUserQuery user) {
        return userService.login(user);
    }

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public R register(@RequestBody @Validated RegisterUserQuery query) {
        return R.toResult(userService.register(query));
    }

    @ApiOperation("修改密码")
    @PutMapping("/update/password")
    public R update(String password){
        return R.toResult(userService.update(password));
    }

    @ApiOperation("获取用户信息")
    @GetMapping("/info/{userId}")
    @ApiImplicitParam(name = "userId", value = "用户ID")
    public R<SysUser> getUserInfo(@PathVariable Long userId) {
        return R.ok(userService.getById(userId));
    }

}
