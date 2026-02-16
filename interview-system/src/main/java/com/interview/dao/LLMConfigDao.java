package com.interview.dao;

import com.interview.model.LLMConfig;
import com.interview.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 大模型配置数据访问对象
 */
public class LLMConfigDao {
    
    /**
     * 创建LLM配置表
     */
    public void createTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS llm_configs (
                id SERIAL PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                provider VARCHAR(50) NOT NULL,
                model_name VARCHAR(100) NOT NULL,
                api_key VARCHAR(500) NOT NULL,
                api_endpoint VARCHAR(500) NOT NULL,
                is_default BOOLEAN DEFAULT FALSE,
                enabled BOOLEAN DEFAULT TRUE,
                timeout INTEGER DEFAULT 60,
                params TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * 插入新配置
     */
    public boolean insert(LLMConfig config) throws SQLException {
        String sql = """
            INSERT INTO llm_configs 
            (name, provider, model_name, api_key, api_endpoint, is_default, enabled, timeout, params, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, config.getName());
            pstmt.setString(2, config.getProvider().name());
            pstmt.setString(3, config.getModelName());
            pstmt.setString(4, config.getApiKey());
            pstmt.setString(5, config.getApiEndpoint());
            pstmt.setBoolean(6, config.isDefault());
            pstmt.setBoolean(7, config.isEnabled());
            pstmt.setInt(8, config.getTimeout());
            pstmt.setString(9, config.getParams());
            pstmt.setTimestamp(10, config.getCreatedAt() != null ? 
                    Timestamp.valueOf(config.getCreatedAt()) : null);
            pstmt.setTimestamp(11, config.getUpdatedAt() != null ? 
                    Timestamp.valueOf(config.getUpdatedAt()) : null);
            
            // 如果设为默认，取消其他默认
            if (config.isDefault()) {
                clearDefaultConfig(conn);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    config.setId(rs.getInt(1));
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 更新配置
     */
    public boolean update(LLMConfig config) throws SQLException {
        String sql = """
            UPDATE llm_configs 
            SET name = ?, provider = ?, model_name = ?, api_key = ?, api_endpoint = ?,
                is_default = ?, enabled = ?, timeout = ?, params = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, config.getName());
            pstmt.setString(2, config.getProvider().name());
            pstmt.setString(3, config.getModelName());
            pstmt.setString(4, config.getApiKey());
            pstmt.setString(5, config.getApiEndpoint());
            pstmt.setBoolean(6, config.isDefault());
            pstmt.setBoolean(7, config.isEnabled());
            pstmt.setInt(8, config.getTimeout());
            pstmt.setString(9, config.getParams());
            pstmt.setInt(10, config.getId());
            
            // 如果设为默认，取消其他默认
            if (config.isDefault()) {
                clearDefaultConfig(conn, config.getId());
            }
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 清除默认配置
     */
    private void clearDefaultConfig(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("UPDATE llm_configs SET is_default = FALSE");
        }
    }
    
    private void clearDefaultConfig(Connection conn, int excludeId) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "UPDATE llm_configs SET is_default = FALSE WHERE id != ?")) {
            pstmt.setInt(1, excludeId);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * 获取所有配置
     */
    public List<LLMConfig> findAll() throws SQLException {
        List<LLMConfig> configs = new ArrayList<>();
        String sql = "SELECT * FROM llm_configs ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                configs.add(mapResultSetToConfig(rs));
            }
        }
        return configs;
    }
    
    /**
     * 根据ID查找配置
     */
    public LLMConfig findById(int id) throws SQLException {
        String sql = "SELECT * FROM llm_configs WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToConfig(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * 获取默认配置
     */
    public LLMConfig findDefault() throws SQLException {
        String sql = "SELECT * FROM llm_configs WHERE is_default = TRUE LIMIT 1";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return mapResultSetToConfig(rs);
            }
        }
        return null;
    }
    
    /**
     * 删除配置
     */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM llm_configs WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 设置默认配置
     */
    public boolean setDefault(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            clearDefaultConfig(conn);
            
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE llm_configs SET is_default = TRUE WHERE id = ?")) {
                pstmt.setInt(1, id);
                return pstmt.executeUpdate() > 0;
            }
        }
    }
    
    /**
     * 将ResultSet映射到LLMConfig
     */
    private LLMConfig mapResultSetToConfig(ResultSet rs) throws SQLException {
        LLMConfig config = new LLMConfig();
        config.setId(rs.getInt("id"));
        config.setName(rs.getString("name"));
        
        try {
            config.setProvider(LLMConfig.LLMProvider.valueOf(rs.getString("provider")));
        } catch (IllegalArgumentException e) {
            config.setProvider(LLMConfig.LLMProvider.DEEPSEEK);
        }
        
        config.setModelName(rs.getString("model_name"));
        config.setApiKey(rs.getString("api_key"));
        config.setApiEndpoint(rs.getString("api_endpoint"));
        config.setDefault(rs.getBoolean("is_default"));
        config.setEnabled(rs.getBoolean("enabled"));
        config.setTimeout(rs.getInt("timeout"));
        config.setParams(rs.getString("params"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            config.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            config.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return config;
    }
}
