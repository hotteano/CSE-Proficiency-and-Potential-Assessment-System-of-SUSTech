package com.interview.llm;

import com.interview.model.*;
import com.interview.service.LLMConfigService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * LLM管理器 - 管理多个LLM提供商
 * 实现真实的API调用
 */
public class LLMManager {
    
    private final LLMConfigService configService;
    private final HttpClient httpClient;
    private LLMConfig currentConfig;
    
    public LLMManager(LLMConfigService configService) {
        this.configService = configService;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
        
        // 加载默认配置
        loadDefaultConfig();
    }
    
    /**
     * 加载默认配置
     */
    private void loadDefaultConfig() {
        List<LLMConfig> configs = configService.getAllConfigs();
        for (LLMConfig config : configs) {
            if (config.isDefault()) {
                this.currentConfig = config;
                // 解密API密钥
                String decryptedKey = configService.getDecryptedApiKey(config);
                this.currentConfig.setApiKey(decryptedKey);
                break;
            }
        }
    }
    
    /**
     * 设置当前使用的配置
     */
    public void setCurrentConfig(LLMConfig config) {
        // 解密API密钥
        String decryptedKey = configService.getDecryptedApiKey(config);
        config.setApiKey(decryptedKey);
        this.currentConfig = config;
    }
    
    /**
     * 发送LLM API请求 - 真实实现
     * @param prompt 提示词
     * @return LLM响应
     */
    public String callLLMAPI(String prompt) {
        if (currentConfig == null) {
            System.err.println("[LLMManager] 未配置LLM，请先配置API参数");
            return null;
        }
        
        System.out.println("[LLMManager] 调用API: " + currentConfig.getApiEndpoint());
        System.out.println("[LLMManager] 使用模型: " + currentConfig.getModelName());
        
        try {
            // 构建请求体
            String requestBody = buildRequestBody(prompt);
            System.out.println("[LLMManager] 请求体: " + requestBody.substring(0, Math.min(500, requestBody.length())) + "...");
            
            // 构建HTTP请求
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(currentConfig.getApiEndpoint()))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(120));
            
            // 添加授权头（根据不同提供商格式）
            String authHeader = buildAuthHeader();
            requestBuilder.header("Authorization", authHeader);
            
            HttpRequest request = requestBuilder
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();
            
