package com.aiworkorder.ai_workorder_service.service;

import com.aiworkorder.ai_workorder_service.config.JwtUtil;
import com.aiworkorder.ai_workorder_service.entity.User;
import com.aiworkorder.ai_workorder_service.mapper.UserMapper;
import com.aiworkorder.ai_workorder_service.model.dto.LoginDTO;
import com.aiworkorder.ai_workorder_service.model.dto.RegisterDTO;
import com.aiworkorder.ai_workorder_service.model.vo.LoginVO;
import com.aiworkorder.ai_workorder_service.model.vo.UserVO;
import com.aiworkorder.ai_workorder_service.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(RegisterDTO registerDTO) {
        // 1. 参数验证
        validateRegisterDTO(registerDTO);

        // 2. 检查用户名是否已存在
        if (!isUsernameAvailable(registerDTO.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 3. 创建用户对象
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setRole("USER");
        user.setCreatetime(LocalDateTime.now().toString());

        // 4. 保存用户
        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new RuntimeException("用户注册失败，请重试");
        }

        log.info("用户注册成功: id={}, username={}", user.getId(), user.getUsername());

        // 5. 返回用户信息
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setRole(user.getRole());
        return userVO;
    }

    /**
     * 用户登录
     */
    public LoginVO login(LoginDTO loginDTO) {
        // 1. 参数验证
        if (!StringUtils.hasText(loginDTO.getUsername()) ||
                !StringUtils.hasText(loginDTO.getPassword())) {
            throw new RuntimeException("用户名和密码不能为空");
        }

        try {
            // 2. 使用 Spring Security 进行认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getUsername(),
                            loginDTO.getPassword()
                    )
            );

            // 3. 设置认证信息到 SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 4. 获取用户信息
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // 5. 查询数据库中的完整用户信息
            User user = userMapper.findByUsername(loginDTO.getUsername());
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            // 6. 生成 JWT Token
            String token = jwtUtil.generateToken(
                    user.getUsername(),
                    userDetails.getAuthorities()
            );

            // 7. 将 Token 存入 Redis
            saveTokenToRedis(user.getUsername(), token);

            // 8. 构建返回结果
            LoginVO loginVO = new LoginVO();
            loginVO.setToken(token);
            loginVO.setTokenType("Bearer");
            loginVO.setExpiresIn(jwtUtil.getExpirationFromToken(token).getTime());

            LoginVO.UserVO userInfo = new LoginVO.UserVO();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setRoles(List.of(user.getRole()));
            loginVO.setUser(userInfo);

            return loginVO;

        } catch (BadCredentialsException e) {
            log.warn("登录失败: 用户名或密码错误, username={}", loginDTO.getUsername());
            throw new RuntimeException("用户名或密码错误");
        } catch (AuthenticationException e) {
            log.warn("登录失败: {}, username={}", e.getMessage(), loginDTO.getUsername());
            throw new RuntimeException("认证失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("登录异常: {}, username={}", e.getMessage(), loginDTO.getUsername(), e);
            throw new RuntimeException("登录失败，请重试");
        }
    }

    /**
     * 用户登出
     */
    public void logout(String token) {
        try {
            // 1. 从 Token 中提取用户名
            String username = jwtUtil.getUsernameFromToken(token);

            // 2. 从 Redis 中删除 Token
            redisUtil.delete("auth:token:" + username);

            // 3. 清除 SecurityContext
            SecurityContextHolder.clearContext();

            log.info("用户登出成功: username={}", username);
        } catch (Exception e) {
            log.warn("登出时发生异常: {}", e.getMessage());
        }
    }

    /**
     * 获取当前登录用户信息
     */
    public UserVO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("用户未登录");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            User user = userMapper.findByUsername(username);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            UserVO userVO = new UserVO();
            userVO.setId(user.getId());
            userVO.setUsername(user.getUsername());
            userVO.setRole(user.getRole());
            return userVO;
        } else {
            throw new RuntimeException("获取用户信息失败");
        }
    }

    /**
     * 检查用户名是否可用
     */
    public boolean isUsernameAvailable(String username) {
        if (!StringUtils.hasText(username)) {
            return false;
        }
        return userMapper.findByUsername(username) == null;
    }

    /**
     * 验证注册参数
     */
    private void validateRegisterDTO(RegisterDTO dto) {
        if (!StringUtils.hasText(dto.getUsername())) {
            throw new RuntimeException("用户名不能为空");
        }

        if (dto.getUsername().length() < 3 || dto.getUsername().length() > 20) {
            throw new RuntimeException("用户名长度必须在3-20个字符之间");
        }

        if (!StringUtils.hasText(dto.getPassword())) {
            throw new RuntimeException("密码不能为空");
        }

        if (dto.getPassword().length() < 6 || dto.getPassword().length() > 20) {
            throw new RuntimeException("密码长度必须在6-20个字符之间");
        }
    }

    /**
     * 保存 Token 到 Redis
     */
    private void saveTokenToRedis(String username, String token) {
        try {
            // Token 有效期
            Date expiration = jwtUtil.getExpirationFromToken(token);
            long ttl = expiration.getTime() - System.currentTimeMillis();

            // 保存到 Redis
            redisUtil.set("auth:token:" + username, token, ttl, TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            log.warn("保存Token到Redis失败: {}", e.getMessage());
        }
    }
}