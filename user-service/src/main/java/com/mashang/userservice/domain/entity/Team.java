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
public class Team {

    @TableId(type = IdType.AUTO)
    private Long teamId;
    private String teamName;
    private Long deptId;
    private Long captainId;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}
