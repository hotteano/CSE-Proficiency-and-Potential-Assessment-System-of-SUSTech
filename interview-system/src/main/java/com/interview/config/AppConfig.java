package com.interview.config;

import com.interview.config.DatabaseConfig.DbUserRole;
import com.interview.config.SSLConfig.SSLMode;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 应用程序配置类
 * 支持从配置文件读取数据库连接信息，包括 SSL/HTTPS 配置和角色数据库用户配置
 */
public class AppConfig {
    
    private static final String CONFIG_FILE = "config.properties";
    private static final Properties properties = new Properties();
    private static boolean loaded = false;
    
    /**
     * 加载配置文件
     */
    public static void loadConfig() {
        if (loaded) {
            return;
        }
        
        // 首先尝试从当前目录加载
        try (InputStream is = new FileInputStream(CONFIG_FILE)) {
            properties.load(is);
            loaded = true;
            System.out.println("配置文件加载成功: " + CONFIG_FILE);
            return;
        } catch (IOException e) {
            System.out.println("未找到外部配置文件，尝试从资源目录加载...");
        }
        
        // 尝试从资源目录加载
        try (InputStream is = AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (is != null) {
                properties.load(is);
                loaded = true;
                System.out.println("配置文件加载成功（内置）");
            }
        } catch (IOException e) {
            System.out.println("未找到配置文件，使用默认配置");
        }
    }
    
    /**
     * 从配置文件获取值，如果不存在返回默认值
     */
    public static String getString(String key, String defaultValue) {
        loadConfig();
        String value = properties.getProperty(key, defaultValue);
        // 处理变量引用，如 ${db.admin.user}
        if (value != null && value.startsWith("${") && value.endsWith("}")) {
            String refKey = value.substring(2, value.length() - 1);
            value = properties.getProperty(refKey, defaultValue);
        }
        return value;
    }
    
    public static int getInt(String key, int defaultValue) {
        loadConfig();
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    public static boolean getBoolean(String key, boolean defaultValue) {
        loadConfig();
        String value = properties.getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value.trim());
        }
        return defaultValue;
    }
    
    /**
     * 应用数据库配置到 DatabaseConfig
     */
    public static void applyDatabaseConfig() {
        loadConfig();
        
        // 基础配置
        String host = getString("db.host", "localhost");
        int port = getInt("db.port", 5432);
        String database = getString("db.name", "interview_system");
        
        DatabaseConfig.setPgHost(host);
        DatabaseConfig.setPgPort(port);
        DatabaseConfig.setPgDatabase(database);
        
        // SSL 配置
        boolean sslEnabled = getBoolean("db.ssl.enabled", false);
        DatabaseConfig.setSslEnabled(sslEnabled);
        
        String sslModeStr = getString("db.ssl.mode", "require");
        try {
            SSLMode sslMode = SSLMode.valueOf(sslModeStr.toUpperCase().replace("-", "_"));
            DatabaseConfig.setSslMode(sslMode);
        } catch (IllegalArgumentException e) {
            System.err.println("无效的 SSL 模式: " + sslModeStr + "，使用默认 require");
            DatabaseConfig.setSslMode(SSLMode.REQUIRE);
        }
        
        // SSL 证书路径
        String sslCertPath = getString("db.ssl.cert.path", "");
        String sslKeyPath = getString("db.ssl.key.path", "");
        String sslRootCertPath = getString("db.ssl.rootcert.path", "");
        
        if (!sslCertPath.isEmpty()) DatabaseConfig.setSslCertPath(sslCertPath);
        if (!sslKeyPath.isEmpty()) DatabaseConfig.setSslKeyPath(sslKeyPath);
        if (!sslRootCertPath.isEmpty()) DatabaseConfig.setSslRootCertPath(sslRootCertPath);
        
        // 角色数据库用户配置
        // 考生
        String candidateUser = getString("db.candidate.user", "candidate");
        String candidatePass = getString("db.candidate.password", "candidate_secure_pass");
        DatabaseConfig.setCandidateCredentials(candidateUser, candidatePass);
        
        // 出题人
        String setterUser = getString("db.setter.user", "test_setter");
        String setterPass = getString("db.setter.password", "setter_secure_pass");
        DatabaseConfig.setSetterCredentials(setterUser, setterPass);
        
        // 考官
        String judgeUser = getString("db.judge.user", "judge");
        String judgePass = getString("db.judge.password", "judge_secure_pass");
        DatabaseConfig.setJudgeCredentials(judgeUser, judgePass);
        
        // 管理员（向后兼容）
        String adminUser = getString("db.admin.user", "postgres");
        String adminPass = getString("db.admin.password", "postgres");
        DatabaseConfig.setPgCredentials(adminUser, adminPass);
        
        System.out.println("数据库配置已应用:");
        System.out.println("  主机: " + host);
        System.out.println("  端口: " + port);
        System.out.println("  数据库: " + database);
        System.out.println("  SSL 启用: " + sslEnabled);
        System.out.println("  SSL 模式: " + DatabaseConfig.getSslMode().getMode());
    }
    
    /**
     * 获取指定角色的数据库配置信息
     */
    public static void printRoleConfig(DbUserRole role) {
        System.out.println("  " + role.getDisplayName() + " (" + role.getDbUser() + "):");
        System.out.println("    数据库用户: " + DatabaseConfig.getDbUsername(role));
        System.out.println("    连接 URL: " + maskPassword(DatabaseConfig.getSecureUrl(role)));
    }
    
    /**
     * 隐藏 URL 中的密码
     */
    private static String maskPassword(String url) {
        return url.replaceAll("password=[^&]*", "password=***");
    }
    
    /**
     * 显示配置信息
     */
    public static void printConfig() {
        loadConfig();
        System.out.println("\n========== 当前数据库配置 ==========");
        System.out.println("数据库主机: " + getString("db.host", "localhost"));
        System.out.println("数据库端口: " + getInt("db.port", 5432));
        System.out.println("数据库名: " + getString("db.name", "interview_system"));
        System.out.println("SSL 启用: " + DatabaseConfig.isSslEnabled());
        System.out.println("SSL 模式: " + DatabaseConfig.getSslMode().getMode());
        System.out.println("\n角色数据库用户配置:");
        for (DbUserRole role : DbUserRole.values()) {
            printRoleConfig(role);
        }
        System.out.println("==================================\n");
    }
}
