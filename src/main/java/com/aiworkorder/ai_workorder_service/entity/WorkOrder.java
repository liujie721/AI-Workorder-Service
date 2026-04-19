package com.aiworkorder.ai_workorder_service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("work_order")
@Schema(description = "工单实体")
public class WorkOrder {
    @TableId(type = IdType.AUTO)
    @Schema(description = "工单Id")
    private Long id;

    @NotNull(message = "用户ID不能为空")
    @TableField("user_id") // 适配数据库下划线字段
    @Schema(description = "用户ID", required = true, example = "1001")
    private Long userId;

    @NotBlank(message = "工单类型不能为空")
    @TableField("type")
    @Schema(description = "工单类型", required = true, example = "故障报修")
    private String type;

    @NotBlank(message = "优先级不能为空")
    @TableField("level")
    @Schema(description = "优先级", required = true, example = "高")
    private String level;

    @NotBlank(message = "工单内容不能为空")
    @TableField("content")
    @Schema(description = "工单内容", required = true, example = "系统无法登录")
    private String content;

    @TableField("status")
    @Schema(description = "状态", example = "待处理")
    private String status;

    @TableField("create_time") // 适配数据库下划线字段
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}