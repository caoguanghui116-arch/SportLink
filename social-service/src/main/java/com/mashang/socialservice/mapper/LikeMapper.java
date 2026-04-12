package com.mashang.socialservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mashang.socialservice.domain.entity.Like;
import org.apache.ibatis.annotations.Param;

public interface LikeMapper extends BaseMapper<Like> {

    int deleteByTargetAndUser(@Param("targetType") Long targetType, @Param("targetId") Long targetId, @Param("userId") Long userId);

    int existsByTargetAndUser(@Param("targetType") Long targetType, @Param("targetId") Long targetId, @Param("userId") Long userId);

}
