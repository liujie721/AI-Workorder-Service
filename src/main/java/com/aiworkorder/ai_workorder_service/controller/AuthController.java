package com.aiworkorder.ai_workorder_service.controller;

import com.aiworkorder.ai_workorder_service.common.Result;
import com.aiworkorder.ai_workorder_service.entity.User;
import com.aiworkorder.ai_workorder_service.service.UserService;
import com.aiworkorder.ai_workorder_service.vo.LoginRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户认证控制器（登录/注册）
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor  // Spring推荐：构造器注入（替代@Autowired）
@CrossOrigin(origins = "*", maxAge = 3600)  // 跨域配置
public class AuthController {

    // 最终注入，无循环依赖，Spring官方推荐
    private final UserService userService;

    /**
     * 用户注册接口
     */
    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody User user) {
        log.info("用户注册：{}", user.getUsername());
        return userService.register(user);
    }

    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("用户登录：{}", loginRequest.getUsername());
        return userService.login(loginRequest.getUsername(), loginRequest.getPassword());
    }
}