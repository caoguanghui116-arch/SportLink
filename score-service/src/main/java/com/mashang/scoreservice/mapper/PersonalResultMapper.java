package com.mashang.scoreservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mashang.scoreservice.domain.entity.PersonalResult;
import com.mashang.scoreservice.domain.vo.PersonalResultVo;
import com.mashang.scoreservice.domain.vo.RankingVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PersonalResultMapper extends BaseMapper<PersonalResult> {

    /**
     * 根据项目id查询个人成绩列表，按成绩降序排列
     */
    List<PersonalResultVo> selectByItemId(@Param("itemId") Long itemId);

    /**
     * 根据用户id查询个人成绩
     */
    List<PersonalResultVo> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询排行榜（按运动会id或项目id）
     */
    List<RankingVo> selectRanking(@Param("meetingId") Long meetingId, @Param("itemId") Long itemId);

}
