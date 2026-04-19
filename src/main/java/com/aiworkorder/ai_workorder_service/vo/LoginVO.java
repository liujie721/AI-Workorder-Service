package com.aiworkorder.ai_workorder_service.model.vo;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class LoginVO {

    private String token;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserVO user;

    @Data
    public static class UserVO {
        private String id;
        private String username;
        private String email;
        private String nickname;
        private String avatar;
        private List<String> roles;
    }
}