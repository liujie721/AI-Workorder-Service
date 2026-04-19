package com.aiworkorder.ai_workorder_service.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class AiRemoteClient {

    // 从配置文件读取硅基流动的 API Key（复用 spring.ai.openai.api-key）
    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    // 硅基流动的完整 API 地址（base-url + /chat/completions）
    @Value("${spring.ai.openai.base-url}/chat/completions")
    private String apiUrl;

    // 从配置读取模型名称（例如 deepseek-ai/DeepSeek-V2-Chat）
    @Value("${spring.ai.openai.chat.options.model}")
    private String modelName;

    public String callAi(String question) {
        String prompt = """
                你是智能工单分类助手。
                规则：
                1. 只返回标准JSON，不要任何解释、文字、代码块。
                2. 格式严格固定：{"type":"","level":"low/medium/high"}
                3. type 只能选：登录问题、功能异常、账号问题、咨询建议、其他
                4. level 只能选：low、medium、high
                用户问题：%s
                """.formatted(question);

        try {
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("model", modelName);                    // 使用配置的模型
            bodyMap.put("messages", new Object[]{
                    Map.of("role", "user", "content", prompt)
            });
            bodyMap.put("temperature", 0.7);                    // 可选，稳定输出

            String resp = HttpRequest.post(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(20000)                             // 20秒超时，避免长时间阻塞
                    .body(JSONUtil.toJsonStr(bodyMap))
                    .execute()
                    .body();

            JSONObject result = JSONUtil.parseObj(resp);
            return result.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getStr("content");

        } catch (Exception e) {
            throw new RuntimeException("硅基流动 AI 调用失败：" + e.getMessage());
        }
    }
}