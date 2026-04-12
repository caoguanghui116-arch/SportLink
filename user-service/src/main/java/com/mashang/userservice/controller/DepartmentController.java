package com.mashang.userservice.controller;

import com.mashang.userservice.domain.entity.Department;
import com.mashang.userservice.domain.entity.R;
import com.mashang.userservice.service.IDepartmentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "院系管理")
@RestController
@RequestMapping("/usercenter/department")
public class DepartmentController {

    @Autowired
    private IDepartmentService departmentService;

    @ApiOperation("添加院系")
    @PostMapping("/add")
    public R add(@RequestBody Department department) {
        return R.toResult(departmentService.add(department));
    }

    @ApiOperation("修改院系")
    @PutMapping("/update")
    public R update(@RequestBody Department department) {
        return R.toResult(departmentService.update(department));
    }

    @ApiOperation("删除院系")
    @DeleteMapping("/delete/{deptId}")
    @ApiImplicitParam(name = "deptId", value = "院系ID")
    public R delete(@PathVariable Long deptId) {
        return R.toResult(departmentService.delete(deptId));
    }

    @ApiOperation("院系列表")
    @GetMapping("/list")
    public R<List<Department>> list() {
        return R.ok(departmentService.listAll());
    }

    @ApiOperation("按父级ID查询院系")
    @GetMapping("/list/{parentId}")
    @ApiImplicitParam(name = "parentId", value = "父级ID")
    public R<List<Department>> listByParent(@PathVariable Long parentId) {
        return R.ok(departmentService.listByParentId(parentId));
    }
}
