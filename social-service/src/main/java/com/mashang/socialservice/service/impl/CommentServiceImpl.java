package com.mashang.socialservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.socialservice.domain.entity.Comment;
import com.mashang.socialservice.domain.entity.R;
import com.mashang.socialservice.domain.query.create.CommentQuery;
import com.mashang.socialservice.domain.vo.CommentVo;
import com.mashang.socialservice.feign.UserServiceFeign;
import com.mashang.socialservice.mapper.CommentMapper;
import com.mashang.socialservice.mapper.PostMapper;
import com.mashang.socialservice.mapping.CommentMapping;
import com.mashang.socialservice.service.ICommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    private static final String LIKE_KEY_PREFIX = "social:like:comment:";

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserServiceFeign userServiceFeign;

    @Override
    @Transactional
    public CommentVo add(CommentQuery commentQuery, Long userId) {
        Comment comment = CommentMapping.INSTANCE.toEntity(commentQuery);
        comment.setUserId(userId);
        comment.setLikeCount(0L);
        comment.setDelFlag(0L);

        commentMapper.insert(comment);

        // 更新动态的评论数
        postMapper.updateCommentCount(commentQuery.getPostId(), 1);

        CommentVo vo = new CommentVo();
        vo.setCommentId(comment.getCommentId());
        vo.setPostId(comment.getPostId());
        vo.setUserId(comment.getUserId());
        vo.setParentId(comment.getParentId());
        vo.setContent(comment.getContent());
        vo.setLikeCount(0L);
        vo.setCreateTime(comment.getCreateTime());
        vo.setUsername(getUsername(userId));
        vo.setReplies(new ArrayList<>());

        return vo;
    }

    @Override
    public int delete(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除他人评论");
        }

        comment.setDelFlag(2L);
        int result = commentMapper.updateById(comment);

        if (result > 0) {
            // 更新动态的评论数
            postMapper.updateCommentCount(comment.getPostId(), -1);
        }

        return result;
    }

    @Override
    public List<CommentVo> listByPostId(Long postId) {
        // 获取所有评论
        List<CommentVo> allComments = commentMapper.selectByPostId(postId);
        if (allComments == null || allComments.isEmpty()) {
            return new ArrayList<>();
        }

        // 填充用户名
        allComments.forEach(vo -> vo.setUsername(getUsername(vo.getUserId())));

        // 构建树形结构
        return buildCommentTree(allComments);
    }

    @Override
    public boolean like(Long commentId, Long userId) {
        String key = LIKE_KEY_PREFIX + commentId;
        Boolean isMember = redisTemplate.opsForSet().isMember(key, userId.toString());
        if (Boolean.TRUE.equals(isMember)) {
            throw new RuntimeException("已经点赞过了");
        }

        redisTemplate.opsForSet().add(key, userId.toString());

        // 更新评论点赞数
        Comment comment = commentMapper.selectById(commentId);
        if (comment != null) {
            comment.setLikeCount(comment.getLikeCount() + 1);
            commentMapper.updateById(comment);
        }

        return true;
    }

    /**
     * 构建评论树形结构
     */
    private List<CommentVo> buildCommentTree(List<CommentVo> allComments) {
        // 找出所有根评论 (parentId == null)
        List<CommentVo> roots = allComments.stream()
                .filter(c -> c.getParentId() == null)
                .collect(Collectors.toList());

        // 为每个根评论递归挂载子回复
        for (CommentVo root : roots) {
            root.setReplies(getChildReplies(root.getCommentId(), allComments));
        }

        return roots;
    }

    /**
     * 递归获取子回复
     */
    private List<CommentVo> getChildReplies(Long parentId, List<CommentVo> allComments) {
        List<CommentVo> children = allComments.stream()
                .filter(c -> parentId.equals(c.getParentId()))
                .collect(Collectors.toList());

        for (CommentVo child : children) {
            child.setReplies(getChildReplies(child.getCommentId(), allComments));
        }

        return children;
    }

    /**
     * 通过Feign获取用户名
     */
    private String getUsername(Long userId) {
        try {
            R<Map<String, Object>> result = userServiceFeign.getUserInfo(userId);
            if (result != null && result.getData() != null && result.getData().get("username") != null) {
                return result.getData().get("username").toString();
            }
        } catch (Exception e) {
            // feign调用失败，返回默认值
        }
        return "用户" + userId;
    }

}
