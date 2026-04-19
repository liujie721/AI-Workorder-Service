package com.aiworkorder.ai_workorder_service.service;

import com.aiworkorder.ai_workorder_service.common.Result;
import com.aiworkorder.ai_workorder_service.entity.WorkOrder;

public interface WorkOrderService {
    //创建工单
    Result<Long> createOrder(WorkOrder workOrder);

    //根据Id查询
    Result<WorkOrder> getOrderById(Long id);

    //修改状态
    Result<String> updateOrderStatus(Long id,String status);

    // 修改点：同步方法名
    //自动创建工单，返回工单id
    Long autoCreateWorkOrder(Long userId,String question);

    //返回工单
    WorkOrder parseAiResult(String json);
}