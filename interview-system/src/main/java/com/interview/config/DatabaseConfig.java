package com.interview.config;

import com.interview.model.Role;

/**
 * 数据库配置类
 * 支持 PostgreSQL 数据库，按角色配置不同的数据库用户
 * 支持 SSL/HTTPS 连接
 */
public class DatabaseConfig {
    
    // 数据库类型枚举
    public enum DatabaseType {
        POSTGRESQL    // PostgreSQL 数据库
    }
    
    // 默认使用 PostgreSQL
    private static DatabaseType currentType = DatabaseType.POSTGRESQL;
    
    // PostgreSQL 基础配置
    private static String pgHost = "localhost";
    private static int pgPort = 5432;
    private static String pgDatabase = "interview_system";
    public static final String POSTGRESQL_DRIVER = "org.postgresql.Driver";
    
    // SSL/HTTPS 配置
    private static boolean sslEnabled = true;
    private static SSLConfig.SSLMode sslMode = SSLConfig.SSLMode.REQUIRE;
    private static String sslCertPath = "";
    private static String sslKeyPath = "";
    private static String sslRootCertPath = "";
    
    // 角色对应的数据库用户配置
    // 考生 (Candidate) - 只能通过 HTTPS 访问，权限受限
    private static String candidateDbUser = "candidate";
    private static String candidateDbPassword = "candidate_secure_pass";
    
    // 出题人 (Test Setter) - 只能通过 HTTPS 访问，管理题目
    private static String setterDbUser = "test_setter";
    private static String setterDbPassword = "setter_secure_pass";
    
    // 考官 (Judge) - 只能通过 HTTPS 访问，查看记录和评分
    private static String judgeDbUser = "judge";
    private static String judgeDbPassword = "judge_secure_pass";
    
    // 管理员 (Admin) - 保留完整权限
    private static String adminDbUser = "postgres";
    private static String adminDbPassword = "postgres";
    
    /**
     * 数据库用户角色枚举
     */
    public enum DbUserRole {
        CANDIDATE("candidate", "考生"),
        TEST_SETTER("test_setter", "出题人"),
        JUDGE("judge", "考官"),
        ADMIN("admin", "管理员");
        
        private final String dbUser;
        private final String displayName;
        
        DbUserRole(String dbUser, String displayName) {
            this.dbUser = dbUser;
            this.displayName = displayName;
        }
        
        public String getDbUser() { return dbUser; }
        public String getDisplayName() { return displayName; }
        
        /**
         * 从应用角色映射到数据库用户角色
         */
        public static DbUserRole fromAppRole(Role role) {
            if (role == null) return CANDIDATE;
            return switch (role) {
                case CANDIDATE -> CANDIDATE;
                case QUESTION_CREATOR -> TEST_SETTER;
                case EXAMINER -> JUDGE;
                case ADMIN -> ADMIN;
            };
        }
    }
    
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
     * 获取当前数据库连接URL（基础URL，不含SSL参数）
     */
    public static String getCurrentUrl() {
        return String.format("jdbc:postgresql://%s:%d/%s", 
                pgHost, pgPort, pgDatabase);
    }
    
    /**
     * 获取带SSL参数的数据库连接URL
     */
    public static String getSecureUrl(DbUserRole role) {
        StringBuilder url = new StringBuilder();
        url.append(String.format("jdbc:postgresql://%s:%d/%s?user=%s&password=%s",
                pgHost, pgPort, pgDatabase, 
                getDbUsername(role), getDbPassword(role)));
        
        // 添加 SSL 参数
        if (sslEnabled) {
            url.append("&ssl=true");
            url.append("&sslmode=").append(sslMode.getMode());
            
            if (!sslCertPath.isEmpty()) {
                url.append("&sslcert=").append(sslCertPath);
            }
            if (!sslKeyPath.isEmpty()) {
                url.append("&sslkey=").append(sslKeyPath);
            }
            if (!sslRootCertPath.isEmpty()) {
                url.append("&sslrootcert=").append(sslRootCertPath);
            }
        }
        
        return url.toString();
    }
    
    /**
     * 获取当前数据库驱动类名
     */
    public static String getCurrentDriver() {
        return POSTGRESQL_DRIVER;
    }
    
    /**
     * 获取指定角色的数据库用户名
     */
    public static String getDbUsername(DbUserRole role) {
        if (role == null) role = DbUserRole.CANDIDATE;
        return switch (role) {
            case CANDIDATE -> candidateDbUser;
            case TEST_SETTER -> setterDbUser;
            case JUDGE -> judgeDbUser;
            case ADMIN -> adminDbUser;
        };
    }
    
    /**
     * 获取指定角色的数据库密码
     */
    public static String getDbPassword(DbUserRole role) {
        if (role == null) role = DbUserRole.CANDIDATE;
        return switch (role) {
            case CANDIDATE -> candidateDbUser;
            case TEST_SETTER -> setterDbPassword;
            case JUDGE -> judgeDbPassword;
            case ADMIN -> adminDbPassword;
        };
    }
    
    /**
     * 是否需要认证
     */
    public static boolean requiresAuth() {
        return true;
    }
    
    /**
     * 是否启用 SSL
     */
    public static boolean isSslEnabled() {
        return sslEnabled;
    }
    
    public static void setSslEnabled(boolean enabled) {
        sslEnabled = enabled;
    }
    
    /**
     * 获取 SSL 模式
     */
    public static SSLConfig.SSLMode getSslMode() {
        return sslMode;
    }
    
    public static void setSslMode(SSLConfig.SSLMode mode) {
        sslMode = mode;
    }
    
    // PostgreSQL 基础配置设置方法
    public static void setPgHost(String host) {
        pgHost = host;
    }
    
    public static void setPgPort(int port) {
        pgPort = port;
    }
    
    public static void setPgDatabase(String database) {
        pgDatabase = database;
    }
    
    /**
     * 设置管理员数据库凭据（向后兼容）
     */
    public static void setPgCredentials(String username, String password) {
        adminDbUser = username;
        adminDbPassword = password;
    }
    
    /**
     * 设置角色数据库凭据
     */
    public static void setCandidateCredentials(String username, String password) {
        candidateDbUser = username;
        candidateDbPassword = password;
    }
    
    public static void setSetterCredentials(String username, String password) {
        setterDbUser = username;
        setterDbPassword = password;
    }
    
    public static void setJudgeCredentials(String username, String password) {
        judgeDbUser = username;
        judgeDbPassword = password;
    }
    
    /**
     * 设置 SSL 证书路径
     */
    public static void setSslCertPath(String path) {
        sslCertPath = path;
    }
    
    public static void setSslKeyPath(String path) {
        sslKeyPath = path;
    }
    
    public static void setSslRootCertPath(String path) {
        sslRootCertPath = path;
    }
    
    public static String getSslCertPath() {
        return sslCertPath;
    }
    
    public static String getSslKeyPath() {
        return sslKeyPath;
    }
    
    public static String getSslRootCertPath() {
        return sslRootCertPath;
    }
    
    public static String getPgHost() {
        return pgHost;
    }
    
    public static int getPgPort() {
        return pgPort;
    }
    
    public static String getPgDatabase() {
        return pgDatabase;
    }
}
