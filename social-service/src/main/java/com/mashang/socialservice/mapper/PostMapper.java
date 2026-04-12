package com.mashang.socialservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mashang.socialservice.domain.entity.Post;
import com.mashang.socialservice.domain.vo.PostVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PostMapper extends BaseMapper<Post> {

    List<PostVo> selectByMeetingId(@Param("meetingId") Long meetingId);

    PostVo selectDetailById(@Param("postId") Long postId);

    int updateLikeCount(@Param("postId") Long postId, @Param("increment") int increment);

    int updateCommentCount(@Param("postId") Long postId, @Param("increment") int increment);

}
