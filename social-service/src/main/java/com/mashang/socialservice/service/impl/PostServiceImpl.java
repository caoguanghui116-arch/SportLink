package com.mashang.socialservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.socialservice.domain.entity.Post;
import com.mashang.socialservice.domain.entity.R;
import com.mashang.socialservice.domain.query.create.PostQuery;
import com.mashang.socialservice.domain.vo.CommentVo;
import com.mashang.socialservice.domain.vo.PostVo;
import com.mashang.socialservice.feign.UserServiceFeign;
import com.mashang.socialservice.mapper.PostMapper;
import com.mashang.socialservice.mapping.PostMapping;
import com.mashang.socialservice.service.ICommentService;
import com.mashang.socialservice.service.IPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements IPostService {

    private static final String LIKE_KEY_PREFIX = "social:like:post:";

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private ICommentService commentService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserServiceFeign userServiceFeign;

    @Override
    @Transactional
    public PostVo publish(PostQuery postQuery, Long userId) {
        Post post = PostMapping.INSTANCE.toEntity(postQuery);
        post.setUserId(userId);
        post.setLikeCount(0L);
        post.setCommentCount(0L);
        post.setStatus(0L);
        post.setDelFlag(0L);

        postMapper.insert(post);

        PostVo vo = new PostVo();
        vo.setPostId(post.getPostId());
        vo.setUserId(post.getUserId());
        vo.setMeetingId(post.getMeetingId());
        vo.setContent(post.getContent());
        vo.setImageUrl(post.getImageUrl());
        vo.setLikeCount(0L);
        vo.setCommentCount(0L);
        vo.setCreateTime(post.getCreateTime());
        vo.setUsername(getUsername(userId));

        return vo;
    }

    @Override
    public int delete(Long postId, Long userId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("动态不存在");
        }
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除他人动态");
        }

        post.setDelFlag(2L);
        return postMapper.updateById(post);
    }

    @Override
    public List<PostVo> listByMeetingId(Long meetingId) {
        List<PostVo> list = postMapper.selectByMeetingId(meetingId);
        if (list != null) {
            list.forEach(vo -> vo.setUsername(getUsername(vo.getUserId())));
        }
        return list;
    }

    @Override
    public PostVo detail(Long postId) {
        PostVo vo = postMapper.selectDetailById(postId);
        if (vo != null) {
            vo.setUsername(getUsername(vo.getUserId()));
            // 加载评论树
            List<CommentVo> comments = commentService.listByPostId(postId);
            vo.setComments(comments);
        }
        return vo;
    }

    @Override
    public boolean like(Long postId, Long userId) {
        String key = LIKE_KEY_PREFIX + postId;
        Boolean isMember = redisTemplate.opsForSet().isMember(key, userId.toString());
        if (Boolean.TRUE.equals(isMember)) {
            throw new RuntimeException("已经点赞过了");
        }

        redisTemplate.opsForSet().add(key, userId.toString());
        postMapper.updateLikeCount(postId, 1);
        return true;
    }

    @Override
    public boolean unlike(Long postId, Long userId) {
        String key = LIKE_KEY_PREFIX + postId;
        Long removed = redisTemplate.opsForSet().remove(key, userId.toString());
        if (removed != null && removed > 0) {
            postMapper.updateLikeCount(postId, -1);
            return true;
        }
        return false;
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
