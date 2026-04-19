package com.aiworkorder.ai_workorder_service.service;

import com.aiworkorder.ai_workorder_service.common.BusinessException;
import com.aiworkorder.ai_workorder_service.common.Result;
import com.aiworkorder.ai_workorder_service.entity.WorkOrder;
import com.aiworkorder.ai_workorder_service.mapper.WorkOrderMapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WorkOrderServiceImpl implements WorkOrderService {
    private final WorkOrderMapper workOrderMapper;
    private final ObjectMapper objectMapper;
    private final AiRemoteClient aiRemoteClient;

    @Override
    public Result<Long> createOrder(WorkOrder workOrder) {
        workOrder.setStatus("PENDING");
        workOrder.setCreateTime(LocalDateTime.now());
        workOrderMapper.insert(workOrder);
        return Result.success(workOrder.getId());
    }

    @Override
    public Result<WorkOrder> getOrderById(Long id) {
        WorkOrder workOrder = workOrderMapper.selectById(id);
        if (workOrder == null) {
            return Result.fail("工单不存在");
        }
        return Result.success(workOrder);
    }

    @Override
    public Result<String> updateOrderStatus(Long id, String status) {
        WorkOrder workOrder = workOrderMapper.selectById(id);
        if (workOrder == null) {
            return Result.fail("工单不存在");
        }

        // 修复：明确状态流转规则，适配数据库存储的字符串
        boolean allowed = ("PENDING".equals(workOrder.getStatus()) && "PROCESSING".equals(status))
                || ("PROCESSING".equals(workOrder.getStatus()) && "CLOSED".equals(status));

        if (!allowed) {
            return Result.fail("状态不合法，仅支持：PENDING→PROCESSING→CLOSED");
        }

        UpdateWrapper<WorkOrder> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", id).set("status", status);
        workOrderMapper.update(null, wrapper);
        return Result.success("状态更新成功");
    }

    @Override
    public Long autoCreateWorkOrder(Long userId, String question) {
        String aiJson = aiRemoteClient.callAi(question);
        WorkOrder order = parseAiResult(aiJson);

        // 填充必填字段，适配数据库非空约束
        order.setId(null);
        order.setUserId(userId);
        order.setContent(question);
        order.setStatus("PENDING");
        order.setCreateTime(LocalDateTime.now());

        workOrderMapper.insert(order);
        return order.getId();
    }

    @Override
    public WorkOrder parseAiResult(String json) {
        if (json == null || json.isBlank()) {
            throw new BusinessException("AI 返回内容为空"); // 改用自定义业务异常
        }

        try {
            return objectMapper.readValue(json, WorkOrder.class);
        } catch (Exception e) {
            throw new BusinessException("AI 返回格式错误：" + json);
        }
    }
}