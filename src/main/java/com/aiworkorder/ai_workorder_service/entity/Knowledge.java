package com.aiworkorder.ai_workorder_service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("knowledge")
@Schema(description = "知识库实体")
public class Knowledge {
    @TableId(type = IdType.AUTO)
    @Schema(description = "ID")
    private Long id;

    @NotBlank(message = "标题不能为空")
    @Size(min = 2, max = 100, message = "标题长度必须在2-100个字符之间")
    @TableField("title")
    @Schema(description = "标题", required = true)
    private String title;

    @NotBlank(message = "内容不能为空")
    @TableField("content")
    @Schema(description = "内容", required = true)
    private String content;

    @TableField("keyword")
    @Schema(description = "关键词")
    private String keyword;

    @TableField("create_time") // 适配数据库下划线字段
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}