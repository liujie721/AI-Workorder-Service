package com.aiworkorder.ai_workorder_service.mapper;

import com.aiworkorder.ai_workorder_service.entity.WorkOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

// 修改点：添加@Mapper注解、public修饰
@Mapper
public interface WorkOrderMapper extends BaseMapper<WorkOrder> {
}