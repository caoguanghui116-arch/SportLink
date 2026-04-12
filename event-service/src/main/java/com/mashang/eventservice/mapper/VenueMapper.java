package com.mashang.eventservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mashang.eventservice.domain.entity.Venue;
import com.mashang.eventservice.domain.vo.VenueVo;

import java.util.List;

public interface VenueMapper extends BaseMapper<Venue> {

    List<VenueVo> allVenue();
}
