package com.aiworkorder.ai_workorder_service.service;

import com.aiworkorder.ai_workorder_service.common.Result;
import com.aiworkorder.ai_workorder_service.entity.Knowledge;
import com.aiworkorder.ai_workorder_service.mapper.KnowledgeMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

// 修改点：修正类名Servicelmpl→ServiceImpl
@Service
public class KnowledgeServiceImpl implements KnowledgeService {

    @Autowired
    private KnowledgeMapper knowledgeMapper;

    // ====================== 新增：清空缓存 ======================
    @CacheEvict(value = "knowledge", allEntries = true)
    @Override
    public Result<String> addKnowledge(Knowledge knowledge) {
        int row = knowledgeMapper.insert(knowledge);
        return row > 0 ? Result.success("添加成功") : Result.fail("添加失败");
    }

    // ====================== 修改：清空缓存 ======================
    @CacheEvict(value = "knowledge", allEntries = true)
    @Override
    public Result<String> updateKnowledge(Knowledge knowledge) {
        int row = knowledgeMapper.updateById(knowledge);
        return row > 0 ? Result.success("修改成功") : Result.fail("修改失败");
    }

    // ====================== 删除：清空缓存 ======================
    @CacheEvict(value = "knowledge", allEntries = true)
    @Override
    public Result<String> deleteKnowledge(Long id) {
        int row = knowledgeMapper.deleteById(id);
        return row > 0 ? Result.success("删除成功") : Result.fail("删除失败");
    }

    // ====================== 查询全部 ======================
    @Override
    public Result<List<Knowledge>> listKnowledge() {
        List<Knowledge> list = knowledgeMapper.selectList(
                new LambdaQueryWrapper<Knowledge>()
                        .orderByDesc(Knowledge::getId)
        );
        return Result.success(list);
    }

    // ====================== 关键词查询：加入缓存 ======================
    @Cacheable(value = "knowledge", key = "#keyword")
    @Override
    public List<Knowledge> searchByKeyword(String keyword) {
        return knowledgeMapper.selectByKeyword(keyword);
    }
}