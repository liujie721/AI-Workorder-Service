package com.aiworkorder.ai_workorder_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {
    private  final KnowledgeRetrievalService knowledgeRetrievalService;

    private final ChatClient client;

    public String chat(String message){
        String konow=knowledgeRetrievalService.hybridRetieval(message);
        if(konow!=null&&!konow.isBlank()){
            return "本地知识库答案：\n" + konow;
        }

        String aiChat=client.prompt()
                .user(message)
                .call()
                .content();
        return "AI 回答：\n" + aiChat;
    }
}
