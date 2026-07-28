package com.mashang.userservice.controller;

import com.mashang.common.common.R;
import com.mashang.userservice.domain.entity.SysUser;
import com.mashang.userservice.domain.query.LoginUserQuery;
import com.mashang.userservice.domain.query.create.RegisterUserQuery;
import com.mashang.userservice.service.IUserService;
import com.mashang.userservice.utils.JWTUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 *
 * 核心职责：
 * - 用户登录：接收用户名/密码，委托 Service 完成认证并返回 JWT 令牌
 * - 用户注册：接收注册表单，委托 Service 完成密码加密和入库
 * - 密码修改：从 JWT 中解析当前用户，更新加密后的新密码
 * - 用户信息查询：根据用户ID获取用户基本信息
 *
 * 设计思路：
 * - 控制器层仅做参数校验（@Validated）和结果封装（R 统一响应体），
 *   所有业务逻辑下沉到 IUserService 实现中
 * - Swagger @Api / @ApiOperation 注解提供自动生成的 API 文档
 */
@RestController
@RequestMapping("/user")
@Api(tags = "用户管理")
public class UserController {

    /** 用户服务接口，实际注入的是 UserServiceImpl */
    @Autowired
    private IUserService userService;

    /**
     * 用户登录
     *
     * 接收用户名和密码，调用 Service 层完成以下流程：
     * 1. AuthenticationManager.authenticate() 执行 BCrypt 密码比对
     * 2. 认证通过后签发 JWT 令牌
     * 3. 将用户会话信息写入 Redis（供 Gateway 校验）
     *
     * @param user 登录请求体，包含 username 和 password 字段
     * @return 统一响应体，data 字段为 JWT 令牌字符串
     */
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public R<?> login(@RequestBody @Validated LoginUserQuery user) {
        return R.ok(userService.login(user));
    }

    /**
     * 用户注册
     *
     * 新用户注册流程：
     * 1. 校验用户名是否已存在（唯一性检查）
     * 2. 使用 BCryptPasswordEncoder 对明文密码加密
     * 3. 设置默认角色和启用状态后入库
     *
     * @param query 注册请求体，包含 username、password、realName、phone 等字段
     * @return 统一响应体，ok 表示注册成功
     */
    @ApiOperation("用户注册")
    @PostMapping("/register")
    public R<Void> register(@RequestBody @Validated RegisterUserQuery query) {
        return R.toResult(userService.register(query));
    }

    /**
     * 修改当前登录用户的密码
     *
     * 从请求头中的 JWT 令牌解析当前用户ID，
     * 使用 BCryptPasswordEncoder 加密新密码后更新数据库
     *
     * @param password 新密码（明文，由 Service 层负责加密）
     * @return 统一响应体，ok 表示修改成功
     */
    @ApiOperation("修改密码")
    @PutMapping("/password")
    @ApiImplicitParam(name = "password", value = "新密码", required = true)
    public R<Void> updatePassword(@RequestParam String password) {
        return R.toResult(userService.update(password));
    }

    /**
     * 根据用户ID获取用户详细信息
     *
     * @param userId 用户ID（路径参数）
     * @return 统一响应体，data 字段为 SysUser 实体
     */
    @ApiOperation("获取用户信息")
    @GetMapping("/{userId}")
    @ApiImplicitParam(name = "userId", value = "用户ID", required = true)
    public R<SysUser> getUserInfo(@PathVariable Long userId) {
        return R.ok(userService.getById(userId));
    }

    @ApiOperation("获取用户id(fegin)")
    @GetMapping("/userId")
    public Long getUserId() {

        return JWTUtil.getUserId();
    }
}
