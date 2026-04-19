package com.aiworkorder.ai_workorder_service.service;

import com.aiworkorder.ai_workorder_service.common.Result;
import com.aiworkorder.ai_workorder_service.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    // 注册账号
    Result<String> register(User user);

    // 登录账号
    Result<String> login(String username, String password);

    // 检查用户名是否可用
    boolean isUsernameAvailable(String username);

    // 获取当前登录用户
    User getCurrentUser();

    User findByUsername(String username);

    // 登出
    void logout(String token);
}