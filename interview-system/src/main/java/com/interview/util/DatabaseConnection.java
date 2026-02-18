package com.interview.util;

import com.interview.config.DatabaseConfig;
import com.interview.config.DatabaseConfig.DbUserRole;
import com.interview.model.Role;
import com.interview.model.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 数据库连接工具类
 * 负责管理数据库连接，支持按角色分配不同的数据库用户
 * 支持 SSL/HTTPS 连接
 */
public class DatabaseConnection {
    
    // 线程本地存储，用于存储当前线程的数据库连接和角色
    private static final ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();
    private static final ThreadLocal<DbUserRole> roleHolder = new ThreadLocal<>();
    
    /**
     * 设置当前线程的数据库用户角色
     * @param role 数据库用户角色
     */
    public static void setCurrentDbRole(DbUserRole role) {
        roleHolder.set(role);
        // 清除现有连接，下次获取时会使用新角色创建连接
        closeCurrentConnection();
    }
    
    /**
     * 根据应用用户角色设置数据库用户角色
     * @param appRole 应用中的角色
     */
    public static void setRoleByAppRole(Role appRole) {
        setCurrentDbRole(DbUserRole.fromAppRole(appRole));
    }
    
    /**
     * 获取当前线程的数据库用户角色
     */
    public static DbUserRole getCurrentDbRole() {
        DbUserRole role = roleHolder.get();
        return role != null ? role : DbUserRole.CANDIDATE; // 默认使用 candidate
    }
    
    /**
     * 获取数据库连接（使用当前线程设置的角色）
     * 所有用户都通过 HTTPS/SSL 协议访问数据库
     */
    public static Connection getConnection() throws SQLException {
        return getConnection(getCurrentDbRole());
    }
    
    /**
     * 获取指定角色的数据库连接
     * 所有连接都使用 SSL/HTTPS 加密
     * 
     * @param dbRole 数据库用户角色
     */
    public static Connection getConnection(DbUserRole dbRole) throws SQLException {
        try {
            // 加载驱动
            Class.forName(DatabaseConfig.getCurrentDriver());
            
            // 检查当前线程是否有连接
            Connection existingConn = connectionHolder.get();
            if (existingConn != null && !existingConn.isClosed()) {
                return existingConn;
            }
            
            // 创建新的 SSL 连接
            Connection conn;
            if (DatabaseConfig.isSslEnabled()) {
                // 使用带 SSL 的 URL
                String secureUrl = DatabaseConfig.getSecureUrl(dbRole);
                conn = DriverManager.getConnection(secureUrl);
            } else {
                // 回退到普通连接（仅用于开发/测试）
                Properties props = new Properties();
                props.setProperty("user", DatabaseConfig.getDbUsername(dbRole));
                props.setProperty("password", DatabaseConfig.getDbPassword(dbRole));
                conn = DriverManager.getConnection(DatabaseConfig.getCurrentUrl(), props);
            }
            
            connectionHolder.set(conn);
            return conn;
            
        } catch (ClassNotFoundException e) {
            throw new SQLException("数据库驱动加载失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 为指定应用用户获取连接
     * @param user 应用用户，若为 null 则使用 candidate 角色
     */
    public static Connection getConnectionForUser(User user) throws SQLException {
        DbUserRole dbRole = (user != null) 
            ? DbUserRole.fromAppRole(user.getRole()) 
            : DbUserRole.CANDIDATE;
        return getConnection(dbRole);
    }
    
    /**
     * 测试指定角色的数据库连接
     * @param dbRole 要测试的数据库角色
     */
    public static boolean testConnection(DbUserRole dbRole) {
        try (Connection conn = getConnection(dbRole)) {
            boolean valid = conn != null && !conn.isClosed();
            if (valid) {
                System.out.println("数据库连接测试成功 (角色: " + dbRole.getDisplayName() + ")");
            }
            return valid;
        } catch (SQLException e) {
            System.err.println("数据库连接测试失败 (角色: " + dbRole.getDisplayName() + "): " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试所有角色的数据库连接
     */
    public static boolean testAllConnections() {
        System.out.println("\n========== 测试数据库连接 ==========");
        boolean allSuccess = true;
        
        for (DbUserRole role : DbUserRole.values()) {
            boolean success = testConnection(role);
            if (!success) {
                allSuccess = false;
            }
        }
        
        System.out.println("==================================\n");
        return allSuccess;
    }
    
    /**
     * 关闭当前线程的数据库连接
     */
    public static void closeCurrentConnection() {
        Connection conn = connectionHolder.get();
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("关闭数据库连接失败: " + e.getMessage());
            } finally {
                connectionHolder.remove();
            }
        }
    }
    
    /**
     * 关闭所有连接（用于应用关闭时）
     */
    public static void closeAllConnections() {
        closeCurrentConnection();
        roleHolder.remove();
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
    
    /**
     * 获取考生数据库连接
     * 所有考生使用同一个数据库用户 'candidate' 通过 HTTPS 访问
     */
    public static Connection getCandidateConnection() throws SQLException {
        return getConnection(DbUserRole.CANDIDATE);
    }
    
    /**
     * 获取出题人数据库连接
     * 所有出题人使用同一个数据库用户 'test_setter' 通过 HTTPS 访问
     */
    public static Connection getSetterConnection() throws SQLException {
        return getConnection(DbUserRole.TEST_SETTER);
    }
    
    /**
     * 获取考官数据库连接
     * 所有考官使用同一个数据库用户 'judge' 通过 HTTPS 访问
     */
    public static Connection getJudgeConnection() throws SQLException {
        return getConnection(DbUserRole.JUDGE);
    }
    
    /**
     * 获取管理员数据库连接
     */
    public static Connection getAdminConnection() throws SQLException {
        return getConnection(DbUserRole.ADMIN);
    }
}
