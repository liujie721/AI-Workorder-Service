package com.aiworkorder.ai_workorder_service.controller;

import com.aiworkorder.ai_workorder_service.common.Result;
import com.aiworkorder.ai_workorder_service.entity.WorkOrder;
import com.aiworkorder.ai_workorder_service.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/order")
@CrossOrigin(origins = "*") // 🔥 修复1：添加跨域，前端必开！
@RequiredArgsConstructor // 🔥 修复2：Lombok构造器注入，简化代码
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    // 创建工单（正常，无需修改）
    @PostMapping("/create")
    public Result<Long> create(@RequestBody WorkOrder workOrder) {
        return workOrderService.createOrder(workOrder);
    }

    // 查询工单（正常）
    @GetMapping("/{id}")
    public Result<WorkOrder> getById(@PathVariable Long id) {
        return workOrderService.getOrderById(id);
    }

    // 修改状态（正常）
    @PutMapping("/status")
    public Result<String> updateStatus(
            @RequestParam Long id,
            @RequestParam String status
    ) {
        return workOrderService.updateOrderStatus(id, status);
    }

    // 🔥 修复3：@RequestParam → @RequestBody 接收前端JSON参数
    // AI自动创建工单
    @PostMapping("/auto-create")
    public Result<Long> autoCreateOrder(@RequestBody Map<String, Object> param) {
        Long userId = Long.valueOf(param.get("userId").toString());
        String question = (String) param.get("question");
        Long orderId = workOrderService.autoCreateWorkOrder(userId, question);
        return Result.success(orderId);
    }
}