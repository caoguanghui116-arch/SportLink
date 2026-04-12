package com.mashang.userservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mashang.userservice.domain.entity.Department;

import java.util.List;

public interface IDepartmentService extends IService<Department> {

    int add(Department department);

    int update(Department department);

    int delete(Long deptId);

    List<Department> listAll();

    List<Department> listByParentId(Long parentId);
}