            // 发送请求
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            // 处理响应
            if (response.statusCode() == 200) {
                String responseBody = response.body();
                System.out.println("[LLMManager] API调用成功");
                System.out.println("[LLMManager] 响应: " + responseBody.substring(0, Math.min(500, responseBody.length())) + "...");
                return parseResponse(responseBody);
            } else {
                System.err.println("[LLMManager] API调用失败，状态码: " + response.statusCode());
                System.err.println("[LLMManager] 响应: " + response.body());
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("[LLMManager] API调用异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 构建授权头
     */
    private String buildAuthHeader() {
        LLMConfig.LLMProvider provider = currentConfig.getProvider();
        String apiKey = currentConfig.getApiKey();
        
        switch (provider) {
            case DEEPSEEK:
            case DEEPSEEK_THINKING:
            case OPENAI:
            case OPENAI_GPT4_TURBO:
            case AZURE_OPENAI:
            case ANTHROPIC:
                return "Bearer " + apiKey;
            case LOCAL:
            default:
                return apiKey;
        }
    }
    
    /**
     * 构建请求体
     */
    private String buildRequestBody(String prompt) {
        LLMConfig.LLMProvider provider = currentConfig.getProvider();
        
        // 根据提供商构建不同的请求格式
        switch (provider) {
            case DEEPSEEK:
            case DEEPSEEK_THINKING:
            case OPENAI:
            case OPENAI_GPT4_TURBO:
            case AZURE_OPENAI:
            case ANTHROPIC:
            case LOCAL:
            default:
                return buildOpenAICompatibleRequest(prompt);
        }
    }
    
    /**
     * 构建OpenAI兼容格式的请求体 - 手动JSON构建
     */
    private String buildOpenAICompatibleRequest(String prompt) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"model\":\"").append(escapeJson(currentConfig.getModelName())).append("\",");
        sb.append("\"temperature\":0.7,");
        sb.append("\"max_tokens\":2048,");
        sb.append("\"messages\":[");
        sb.append("{");
        sb.append("\"role\":\"user\",");
        sb.append("\"content\":\"").append(escapeJson(prompt)).append("\"");
        sb.append("}");
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * 转义JSON字符串中的特殊字符
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
    
    /**
     * 解析响应
     */
    private String parseResponse(String responseBody) {
        try {
            // 手动解析JSON响应
            // 检查是否有错误
            if (responseBody.contains("\"error\"")) {
                String errorMsg = extractJsonValue(responseBody, "message");
                System.err.println("[LLMManager] API返回错误: " + errorMsg);
                return null;
            }
            
            // 解析标准OpenAI格式 - 提取choices[0].message.content
            if (responseBody.contains("\"choices\"")) {
                // 找到choices数组
                int choicesStart = responseBody.indexOf("\"choices\"");
                int arrayStart = responseBody.indexOf("[", choicesStart);
                int arrayEnd = responseBody.indexOf("]", arrayStart);
                
                if (arrayStart >= 0 && arrayEnd > arrayStart) {
                    String choicesContent = responseBody.substring(arrayStart + 1, arrayEnd);
                    // 找到第一个choice对象
                    int firstBrace = choicesContent.indexOf("{");
                    int lastBrace = choicesContent.indexOf("}");
                    
                    if (firstBrace >= 0 && lastBrace > firstBrace) {
                        String firstChoice = choicesContent.substring(firstBrace, lastBrace + 1);
                        
                        // 检查message或delta
                        String content = null;
                        if (firstChoice.contains("\"message\"")) {
                            String messageObj = extractJsonObject(firstChoice, "message");
                            content = extractJsonValue(messageObj, "content");
                        } else if (firstChoice.contains("\"delta\"")) {
                            String deltaObj = extractJsonObject(firstChoice, "delta");
                            content = extractJsonValue(deltaObj, "content");
                        }
                        
                        if (content != null) {
                            return content;
                        }
                    }
                }
            }
            
            // 如果解析失败，返回原始响应
            System.err.println("[LLMManager] 无法解析响应格式");
            return null;
            
        } catch (Exception e) {
            System.err.println("[LLMManager] 解析响应异常: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 从JSON字符串中提取对象的值（简化版）
     */
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex < 0) {
            return null;
        }
        
        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex < 0) {
            return null;
        }
        
        int valueStart = colonIndex + 1;
        // 跳过空白字符
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }
        
        char firstChar = json.charAt(valueStart);
        if (firstChar == '"') {
            // 字符串值
            valueStart++;
            int valueEnd = valueStart;
            StringBuilder sb = new StringBuilder();
            while (valueEnd < json.length()) {
                char c = json.charAt(valueEnd);
                if (c == '\\' && valueEnd + 1 < json.length()) {
                    // 处理转义字符
                    char next = json.charAt(valueEnd + 1);
                    switch (next) {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        case 'u': 
                            if (valueEnd + 5 < json.length()) {
                                String hex = json.substring(valueEnd + 2, valueEnd + 6);
                                sb.append((char) Integer.parseInt(hex, 16));
                                valueEnd += 4;
                            }
                            break;
                        default: sb.append(next);
                    }
                    valueEnd += 2;
                } else if (c == '"') {
                    break;
                } else {
                    sb.append(c);
                    valueEnd++;
                }
            }
            return sb.toString();
        } else if (firstChar == '{' || firstChar == '[') {
            // 对象或数组，返回原始字符串
            int braceCount = 0;
            boolean inString = false;
            for (int i = valueStart; i < json.length(); i++) {
                char c = json.charAt(i);
                if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                    inString = !inString;
                } else if (!inString) {
                    if (c == firstChar) {
                        braceCount++;
                    } else if ((firstChar == '{' && c == '}') || (firstChar == '[' && c == ']')) {
                        braceCount--;
                        if (braceCount == 0) {
                            return json.substring(valueStart, i + 1);
                        }
                    }
                }
            }
            return json.substring(valueStart);
        } else {
            // 数字、布尔值、null
            int valueEnd = valueStart;
            while (valueEnd < json.length() && 
                   json.charAt(valueEnd) != ',' && 
                   json.charAt(valueEnd) != '}') {
                valueEnd++;
            }
            return json.substring(valueStart, valueEnd).trim();
        }
    }
    
    /**
     * 从JSON字符串中提取嵌套对象
     */
    private String extractJsonObject(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex < 0) {
            return null;
        }
        
        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex < 0) {
            return null;
        }
        
        int objStart = json.indexOf("{", colonIndex);
        if (objStart < 0) {
            return null;
        }
        
        int braceCount = 0;
        boolean inString = false;
        for (int i = objStart; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inString = !inString;
            } else if (!inString) {
                if (c == '{') {
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        return json.substring(objStart, i + 1);
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * 分析面试 - 使用预先准备的面试记录进行测试
     * @param record 面试记录
     * @return 分析结果
     */
    public InterviewAnalysisResult analyzeInterview(InterviewRecord record) {
        // 使用预生成的面试文本（模拟语音识别结果）
        // 实际使用时，这里应该是从record中获取语音识别后的文本
        String interviewText = getTestInterviewText();
        
        return analyzeInterviewText(interviewText);
    }
    
    /**
     * 获取测试用的面试文本
     * 用于在语音识别完成前测试API调用
     */
    public String getTestInterviewText() {
        // 预先生成的一段面试记录
        return """
            面试官：你好，请简单介绍一下自己。
            
            候选人：您好，我是一名计算机专业的应届毕业生。在校期间，我主要学习人工智能和机器学习方向，
            参与过两个科研项目。第一个是图像分类项目，使用ResNet实现了较高的准确率；
            第二个是自然语言处理项目，做情感分析。我对深度学习框架TensorFlow和PyTorch都比较熟悉，
            也有一定的工程实践经验。
            
            面试官：你提到了ResNet，能解释一下残差连接的原理吗？
            
            候选人：好的。残差连接主要是为了解决深层网络的梯度消失问题。通过引入跳跃连接，
            让网络学习残差函数F(x) = H(x) - x，而不是直接学习H(x)。这样梯度可以更好地反向传播，
            网络可以训练得更深。我理解的数学本质是，即使F(x)趋近于0，网络至少可以保持恒等映射，
            不会比浅层网络更差。
            
            面试官：很好。你能举个数学建模的实际例子吗？
            
            候选人：可以。在推荐系统项目中，我用矩阵分解来建模用户-物品交互。
            假设评分矩阵R，分解为用户矩阵P和物品矩阵Q，目标是最小化 ||R - P*Q^T||^2 + 正则化项。
            我使用了梯度下降来优化，收敛效果不错。这个模型虽然简单，但效果比基于规则的基线好30%左右。
            
            面试官：你觉得自己的优势是什么？
            
            候选人：我认为自己有三个优势：第一是扎实的数学基础，在算法理解和建模方面比较顺手；
            第二是工程能力，能把算法快速落地；第三是学习能力，能快速跟进新技术。
            当然，我也意识到自己在系统设计和大型项目架构方面经验不足，需要继续积累。
            """;
    }
    
    /**
     * 分析面试文本
     * @param interviewText 面试转录文本
     * @return 分析结果
     */
    public InterviewAnalysisResult analyzeInterviewText(String interviewText) {
        // 构建提示词
        String prompt = buildAnalysisPrompt(interviewText);
        
        // 调用LLM
        String response = callLLMAPI(prompt);
        
        if (response == null) {
            System.err.println("[LLMManager] API调用失败，无法生成分析");
            return null;
        }
        
        // 解析JSON响应
        return parseAnalysisResult(response);
    }
    
    /**
     * 构建分析提示词
     */
    private String buildAnalysisPrompt(String interviewText) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一名专业的面试评估专家。请根据以下面试记录，对候选人进行多维度评估。\n\n");
        prompt.append("面试记录：\n");
        prompt.append(interviewText);
        prompt.append("\n\n请按照以下17个维度进行评分（0-100分），并给出评语：\n\n");
        
        for (EvaluationDimension dim : EvaluationDimension.values()) {
            prompt.append(String.format("%s (%s): ", dim.getDisplayName(), dim.getCategory().getDisplayName()));
        }
        
        prompt.append("\n\n请以JSON格式返回结果，格式如下：\n");
        prompt.append("{\n");
        prompt.append("  \"scores\": {\n");
        for (EvaluationDimension dim : EvaluationDimension.values()) {
            prompt.append(String.format("    \"%s\": {\"score\": 分数, \"comment\": \"评语\"},\n", dim.name()));
        }
        prompt.append("  },\n");
        prompt.append("  \"overall_comment\": \"整体评价\",\n");
        prompt.append("  \"strengths\": [\"优势1\", \"优势2\"],\n");
        prompt.append("  \"weaknesses\": [\"不足1\", \"不足2\"],\n");
        prompt.append("  \"suggestions\": [\"建议1\", \"建议2\"]\n");
        prompt.append("}\n\n");
        prompt.append("只返回JSON，不要其他内容。");
        
        return prompt.toString();
    }
    
    /**
     * 解析分析结果
     */
    private InterviewAnalysisResult parseAnalysisResult(String response) {
        try {
            // 提取JSON部分
            String jsonStr = extractJson(response);
            if (jsonStr == null) {
                System.err.println("[LLMManager] 无法从响应中提取JSON");
                return null;
            }
            
            InterviewAnalysisResult result = new InterviewAnalysisResult();
            
            // 解析各维度分数
            if (jsonStr.contains("\"scores\"")) {
                String scoresObj = extractJsonObject(jsonStr, "scores");
                if (scoresObj != null) {
                    for (EvaluationDimension dim : EvaluationDimension.values()) {
                        String dimName = dim.name();
                        if (scoresObj.contains("\"" + dimName + "\"")) {
                            String dimObj = extractJsonObject(scoresObj, dimName);
                            if (dimObj != null) {
                                String scoreStr = extractJsonValue(dimObj, "score");
                                String comment = extractJsonValue(dimObj, "comment");
                                if (scoreStr != null) {
                                    try {
                                        result.setDimensionScore(dim, Double.parseDouble(scoreStr));
                                    } catch (NumberFormatException e) {
                                        // 忽略解析错误
                                    }
                                }
                                if (comment != null) {
                                    result.setDimensionComment(dim, comment);
                                }
                            }
                        }
                    }
                }
            }
            
            // 解析其他字段
            if (jsonStr.contains("\"overall_comment\"")) {
                String overallComment = extractJsonValue(jsonStr, "overall_comment");
                if (overallComment != null) {
                    result.setOverallComment(overallComment);
                }
            }
            if (jsonStr.contains("\"strengths\"")) {
                List<String> strengths = extractJsonArray(jsonStr, "strengths");
                if (strengths != null) {
                    result.setStrengths(strengths);
                }
            }
            if (jsonStr.contains("\"weaknesses\"")) {
                List<String> weaknesses = extractJsonArray(jsonStr, "weaknesses");
                if (weaknesses != null) {
                    result.setWeaknesses(weaknesses);
                }
            }
            if (jsonStr.contains("\"suggestions\"")) {
                List<String> suggestions = extractJsonArray(jsonStr, "suggestions");
                if (suggestions != null) {
                    result.setSuggestions(suggestions);
                }
            }
            
            return result;
            
        } catch (Exception e) {
            System.err.println("[LLMManager] 解析分析结果异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 从JSON字符串中提取字符串数组
     */
    private List<String> extractJsonArray(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex < 0) {
            return null;
        }
        
        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex < 0) {
            return null;
        }
        
        int arrayStart = json.indexOf("[", colonIndex);
        if (arrayStart < 0) {
            return null;
        }
        
        int arrayEnd = json.indexOf("]", arrayStart);
        if (arrayEnd < 0) {
            return null;
        }
        
        String arrayContent = json.substring(arrayStart + 1, arrayEnd);
        List<String> result = new ArrayList<>();
        
        // 解析数组元素
        StringBuilder current = new StringBuilder();
        boolean inString = false;
        boolean escaped = false;
        
        for (char c : arrayContent.toCharArray()) {
            if (escaped) {
                switch (c) {
                    case '"': current.append('"'); break;
                    case '\\': current.append('\\'); break;
                    case '/': current.append('/'); break;
                    case 'b': current.append('\b'); break;
                    case 'f': current.append('\f'); break;
                    case 'n': current.append('\n'); break;
                    case 'r': current.append('\r'); break;
                    case 't': current.append('\t'); break;
                    default: current.append(c);
                }
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                if (inString) {
                    result.add(current.toString());
                    current = new StringBuilder();
                    inString = false;
                } else {
                    inString = true;
                }
            } else if (inString) {
                current.append(c);
            }
            // 跳过逗号和空白字符
        }
        
        return result;
    }
    
    /**
     * 从响应文本中提取JSON
     */
    private String extractJson(String text) {
        // 查找JSON开始和结束位置
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        
        return null;
    }
    
    /**
     * 测试API调用
     * 用于验证配置是否正确
     */
    public boolean testApiConnection() {
        String testPrompt = "请回复：API测试成功";
        String response = callLLMAPI(testPrompt);
        return response != null && !response.isEmpty();
    }
    
    /**
     * 使用预定义面试文本测试完整流程
     * 供开发测试使用
     */
    public void testFullFlowWithPredefinedText() {
        System.out.println("\n========== LLM测试流程 ==========");
        System.out.println("使用预定义面试文本测试...\n");
        
        String testText = getTestInterviewText();
        System.out.println("面试文本:\n" + testText);
        
        System.out.println("\n正在调用LLM API...");
        InterviewAnalysisResult result = analyzeInterviewText(testText);
        
        if (result != null) {
            System.out.println("\n========== 分析结果 ==========");
            System.out.println("各维度分数:");
            for (EvaluationDimension dim : EvaluationDimension.values()) {
                Double score = result.getDimensionScores().get(dim);
                System.out.printf("  %s: %.1f%n", dim.getDisplayName(), score);
            }
            System.out.println("\n整体评价: " + result.getOverallComment());
            System.out.println("优势: " + result.getStrengths());
            System.out.println("不足: " + result.getWeaknesses());
            System.out.println("建议: " + result.getSuggestions());
            System.out.println("\n测试完成！");
        } else {
            System.err.println("测试失败，请检查API配置和网络连接");
        }
    }
    
    /**
     * 主函数 - 用于测试
     */
    public static void main(String[] args) {
        // 测试用：创建服务并测试
        LLMConfigService service = new LLMConfigService();
        LLMManager manager = new LLMManager(service);
        
        // 使用预定义文本测试完整流程
        manager.testFullFlowWithPredefinedText();
    }
}
