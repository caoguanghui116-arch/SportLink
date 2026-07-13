package com.mashang.eventservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mashang.eventservice.domain.entity.SportsMeeting;
import com.mashang.eventservice.domain.query.create.BasicSetupQuery;

import java.util.List;

/**
 * 运动会管理服务接口 —— 定义运动会的创建与查询操作。
 *
 * 继承 MyBatis-Plus 的 IService&lt;SportsMeeting&gt;，自动获得 CRUD 基础能力
 * （如 getById、list、updateById 等），本接口只扩展运动会特有的业务方法。
 */
public interface ISportsMeetingService extends IService<SportsMeeting> {

    /**
     * 新增运动会。
     * 包含届数/名称唯一性校验，新增成功后自动清除运动会列表缓存。
     *
     * @param addQuery 运动会创建参数（届数、名称、时间、场馆等）
     * @return 数据库影响行数，大于 0 表示成功
     */
    int addMeeting(BasicSetupQuery addQuery);

    /**
     * 查询全部运动会列表（带 Redis 缓存）。
     * 采用 Cache Aside 模式：先查缓存 → 未命中查 DB → 写入缓存 → 返回。
     * 缓存 Key：list:event:all，TTL：30 分钟。
     *
     * @return 全部运动会列表（按创建时间排序）
     */
    List<SportsMeeting> allMeeting();
}
