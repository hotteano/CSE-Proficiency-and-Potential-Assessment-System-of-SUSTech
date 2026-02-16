package com.interview.model;

import java.time.LocalDateTime;

/**
 * 大语言模型配置实体类
 */
public class LLMConfig {
    
    private Integer id;
    private String name;              // 配置名称
    private LLMProvider provider;     // 提供商
    private String modelName;         // 模型名称
    private String apiKey;            // API密钥（加密存储）
    private String apiEndpoint;       // API端点URL
    private boolean isDefault;        // 是否为默认配置
    private boolean enabled;          // 是否启用
    private int timeout;              // 超时时间（秒）
    private String params;            // 额外参数（JSON格式）
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 大模型提供商枚举
     */
    public enum LLMProvider {
        DEEPSEEK("DeepSeek", "https://api.deepseek.com/v1", "deepseek-chat"),
        DEEPSEEK_THINKING("DeepSeek-Thinking", "https://api.deepseek.com/v1", "deepseek-reasoner"),
        OPENAI("OpenAI", "https://api.openai.com/v1", "gpt-4"),
        OPENAI_GPT4_TURBO("OpenAI GPT-4 Turbo", "https://api.openai.com/v1", "gpt-4-turbo-preview"),
        AZURE_OPENAI("Azure OpenAI", "", "gpt-4"),
        ANTHROPIC("Anthropic", "https://api.anthropic.com", "claude-3-opus-20240229"),
        LOCAL("本地模型", "http://localhost:8000/v1", "local-model");
        
        private final String displayName;
        private final String defaultEndpoint;
        private final String defaultModel;
        
        LLMProvider(String displayName, String defaultEndpoint, String defaultModel) {
            this.displayName = displayName;
            this.defaultEndpoint = defaultEndpoint;
            this.defaultModel = defaultModel;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDefaultEndpoint() { return defaultEndpoint; }
        public String getDefaultModel() { return defaultModel; }
    }
    
    public LLMConfig() {
        this.createdAt = LocalDateTime.now();
        this.enabled = true;
        this.timeout = 60;
        this.isDefault = false;
    }
    
    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public LLMProvider getProvider() { return provider; }
    public void setProvider(LLMProvider provider) { this.provider = provider; }
    
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    
    public String getApiEndpoint() { return apiEndpoint; }
    public void setApiEndpoint(String apiEndpoint) { this.apiEndpoint = apiEndpoint; }
    
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public int getTimeout() { return timeout; }
    public void setTimeout(int timeout) { this.timeout = timeout; }
    
    public String getParams() { return params; }
    public void setParams(String params) { this.params = params; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getProviderDisplayName() {
        return provider != null ? provider.getDisplayName() : "";
    }
}
