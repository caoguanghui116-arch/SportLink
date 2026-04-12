package com.mashang.registrationservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mashang.registrationservice.domain.entity.PersonalEntry;
import com.mashang.registrationservice.domain.vo.PersonalEntryVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PersonalEntryMapper extends BaseMapper<PersonalEntry> {

    List<PersonalEntryVo> selectByUserId(@Param("userId") Long userId);

    PersonalEntryVo selectDetailById(@Param("entryId") Long entryId);

    int countByItemId(@Param("itemId") Long itemId);
}
