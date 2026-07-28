package com.mashang.userservice.controller;

import com.mashang.userservice.domain.entity.Department;
import com.mashang.common.common.R;
import com.mashang.userservice.domain.query.create.DepartmentCreateQuerry;
import com.mashang.userservice.domain.query.update.DepartmentUpdateQuerry;
import com.mashang.userservice.service.IDepartmentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 院系管理控制器
 *
 * 核心职责：
 * - 院系增删改查：管理学校各院系/部门的基本信息
 * - 按父级查询：支持树形层级结构，查询指定父级院系下的所有子院系
 *
 * 设计思路：
 * - 院系采用树形结构，通过 parentId 字段实现父子层级关系
 * - sort 字段控制同级院系的展示顺序
 * - 院系名称全局唯一，添加时进行校验
 * - 查询结果使用 Redis 缓存（Cache Aside 模式），提升高频读取场景下的性能
 */
@Api(tags = "院系管理")
@RestController
@RequestMapping("/department")
public class DepartmentController {

    /** 院系服务接口 */
    @Autowired
    private IDepartmentService departmentService;

    /**
     * 添加院系
     *
     * 添加前校验院系名称是否已存在（唯一性约束）
     *
     * @param department 院系实体，需包含 deptName、parentId、sort 等字段
     * @return 统一响应体，ok 表示添加成功
     */
    @ApiOperation("添加院系")
    @PostMapping
    public R<Void> add(@RequestBody @Validated DepartmentCreateQuerry department) {
        return R.toResult(departmentService.add(department));
    }

    /**
     * 修改院系信息
     *
     * @param department 院系实体，id 字段必填
     * @return 统一响应体，ok 表示修改成功
     */
    @ApiOperation("修改院系")
    @PutMapping
    public R<Void> update(@RequestBody @Validated DepartmentUpdateQuerry department) {
        return R.toResult(departmentService.update(department));
    }

    /**
     * 删除院系
     *
     * @param deptId 院系ID
     * @return 统一响应体，ok 表示删除成功
     */
    @ApiOperation("删除院系")
    @DeleteMapping("/{deptId}")
    @ApiImplicitParam(name = "deptId", value = "院系ID", required = true)
    public R<Void> delete(@PathVariable Long deptId) {
        return R.toResult(departmentService.delete(deptId));
    }

    /**
     * 获取所有院系列表
     *
     * 返回的列表按 sort 字段升序排列
     * 查询使用 Redis 缓存（Cache Aside 模式），TTL 60 分钟
     *
     * @return 统一响应体，data 字段为排序后的院系列表
     */
    @ApiOperation("院系列表")
    @GetMapping
    public R<List<Department>> list() {
        return R.ok(departmentService.listAll());
    }

    /**
     * 按父级ID查询子院系
     *
     * 使用场景：点击某个院系节点展开其下属子院系时调用
     * 查询结果按 sort 字段升序排列
     *
     * @param parentId 父级院系ID
     * @return 统一响应体，data 字段为该父级下的子院系列表
     */
    @ApiOperation("按父级ID查询院系")
    @GetMapping("/parent/{parentId}")
    @ApiImplicitParam(name = "parentId", value = "父级ID", required = true)
    public R<List<Department>> listByParent(@PathVariable Long parentId) {
        return R.ok(departmentService.listByParentId(parentId));
    }
}
