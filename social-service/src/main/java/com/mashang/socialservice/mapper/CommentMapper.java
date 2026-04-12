package com.mashang.socialservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mashang.socialservice.domain.entity.Comment;
import com.mashang.socialservice.domain.vo.CommentVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommentMapper extends BaseMapper<Comment> {

    List<CommentVo> selectByPostId(@Param("postId") Long postId);

    List<CommentVo> selectRepliesByParentId(@Param("parentId") Long parentId);

}
