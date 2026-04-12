package com.mashang.scoreservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.scoreservice.domain.entity.Award;
import com.mashang.scoreservice.domain.query.create.AwardQuery;
import com.mashang.scoreservice.domain.vo.AwardVo;
import com.mashang.scoreservice.mapper.AwardMapper;
import com.mashang.scoreservice.mapping.ScoreMapping;
import com.mashang.scoreservice.service.IAwardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AwardServiceImpl extends ServiceImpl<AwardMapper, Award> implements IAwardService {

    @Autowired
    private AwardMapper awardMapper;

    @Override
    public int add(AwardQuery query) {

        Award entity = ScoreMapping.INSTANCE.toEntity(query);
        return awardMapper.insert(entity);
    }

    @Override
    public List<AwardVo> listByMeetingId(Long meetingId) {
        return awardMapper.selectByMeetingId(meetingId);
    }

}
