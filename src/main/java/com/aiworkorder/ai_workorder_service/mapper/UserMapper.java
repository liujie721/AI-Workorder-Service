package com.aiworkorder.ai_workorder_service.mapper;

import com.aiworkorder.ai_workorder_service.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * ✅ 修复：直接使用 username 列名（不是关键字，不用反引号）
     */
    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(String id);
}