package com.aiworkorder.ai_workorder_service.service;

import com.aiworkorder.ai_workorder_service.entity.ChatRecord;
import com.aiworkorder.ai_workorder_service.entity.Knowledge;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiChatService {

    private final ChatClient chatClient;
    private final ChatRecordService chatRecordService;
    // 🔥 新增：注入知识库服务（必须加，否则混合检索用不了）
    private final KnowledgeService knowledgeService;

    // 单轮对话（原有，不动）
    public String singleChat(Long userId, String message) {
        String aiAnswer = chatClient.prompt()
                .user(message)
                .call()
                .content();

        chatRecordService.saveRecord(userId, message, aiAnswer);
        return aiAnswer;
    }

    // 拼接历史对话（原有，不动）
    public String buildHistoryContent(List<ChatRecord> historyList) {
        if (historyList == null || historyList.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("历史对话：\n");
        for (ChatRecord record : historyList) {
            sb.append("用户：").append(record.getUserMessage()).append("\n");
            sb.append("AI：").append(record.getAiAnswer()).append("\n");
        }
        sb.append("\n请根据以上历史对话，回答用户问题：");
        return sb.toString();
    }

    // 多轮对话（原有，不动）
    public String conversation(Long userId, String userMessage) {
        List<ChatRecord> historyList = chatRecordService.getHistoryByUserId(userId);
        String context = buildHistoryContent(historyList);
        String fullPrompt = context + userMessage;

        String aiAnswer = chatClient.prompt()
                .user(fullPrompt)
                .call()
                .content();

        chatRecordService.saveRecord(userId, userMessage, aiAnswer);
        return aiAnswer;
    }

    // ========================
    // 🔥 【缺失的核心方法】解决爆红！混合检索：先查知识库，再调AI
    // ========================
    public String hybridRetrieval(String question) {
        // 1. 先查询知识库
        List<Knowledge> knowledgeList = knowledgeService.searchByKeyword(question);

        // 2. 如果有匹配结果，直接返回知识库内容
        if (!knowledgeList.isEmpty()) {
            return buildKnowledgeResult(knowledgeList);
        }

        // 3. 无匹配结果，调用AI大模型回答
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }

    // ========================
    // 🔥 【配套工具方法】拼接知识库结果为文本
    // ========================
    private String buildKnowledgeResult(List<Knowledge> knowledgeList) {
        StringBuilder sb = new StringBuilder();
        sb.append("为您找到相关答案：\n");
        for (Knowledge knowledge : knowledgeList) {
            sb.append("• ").append(knowledge.getContent()).append("\n");
        }
        return sb.toString();
    }
}