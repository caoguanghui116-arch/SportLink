package com.mashang.userservice.mapping;

import com.mashang.userservice.domain.entity.Department;
import com.mashang.userservice.domain.query.create.DepartmentCreateQuerry;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DepartmentMapping {

    DepartmentMapping INSTANCE= Mappers.getMapper(DepartmentMapping.class);

    //院系添加转实体
    Department department(DepartmentCreateQuerry createQuerry);
}
