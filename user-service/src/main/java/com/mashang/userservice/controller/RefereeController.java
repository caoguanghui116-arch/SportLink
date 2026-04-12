package com.mashang.userservice.controller;

import com.mashang.userservice.domain.entity.R;
import com.mashang.userservice.domain.entity.SysUser;
import com.mashang.userservice.domain.vo.RefereeVo;
import com.mashang.userservice.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/referee")
@Api(tags = "裁判接口")
public class RefereeController {

    @Autowired
    private IUserService userService;

    @GetMapping("/all")
    @ApiOperation("查询所有裁判")
    public R<List<RefereeVo>> allReferee(){

        return R.ok(userService.allReferee());
    }
}
