package com.aiworkorder.ai_workorder_service.service.impl;

import com.aiworkorder.ai_workorder_service.common.Result;
import com.aiworkorder.ai_workorder_service.config.JwtUtil;
import com.aiworkorder.ai_workorder_service.entity.User;
import com.aiworkorder.ai_workorder_service.mapper.UserMapper;
import com.aiworkorder.ai_workorder_service.service.UserService;
import com.aiworkorder.ai_workorder_service.utils.RedisUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Primary
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private org.springframework.context.ApplicationContext context; // 统一容器

    // ✅ 延迟获取：切断构造时依赖链
    private PasswordEncoder getPasswordEncoder() {
        return context.getBean(PasswordEncoder.class);
    }

    private AuthenticationManager getAuthManager() {
        return context.getBean(AuthenticationManager.class);
    }

    private JwtUtil getJwtUtil() {
        return context.getBean(JwtUtil.class); // 用到时才加载
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
        if (user == null) throw new UsernameNotFoundException("用户不存在");

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }

    @Override
    public Result<String> register(User user) {
        try {
            // 参数校验
            if (user == null || !StringUtils.hasText(user.getUsername()) || !StringUtils.hasText(user.getPassword())) {
                return Result.fail("用户名和密码不能为空");
            }
            if (user.getUsername().length() < 3 || user.getUsername().length() > 20) {
                return Result.fail("用户名长度需为3-20位");
            }
            if (user.getPassword().length() < 6 || user.getPassword().length() > 20) {
                return Result.fail("密码长度需为6-20位");
            }

            // 查重
            User existUser = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername())
            );
            if (existUser != null) return Result.fail("用户名已存在");

            // ✅ 使用运行时获取的编码器
            user.setPassword(getPasswordEncoder().encode(user.getPassword()));
            user.setRole("USER");
            user.setCreatetime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            int rows = userMapper.insert(user);
            if (rows <= 0) return Result.fail("注册失败");

            log.info("✅ 注册成功: username={}", user.getUsername());
            return Result.success("注册成功");

        } catch (Exception e) {
            log.error("❌ 注册异常", e);
            return Result.fail("注册失败: " + e.getMessage());
        }
    }

    @Override
    public Result<String> login(String username, String password) {
        try {
            if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
                return Result.fail("用户名和密码不能为空");
            }

            // ✅ 延迟调用认证管理器
            Authentication auth = getAuthManager().authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            SecurityContextHolder.getContext().setAuthentication(auth);

            // ✅ 延迟调用 JwtUtil
            String token = getJwtUtil().generateToken(username);
            saveTokenToRedis(username, token);

            return Result.success(token);

        } catch (BadCredentialsException e) {
            return Result.fail(401, "用户名或密码错误");
        } catch (Exception e) {
            log.error("登录异常", e);
            return Result.fail("登录失败: " + e.getMessage());
        }
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return !StringUtils.hasText(username) ? false :
                userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) == null;
    }

    @Override
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new RuntimeException("用户未登录");

        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        }
        return null;
    }

    @Override
    public User findByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Override
    public void logout(String token) {
        try {
            if (StringUtils.hasText(token)) {
                // ✅ 延迟调用 JwtUtil
                String username = getJwtUtil().getUsernameFromToken(token);
                redisUtil.delete("auth:token:" + username);
                SecurityContextHolder.clearContext();
            }
        } catch (Exception e) {
            log.warn("登出异常", e);
        }
    }

    private void saveTokenToRedis(String username, String token) {
        try {
            // ✅ 延迟调用 JwtUtil
            Date exp = getJwtUtil().getExpirationFromToken(token);
            long ttl = exp.getTime() - System.currentTimeMillis();
            if (ttl > 0) redisUtil.set("auth:token:" + username, token, ttl, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("保存 Token 失败", e);
        }
    }
}