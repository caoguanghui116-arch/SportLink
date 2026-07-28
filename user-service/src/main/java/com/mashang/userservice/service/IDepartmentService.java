package com.mashang.userservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mashang.userservice.domain.entity.Department;
import com.mashang.userservice.domain.query.create.DepartmentCreateQuerry;
import com.mashang.userservice.domain.query.update.DepartmentUpdateQuerry;

import java.util.List;

public interface IDepartmentService extends IService<Department> {

    int add(DepartmentCreateQuerry department);

    int update(DepartmentUpdateQuerry department);

    int delete(Long deptId);

    List<Department> listAll();

    List<Department> listByParentId(Long parentId);
}
