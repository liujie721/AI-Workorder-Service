package com.aiworkorder.ai_workorder_service.model.vo;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class UserVO {
    private String id;
    private String username;
    private String role;
    private String createTime;
}