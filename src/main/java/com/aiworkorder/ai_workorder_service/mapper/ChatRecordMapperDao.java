package com.aiworkorder.ai_workorder_service.mapper;

import com.aiworkorder.ai_workorder_service.entity.ChatRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

// 注意：这里不加 @Mapper！！！
public interface ChatRecordMapperDao extends BaseMapper<ChatRecord> {

    @Select("SELECT * FROM chat_record WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<ChatRecord> selectByUserId(Long userId);
}