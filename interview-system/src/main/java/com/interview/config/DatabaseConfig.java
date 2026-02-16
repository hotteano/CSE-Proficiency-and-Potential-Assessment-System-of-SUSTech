package com.interview.config;

/**
 * 数据库配置类
 * 支持 PostgreSQL 数据库
 */
public class DatabaseConfig {
    
    // 数据库类型枚举
    public enum DatabaseType {
        POSTGRESQL    // PostgreSQL 数据库
    }
    
    // 默认使用 PostgreSQL
    private static DatabaseType currentType = DatabaseType.POSTGRESQL;
    
    // PostgreSQL 配置
    private static String pgHost = "localhost";
    private static int pgPort = 5432;
    private static String pgDatabase = "interview_system";
    private static String pgUsername = "postgres";
    private static String pgPassword = "postgres";
    public static final String POSTGRESQL_DRIVER = "org.postgresql.Driver";
    
    /**
     * 获取当前数据库类型
     */
    public static DatabaseType getCurrentType() {
        return currentType;
    }
    
    /**
     * 设置数据库类型
     */
    public static void setDatabaseType(DatabaseType type) {
        currentType = type;
    }
    
    /**
     * 获取当前数据库连接URL
     */
    public static String getCurrentUrl() {
        return String.format("jdbc:postgresql://%s:%d/%s", 
                pgHost, pgPort, pgDatabase);
    }
    
    /**
     * 获取当前数据库驱动类名
     */
    public static String getCurrentDriver() {
        return POSTGRESQL_DRIVER;
    }
    
    /**
     * 获取数据库用户名
     */
    public static String getUsername() {
        return pgUsername;
    }
    
    /**
     * 获取数据库密码
     */
    public static String getPassword() {
        return pgPassword;
    }
    
    /**
     * 是否需要认证
     */
    public static boolean requiresAuth() {
        return true;
    }
    
    // PostgreSQL 配置设置方法
    public static void setPgHost(String host) {
        pgHost = host;
    }
    
    public static void setPgPort(int port) {
        pgPort = port;
    }
    
    public static void setPgDatabase(String database) {
        pgDatabase = database;
    }
    
    public static void setPgCredentials(String username, String password) {
        pgUsername = username;
        pgPassword = password;
    }
}
