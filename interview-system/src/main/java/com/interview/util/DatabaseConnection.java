package com.interview.util;

import com.interview.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 数据库连接工具类
 * 负责管理数据库连接，支持连接池和单连接模式
 */
public class DatabaseConnection {
    
    private static Connection connection;
    
    /**
     * 获取数据库连接
     * 如果连接已存在且未关闭，则返回现有连接
     * 否则创建新连接
     */
    public static Connection getConnection() throws SQLException {
        try {
            // 加载驱动
            Class.forName(DatabaseConfig.getCurrentDriver());
            
            // 检查现有连接
            if (connection != null && !connection.isClosed()) {
                return connection;
            }
            
            // 创建新连接
            if (DatabaseConfig.requiresAuth()) {
                connection = DriverManager.getConnection(
                    DatabaseConfig.getCurrentUrl(),
                    DatabaseConfig.getUsername(),
                    DatabaseConfig.getPassword()
                );
            } else {
                connection = DriverManager.getConnection(DatabaseConfig.getCurrentUrl());
            }
            
            return connection;
            
        } catch (ClassNotFoundException e) {
            throw new SQLException("数据库驱动加载失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 测试数据库连接
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("数据库连接测试失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 关闭数据库连接
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("关闭数据库连接失败: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }
    
    /**
     * 开始事务
     */
    public static void beginTransaction() throws SQLException {
        Connection conn = getConnection();
        conn.setAutoCommit(false);
    }
    
    /**
     * 提交事务
     */
    public static void commitTransaction() throws SQLException {
        Connection conn = getConnection();
        conn.commit();
        conn.setAutoCommit(true);
    }
    
    /**
     * 回滚事务
     */
    public static void rollbackTransaction() throws SQLException {
        Connection conn = getConnection();
        conn.rollback();
        conn.setAutoCommit(true);
    }
}
