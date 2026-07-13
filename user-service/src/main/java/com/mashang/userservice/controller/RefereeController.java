package com.mashang.userservice.controller;

import com.mashang.common.common.R;
import com.mashang.userservice.domain.vo.RefereeVo;
import com.mashang.userservice.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 裁判管理控制器
 *
 * 核心职责：
 * - 查询系统中所有具有裁判角色的用户列表
 *
 * 设计思路：
 * - 裁判数据来源于用户表（SysUser），通过角色字段区分
 * - 查询结果通过 RefereeVo 视图对象返回，隐藏敏感字段（如密码）
 * - 结果集使用 Redis 缓存，TTL 为 60 分钟，提升查询性能
 */
@RestController
@RequestMapping("/referee")
@Api(tags = "裁判管理")
public class RefereeController {

    /** 用户服务接口，裁判查询复用 IUserService 的 allReferee() 方法 */
    @Autowired
    private IUserService userService;

    /**
     * 查询所有裁判
     *
     * 使用场景：赛程编排时选择裁判、裁判分配页面展示可选裁判列表
     * 缓存策略：优先从 Redis 读取；未命中则查询数据库并回写缓存
     *
     * @return 统一响应体，data 字段为裁判视图对象列表
     */
    @GetMapping
    @ApiOperation("查询所有裁判")
    public R<List<RefereeVo>> allReferee() {
        return R.ok(userService.allReferee());
    }
}
