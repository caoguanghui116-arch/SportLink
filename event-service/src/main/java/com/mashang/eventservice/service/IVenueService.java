package com.mashang.eventservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mashang.eventservice.domain.entity.Venue;
import com.mashang.eventservice.domain.vo.VenueVo;

import java.util.List;

public interface IVenueService extends IService<Venue> {

    /**
     * 查询所有场地
     * @return 场地集合
     */
    List<VenueVo> allVenue();

}
