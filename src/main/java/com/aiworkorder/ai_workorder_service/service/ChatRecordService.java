package com.aiworkorder.ai_workorder_service.service;

import com.aiworkorder.ai_workorder_service.entity.ChatRecord;
import com.aiworkorder.ai_workorder_service.mapper.ChatRecordMapperDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRecordService {
    // 修正：Mapper 实际在根包下，所以导入路径和类型都要对应
    private final ChatRecordMapperDao chatRecordMapperDao;

    // 保存用户与AI对话内容
    public void saveRecord(Long userId, String userMessage, String aiAnswer) {
        ChatRecord chatRecord = new ChatRecord();
        chatRecord.setUserId(userId);
        chatRecord.setUserMessage(userMessage);
        chatRecord.setAiAnswer(aiAnswer);
        chatRecord.setCreateTime(LocalDateTime.now());
        chatRecordMapperDao.insert(chatRecord);
    }

    // 获取历史对话
    public List<ChatRecord> getHistoryByUserId(Long userId) {
        return chatRecordMapperDao.selectByUserId(userId);
    }
}