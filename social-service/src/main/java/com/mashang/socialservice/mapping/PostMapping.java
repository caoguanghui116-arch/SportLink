package com.mashang.socialservice.mapping;

import com.mashang.socialservice.domain.entity.Post;
import com.mashang.socialservice.domain.query.create.PostQuery;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PostMapping {

    PostMapping INSTANCE = Mappers.getMapper(PostMapping.class);

    Post toEntity(PostQuery postQuery);

}
