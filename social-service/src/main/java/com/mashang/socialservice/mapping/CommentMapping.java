package com.mashang.socialservice.mapping;

import com.mashang.socialservice.domain.entity.Comment;
import com.mashang.socialservice.domain.query.create.CommentQuery;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CommentMapping {

    CommentMapping INSTANCE = Mappers.getMapper(CommentMapping.class);

    Comment toEntity(CommentQuery commentQuery);

}
