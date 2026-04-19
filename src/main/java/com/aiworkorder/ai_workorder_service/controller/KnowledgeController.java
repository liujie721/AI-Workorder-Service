package com.aiworkorder.ai_workorder_service.controller;

import com.aiworkorder.ai_workorder_service.common.Result;
import com.aiworkorder.ai_workorder_service.entity.Knowledge;
import com.aiworkorder.ai_workorder_service.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kb")
@CrossOrigin(origins = "*") // 🔥 修复1：添加跨域（前端必开，否则无法访问）
@RequiredArgsConstructor // 🔥 修复2：标准构造器注入，简化代码
public class KnowledgeController {

    // 移除@Autowired，使用lombok注入
    private final KnowledgeService knowledgeService;

    // 添加知识库
    @PostMapping("/add")
    public Result<String> add(@RequestBody Knowledge knowledge){
        return knowledgeService.addKnowledge(knowledge);
    }

    // 修改知识库（RESTful规范：PUT）
    @PostMapping("/update")
    public Result<String> update(@RequestBody Knowledge knowledge){
        return knowledgeService.updateKnowledge(knowledge);
    }

    // 🔥 修复3：删除接口参数错误 @RequestBody → @RequestParam
    // 前端传普通参数，不是JSON，用@RequestParam接收
    @PostMapping("/delete")
    public Result<String> delete(@RequestParam Long id){
        return knowledgeService.deleteKnowledge(id);
    }

    // 查询知识库列表
    @PostMapping("/list")
    public Result<List<Knowledge>> list(){
        return knowledgeService.listKnowledge();
    }

    // 关键词搜索
    @GetMapping("/search")
    public Result<List<Knowledge>> search(@RequestParam String keyword){
        return Result.success(knowledgeService.searchByKeyword(keyword));
    }
}