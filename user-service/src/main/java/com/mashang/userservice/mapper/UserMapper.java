package com.mashang.userservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mashang.userservice.domain.entity.SysUser;
import com.mashang.userservice.domain.vo.RefereeVo;

import java.util.List;

public interface UserMapper extends BaseMapper<SysUser> {

    List<RefereeVo> allReferee();
}
