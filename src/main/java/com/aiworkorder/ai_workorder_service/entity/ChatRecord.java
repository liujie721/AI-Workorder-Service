package com.aiworkorder.ai_workorder_service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Schema(description = "工单创建")
@Data
@TableName("chat_record")
public class ChatRecord {
    @TableId(type = IdType.AUTO)
    @Schema(description = "记录ID（自增）", example = "1")
    private Long id;

    @NotNull(message = "用户ID不能为空")
    @TableField("user_id") // 适配数据库下划线字段，不修改字段名
    @Schema(description = "用户ID", required = true, example = "1001")
    private Long userId;

    @NotNull(message = "用户提问内容不能为空")
    @TableField("user_message") // 适配数据库下划线字段
    @Schema(description = "用户提问", required = true, example = "怎么创建工单")
    private String userMessage;

    @TableField("ai_answer") // 适配数据库下划线字段
    @Schema(description = "AI 回答", example = "你可以通过...")
    private String aiAnswer;

    @TableField("create_time") // 适配数据库下划线字段
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}