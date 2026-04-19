package com.aiworkorder.ai_workorder_service.service;

import com.aiworkorder.ai_workorder_service.common.Result;
import com.aiworkorder.ai_workorder_service.entity.Knowledge;

import java.util.List;

public interface KnowledgeService {
    Result<String> addKnowledge(Knowledge knowledge);
    Result<String> updateKnowledge(Knowledge knowledge);
    Result<String> deleteKnowledge(Long id);
    Result<List<Knowledge>> listKnowledge();
    List<Knowledge> searchByKeyword(String keyword);
}
