package com.aiworkorder.ai_workorder_service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@TableName("user") // 表名
@Schema(description = "用户")
public class User {

    @Schema(description = "用户ID")
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @Schema(description = "用户账号", required = true)
    @NotBlank(message = "用户账号不能为空")
    @Length(min = 3, max = 20, message = "用户账号长度3-20位")
    @TableField("username") // ✅ 映射到数据库的 username 列
    private String username; // Java 字段名

    @Schema(description = "登入密码")
    @NotBlank(message = "登入密码不能为空")
    @Length(min = 6, max = 20, message = "密码长度6-20位")
    private String password;

    @Schema(description = "角色")
    private String role;

    @Schema(description = "创建时间")
    private String createtime;
}