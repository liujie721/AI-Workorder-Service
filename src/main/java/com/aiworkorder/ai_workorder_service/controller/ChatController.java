package com.aiworkorder.ai_workorder_service.controller;

import com.aiworkorder.ai_workorder_service.common.Result;
import com.aiworkorder.ai_workorder_service.entity.ChatRecord;
import com.aiworkorder.ai_workorder_service.service.AiChatService;
import com.aiworkorder.ai_workorder_service.service.ChatRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 🔥 修复1：添加跨域（前端必需要！）
public class ChatController {

    private final AiChatService aiChatService;
    private final ChatRecordService chatRecordService;

    // 🔥 修复2：@RequestParam → @RequestBody 接收JSON（前端传的是JSON）
    // 单轮对话 + 保存历史
    @PostMapping("/single")
    public Result<String> singleChat(@RequestBody Map<String, Object> param) {
        Long userId = Long.valueOf(param.get("userId").toString());
        String message = (String) param.get("message");
        String answer = aiChatService.singleChat(userId, message);
        return Result.success(answer);
    }

    // 🔥 修复3：@RequestParam → @RequestBody 接收JSON
    // 多轮对话
    @PostMapping("/conversation")
    public Result<String> conversation(@RequestBody Map<String, Object> param) {
        Long userId = Long.valueOf(param.get("userId").toString());
        String message = (String) param.get("message");
        String answer = aiChatService.conversation(userId, message);
        return Result.success(answer);
    }

    // 查询历史记录（GET请求，保留@RequestParam不变）
    @GetMapping("/history")
    public Result<List<ChatRecord>> history(@RequestParam Long userId) {
        return Result.success(chatRecordService.getHistoryByUserId(userId));
    }

    // 混合检索（保持不变）
    @PostMapping("/retrieval")
    public Result<String> retrieval(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        return Result.success(aiChatService.hybridRetrieval(message));
    }
}