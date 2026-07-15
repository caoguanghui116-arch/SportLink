package com.mashang.userservice.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
/**
 * 菜单权限实体
 */
public class SysMenu {

    @TableId(type = IdType.AUTO)
    private Long menuId;
    private String menuName;
    private String path;
    private String perms;
    private Long parentId;
    private Integer sort;
}
