package com.interview.service;

import com.interview.dao.LLMConfigDao;
import com.interview.model.LLMConfig;

import java.sql.SQLException;
import java.util.List;

/**
 * LLM配置服务层
 * 处理LLM配置的保存、更新、删除和切换
 */
public class LLMConfigService {
    
    private final LLMConfigDao configDao;
    private LLMConfig currentConfig;
    
    public LLMConfigService() {
        this.configDao = new LLMConfigDao();
        // 加载默认配置
        loadDefaultConfig();
    }
    
    /**
     * 加载默认配置
     */
    private void loadDefaultConfig() {
        try {
            currentConfig = configDao.findDefault();
        } catch (SQLException e) {
            System.err.println("加载默认配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有配置
     */
    public List<LLMConfig> getAllConfigs() {
        try {
            return configDao.findAll();
        } catch (SQLException e) {
            System.err.println("获取配置列表失败: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 添加新配置
     */
    public String addConfig(LLMConfig config) {
        try {
            // 验证必填字段
            if (config.getName() == null || config.getName().trim().isEmpty()) {
                return "配置名称不能为空";
            }
            if (config.getProvider() == null) {
                return "请选择提供商";
            }
            if (config.getApiEndpoint() == null || config.getApiEndpoint().trim().isEmpty()) {
                return "API端点不能为空";
            }
            
            // 加密API Key
            if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
                config.setApiKey(encrypt(config.getApiKey()));
            }
            
            // 保存到数据库
            if (configDao.insert(config)) {
                // 如果设为默认，更新当前配置
                if (config.isDefault()) {
                    currentConfig = config;
                }
                return "配置添加成功";
            } else {
                return "配置添加失败";
            }
        } catch (SQLException e) {
            return "添加失败: " + e.getMessage();
        }
    }
    
    /**
     * 更新配置
     */
    public String updateConfig(LLMConfig config) {
        try {
            if (config.getId() == null) {
                return "配置ID不能为空";
            }
            
            // 如果API Key不为空且不是加密状态（长度较短），则加密
            String apiKey = config.getApiKey();
            if (apiKey != null && !apiKey.isEmpty() && apiKey.length() < 100) {
                config.setApiKey(encrypt(apiKey));
            }
            
            if (configDao.update(config)) {
                // 如果更新的是当前默认配置，刷新缓存
                if (currentConfig != null && currentConfig.getId().equals(config.getId())) {
                    currentConfig = config;
                }
                return "配置更新成功";
            } else {
                return "配置更新失败";
            }
        } catch (SQLException e) {
            return "更新失败: " + e.getMessage();
        }
    }
    
    /**
     * 删除配置
     */
    public String deleteConfig(int id) {
        try {
            // 不能删除最后一个配置
            List<LLMConfig> configs = configDao.findAll();
            if (configs.size() <= 1) {
                return "不能删除最后一个配置";
            }
            
            if (configDao.delete(id)) {
                // 如果删除的是当前配置，重新加载默认配置
                if (currentConfig != null && currentConfig.getId() == id) {
                    loadDefaultConfig();
                }
                return "配置删除成功";
            } else {
                return "配置删除失败";
            }
        } catch (SQLException e) {
            return "删除失败: " + e.getMessage();
        }
    }
    
    /**
     * 设置默认配置
     */
    public String setDefaultConfig(int id) {
        try {
            if (configDao.setDefault(id)) {
                // 刷新当前配置
                currentConfig = configDao.findById(id);
                return "已设为默认配置";
            } else {
                return "设置失败";
            }
        } catch (SQLException e) {
            return "设置失败: " + e.getMessage();
        }
    }
    
    /**
     * 获取当前使用的配置
     */
    public LLMConfig getCurrentConfig() {
        return currentConfig;
    }
    
    /**
     * 切换当前配置
     */
    public void switchConfig(int id) {
        try {
            LLMConfig config = configDao.findById(id);
            if (config != null && config.isEnabled()) {
                currentConfig = config;
            }
        } catch (SQLException e) {
            System.err.println("切换配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取配置详情
     */
    public LLMConfig getConfigById(int id) {
        try {
            return configDao.findById(id);
        } catch (SQLException e) {
            System.err.println("获取配置失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 简单加密API Key（Base64）
     * 注意：生产环境应使用更安全的加密方式
     */
    private String encrypt(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        try {
            return java.util.Base64.getEncoder().encodeToString(value.getBytes());
        } catch (Exception e) {
            return value;
        }
    }
    
    /**
     * 获取解密后的API Key
     */
    public String getDecryptedApiKey(LLMConfig config) {
        if (config == null || config.getApiKey() == null) {
            return "";
        }
        try {
            return new String(java.util.Base64.getDecoder().decode(config.getApiKey()));
        } catch (Exception e) {
            return config.getApiKey(); // 如果解密失败，返回原值
        }
    }
    
    /**
     * 测试配置是否可用
     */
    public boolean testConfig(LLMConfig config) {
        // TODO: 实现API连通性测试
        // 发送一个简单的请求测试API是否可用
        return true;
    }
}
