package com.mashang.socialservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mashang.socialservice.domain.entity.Comment;
import com.mashang.socialservice.domain.query.create.CommentQuery;
import com.mashang.socialservice.domain.vo.CommentVo;

import java.util.List;

public interface ICommentService extends IService<Comment> {

    /**
     * 添加评论
     * @param commentQuery 评论参数
     * @param userId 用户id
     * @return 返回评论VO
     */
    CommentVo add(CommentQuery commentQuery, Long userId);

    /**
     * 删除评论
     * @param commentId 评论id
     * @param userId 用户id
     * @return 返回影响行数
     */
    int delete(Long commentId, Long userId);

    /**
     * 查询动态评论列表(树形结构)
     * @param postId 动态id
     * @return 返回评论树形列表
     */
    List<CommentVo> listByPostId(Long postId);

    /**
     * 点赞评论
     * @param commentId 评论id
     * @param userId 用户id
     * @return 返回是否成功
     */
    boolean like(Long commentId, Long userId);

}
