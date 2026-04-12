package com.mashang.registrationservice.mapping;

import com.mashang.registrationservice.domain.entity.PersonalEntry;
import com.mashang.registrationservice.domain.entity.TeamEntry;
import com.mashang.registrationservice.domain.query.create.PersonalEntryQuery;
import com.mashang.registrationservice.domain.query.create.TeamEntryQuery;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PersonalEntryMapping {

    PersonalEntryMapping INSTANCE = Mappers.getMapper(PersonalEntryMapping.class);

    PersonalEntry toEntity(PersonalEntryQuery query);

    TeamEntry toEntity(TeamEntryQuery query);
}
