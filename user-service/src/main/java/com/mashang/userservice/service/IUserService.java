package com.mashang.userservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mashang.userservice.domain.entity.SysUser;
import com.mashang.userservice.domain.query.LoginUserQuery;
import com.mashang.userservice.domain.query.create.RegisterUserQuery;
import com.mashang.userservice.domain.vo.RefereeVo;
import com.mashang.userservice.utils.ApiResponse;

import java.util.List;

public interface IUserService extends IService<SysUser> {

    ApiResponse login(LoginUserQuery user);

    int register(RegisterUserQuery query);

    int update(String password);

    List<RefereeVo> allReferee();
}
