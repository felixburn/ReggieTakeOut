package com.example.reggie_take_out.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 员工实体
 */
@Data
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id; // 主键

    private String username; // 姓名

    private String name; // 用户名

    //@TableField(select = false)
    private String password; // 密码

    private String phone; // 手机号

    private String sex; // 性别

    private String idNumber; // 身份证号 id_number

    private Integer status; // 状态 0:禁用 1:正常

    //设置自动填充策略，插入时填充
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime; // 创建时间 create_time

    //设置自动填充策略，插入或更新时填充
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime; // 更新时间 update_time

    @TableField(fill = FieldFill.INSERT)
    private Long createUser; // 创建人 create_user

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser; // 修改人 update_user

}
