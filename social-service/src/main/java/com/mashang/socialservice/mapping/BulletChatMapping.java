package com.mashang.socialservice.mapping;

import com.mashang.socialservice.domain.entity.BulletChat;
import com.mashang.socialservice.domain.query.create.BulletChatQuery;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BulletChatMapping {

    BulletChatMapping INSTANCE = Mappers.getMapper(BulletChatMapping.class);

    BulletChat toEntity(BulletChatQuery bulletChatQuery);

}
