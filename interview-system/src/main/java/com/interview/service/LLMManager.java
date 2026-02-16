package com.interview.service;

import com.interview.model.EvaluationDimension;
import com.interview.model.InterviewRecord;
import com.interview.model.LLMConfig;
import com.interview.model.Question;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 大语言模型管理器
 * 支持多模型配置、切换和API调用
 */
public class LLMManager {
    
    private static final String CONFIG_FILE = "llm_configs.dat";
    private static final String AES_KEY = "InterviewSystem!"; // 16字节密钥
    
    private List<LLMConfig> configs;
    private LLMConfig currentConfig;
    
    public LLMManager() {
        this.configs = new ArrayList<>();
        loadConfigs();
        
        // 如果没有配置，创建默认DeepSeek配置
        if (configs.isEmpty()) {
            createDefaultConfig();
        }
        
        // 设置当前配置为默认配置
        currentConfig = configs.stream()
                .filter(LLMConfig::isDefault)
                .findFirst()
                .orElse(configs.get(0));
    }
    
    /**
     * 创建默认配置
     */
    private void createDefaultConfig() {
        LLMConfig config = new LLMConfig();
        config.setName("默认-DeepSeek-Thinking");
        config.setProvider(LLMConfig.LLMProvider.DEEPSEEK_THINKING);
        config.setModelName("deepseek-reasoner");
        config.setApiEndpoint("https://api.deepseek.com/v1/chat/completions");
        config.setDefault(true);
        config.setEnabled(true);
        
        configs.add(config);
        saveConfigs();
    }
    
    /**
     * 添加新配置
     */
    public void addConfig(LLMConfig config) {
        // 如果设为默认，取消其他默认
        if (config.isDefault()) {
            configs.forEach(c -> c.setDefault(false));
        }
        configs.add(config);
        saveConfigs();
    }
    
    /**
     * 更新配置
     */
    public void updateConfig(LLMConfig config) {
        // 加密API Key
        if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
            config.setApiKey(encrypt(config.getApiKey()));
        }
        
        // 如果设为默认，取消其他默认
        if (config.isDefault()) {
            configs.forEach(c -> {
                if (!c.getId().equals(config.getId())) {
                    c.setDefault(false);
                }
            });
        }
        
        saveConfigs();
    }
    
    /**
     * 删除配置
     */
    public void deleteConfig(int id) {
        configs.removeIf(c -> c.getId() != null && c.getId() == id);
        saveConfigs();
    }
    
    /**
     * 切换当前配置
     */
    public void switchConfig(int id) {
        currentConfig = configs.stream()
                .filter(c -> c.getId() != null && c.getId() == id && c.isEnabled())
                .findFirst()
                .orElse(currentConfig);
    }
    
    /**
     * 调用LLM进行面试评分分析
     * 
     * @param question 题目
     * @param answerText 回答文本
     * @param dimensions 需要评分的维度
     * @return JSON格式的评分结果
     */
    public String analyzeAnswer(Question question, String answerText, 
                                List<EvaluationDimension> dimensions) {
        if (currentConfig == null || !currentConfig.isEnabled()) {
            return createErrorResponse("没有可用的LLM配置");
        }
        
        String prompt = buildEvaluationPrompt(question, answerText, dimensions);
        
        try {
            return callLLMAPI(prompt);
        } catch (Exception e) {
            return createErrorResponse("API调用失败: " + e.getMessage());
        }
    }
    
    /**
     * 构建评测Prompt
     */
    private String buildEvaluationPrompt(Question question, String answerText,
                                        List<EvaluationDimension> dimensions) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位专业的计算机科学领域面试官。请对以下面试回答进行评测。\n\n");
        prompt.append("【题目】\n").append(question.getTitle()).append("\n");
        prompt.append(question.getContent()).append("\n\n");
        prompt.append("【参考答案】\n").append(question.getAnswer()).append("\n\n");
        prompt.append("【面试者回答】\n").append(answerText).append("\n\n");
        prompt.append("【评测维度】\n");
        
        for (EvaluationDimension dim : dimensions) {
            prompt.append("- ").append(dim.getDisplayName())
                  .append("(").append(dim.getEnglishName()).append("): ")
                  .append(dim.getDescription()).append("\n");
        }
        
        prompt.append("\n请以JSON格式返回评测结果，包含以下字段：\n");
        prompt.append("{\n");
        prompt.append("  \"dimension_scores\": {\n");
        for (EvaluationDimension dim : dimensions) {
            prompt.append("    \"").append(dim.name()).append("\": <0-100的整数>,\n");
        }
        prompt.append("  },\n");
        prompt.append("  \"analysis\": \"对该回答的综合分析\",\n");
        prompt.append("  \"strengths\": [\"优势1\", \"优势2\"],\n");
        prompt.append("  \"weaknesses\": [\"不足1\", \"不足2\"],\n");
        prompt.append("  \"suggestions\": \"改进建议\"\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }
    
    /**
     * 调用LLM API
     */
    private String callLLMAPI(String prompt) throws Exception {
        String apiKey = decrypt(currentConfig.getApiKey());
        String endpoint = currentConfig.getApiEndpoint();
        
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setDoOutput(true);
        conn.setConnectTimeout(currentConfig.getTimeout() * 1000);
        conn.setReadTimeout(currentConfig.getTimeout() * 1000);
        
        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", currentConfig.getModelName());
        
        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);
        messages.put(message);
        
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 2000);
        requestBody.put("response_format", new JSONObject().put("type", "json_object"));
        
        // 发送请求
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        // 读取响应
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                
                // 解析响应获取内容
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices.length() > 0) {
                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject messageObj = choice.getJSONObject("message");
                    return messageObj.getString("content");
                }
                return createErrorResponse("API返回格式错误");
            }
        } else {
            return createErrorResponse("API调用失败，状态码: " + responseCode);
        }
    }
    
    /**
     * 创建错误响应
     */
    private String createErrorResponse(String error) {
        JSONObject errorJson = new JSONObject();
        errorJson.put("error", error);
        errorJson.put("dimension_scores", new JSONObject());
        errorJson.put("analysis", "分析失败");
        errorJson.put("strengths", new JSONArray());
        errorJson.put("weaknesses", new JSONArray());
        errorJson.put("suggestions", "");
        return errorJson.toString();
    }
    
    /**
     * 加密API Key
     */
    private String encrypt(String value) {
        try {
            SecretKeySpec key = new SecretKeySpec(AES_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            return value;
        }
    }
    
    /**
     * 解密API Key
     */
    private String decrypt(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        try {
            SecretKeySpec key = new SecretKeySpec(AES_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(value));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }
    
    /**
     * 保存配置到文件
     */
    private void saveConfigs() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(CONFIG_FILE))) {
            oos.writeObject(configs);
        } catch (IOException e) {
            System.err.println("保存LLM配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 从文件加载配置
     */
    @SuppressWarnings("unchecked")
    private void loadConfigs() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) {
            return;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(file))) {
            configs = (List<LLMConfig>) ois.readObject();
        } catch (Exception e) {
            System.err.println("加载LLM配置失败: " + e.getMessage());
            configs = new ArrayList<>();
        }
    }
    
    // Getters
    public List<LLMConfig> getAllConfigs() { return configs; }
    public LLMConfig getCurrentConfig() { return currentConfig; }
    public LLMConfig getConfigById(int id) {
        return configs.stream()
                .filter(c -> c.getId() != null && c.getId() == id)
                .findFirst()
                .orElse(null);
    }
}
