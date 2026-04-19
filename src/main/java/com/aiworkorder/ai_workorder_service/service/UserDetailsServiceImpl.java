package com.aiworkorder.ai_workorder_service.service;

import com.aiworkorder.ai_workorder_service.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 明确指定自定义User实体的全限定名，避免与Spring Security的User冲突
        com.aiworkorder.ai_workorder_service.entity.User dbUser = userMapper.selectOne(
                new LambdaQueryWrapper<com.aiworkorder.ai_workorder_service.entity.User>()
                        .eq(com.aiworkorder.ai_workorder_service.entity.User::getUsername, username)
        );

        if (dbUser == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        // 返回 Security 标准用户
        return User
                .withUsername(dbUser.getUsername())
                .password(dbUser.getPassword())
                .roles(dbUser.getRole())
                .build();
    }
}