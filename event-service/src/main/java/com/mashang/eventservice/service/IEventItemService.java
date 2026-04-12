package com.mashang.eventservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mashang.eventservice.domain.entity.EventItem;
import com.mashang.eventservice.domain.query.create.EventItemQuery;
import com.mashang.eventservice.domain.query.update.EventItemUpdate;
import com.mashang.eventservice.domain.vo.EventItemVo;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface IEventItemService extends IService<EventItem> {


    /**
     * 项目信息添加
     * @param addQuery 添加参数
     * @return 返回操作行数
     */
    int addProject(EventItemQuery addQuery);

    /**
     * 项目信息修改
     * @param updateQuery 修改参数
     * @return 返回操作行数
     */
    int updateProject(EventItemUpdate updateQuery);

    /**
     * 项目信息删除
     * @param itemId 项目id
     * @return 返回操作行数
     */
    int deleteProject(Long itemId);


    /**
     * 查询所有项目名称
     * @return 返回项目信息集合
     */
    List<EventItemVo> allItem();
}
