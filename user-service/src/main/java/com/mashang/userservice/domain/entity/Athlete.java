package com.mashang.userservice.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor

/**
 * 运动员实体
 */
public class Athlete {

    @TableId(type = IdType.AUTO)
    private Long athleteId;
    private Long deptId;
    private Long userId;
    private String stuNo;
    private String name;
    private String gender;
    private String phone;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}
