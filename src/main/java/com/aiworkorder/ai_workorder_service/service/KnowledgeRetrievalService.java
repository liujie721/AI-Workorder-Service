package com.aiworkorder.ai_workorder_service.service;

import com.aiworkorder.ai_workorder_service.entity.Knowledge;
import com.aiworkorder.ai_workorder_service.mapper.KnowledgeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeRetrievalService {
    private final KnowledgeMapper knowledgeMapper;

    //根据问题查数据库，并返回结果
    public String hybridRetieval(String message){
        List<Knowledge> list=knowledgeMapper.selectByKeyword(message);
        // 新增：空指针判断
        if (list == null || list.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Knowledge knowledge : list) {
            sb.append(knowledge.getContent()).append("\n");
        }
        return sb.toString();
    }
}