package com.mashang.socialservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mashang.socialservice.domain.entity.Post;
import com.mashang.socialservice.domain.query.create.PostQuery;
import com.mashang.socialservice.domain.vo.PostVo;

import java.util.List;

public interface IPostService extends IService<Post> {

    /**
     * 发布动态
     * @param postQuery 发布参数
     * @param userId 用户id
     * @return 返回动态VO
     */
    PostVo publish(PostQuery postQuery, Long userId);

    /**
     * 删除动态
     * @param postId 动态id
     * @param userId 用户id(验证)
     * @return 返回影响行数
     */
    int delete(Long postId, Long userId);

    /**
     * 查询运动会动态列表
     * @param meetingId 运动会id
     * @return 返回动态列表
     */
    List<PostVo> listByMeetingId(Long meetingId);

    /**
     * 动态详情(含评论)
     * @param postId 动态id
     * @return 返回动态VO
     */
    PostVo detail(Long postId);

    /**
     * 点赞动态
     * @param postId 动态id
     * @param userId 用户id
     * @return 返回是否成功
     */
    boolean like(Long postId, Long userId);

    /**
     * 取消点赞
     * @param postId 动态id
     * @param userId 用户id
     * @return 返回是否成功
     */
    boolean unlike(Long postId, Long userId);

}
