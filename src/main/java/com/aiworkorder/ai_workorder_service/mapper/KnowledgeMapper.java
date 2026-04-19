package com.aiworkorder.ai_workorder_service.mapper;

import com.aiworkorder.ai_workorder_service.entity.Knowledge;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface KnowledgeMapper extends BaseMapper<Knowledge> {
    //调用方法直接调用这个SQL
    // 修改点：补充SELECT缺失的字段*
    @Select("SELECT * FROM knowledge WHERE keyword LIKE CONCAT('%', #{keyword}, '%')")
    List<Knowledge> selectByKeyword(@Param("keyword") String keyword);
}