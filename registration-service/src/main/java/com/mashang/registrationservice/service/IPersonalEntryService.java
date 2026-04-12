package com.mashang.registrationservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mashang.registrationservice.domain.entity.PersonalEntry;
import com.mashang.registrationservice.domain.query.create.PersonalEntryQuery;
import com.mashang.registrationservice.domain.vo.PersonalEntryVo;

import java.util.List;

public interface IPersonalEntryService extends IService<PersonalEntry> {

    int enroll(PersonalEntryQuery query);

    int cancel(Long entryId, Long userId);

    List<PersonalEntryVo> listByUserId(Long userId);

    PersonalEntryVo detail(Long entryId);

    int countByItemId(Long itemId);
}
