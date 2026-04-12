package com.mashang.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.userservice.domain.entity.Department;
import com.mashang.userservice.mapper.DepartmentMapper;
import com.mashang.userservice.service.IDepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements IDepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Override
    public int add(Department department) {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getDeptName, department.getDeptName());
        if (departmentMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("院系名称已存在");
        }
        return departmentMapper.insert(department);
    }

    @Override
    public int update(Department department) {
        return departmentMapper.updateById(department);
    }

    @Override
    public int delete(Long deptId) {
        return departmentMapper.deleteById(deptId);
    }

    @Override
    public List<Department> listAll() {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Department::getSort);
        return departmentMapper.selectList(wrapper);
    }

    @Override
    public List<Department> listByParentId(Long parentId) {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getParentId, parentId)
                .orderByAsc(Department::getSort);
        return departmentMapper.selectList(wrapper);
    }
}
