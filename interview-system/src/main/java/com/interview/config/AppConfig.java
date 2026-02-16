package com.interview.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 应用程序配置类
 * 支持从配置文件读取数据库连接信息
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
        return properties.getProperty(key, defaultValue);
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
    
    /**
     * 应用数据库配置到 DatabaseConfig
     */
    public static void applyDatabaseConfig() {
        loadConfig();
        
        String host = getString("db.host", "localhost");
        int port = getInt("db.port", 5432);
        String database = getString("db.name", "interview_system");
        String username = getString("db.user", "postgres");
        String password = getString("db.password", "postgres");
        
        DatabaseConfig.setPgHost(host);
        DatabaseConfig.setPgPort(port);
        DatabaseConfig.setPgDatabase(database);
        DatabaseConfig.setPgCredentials(username, password);
        
        System.out.println("数据库配置已应用:");
        System.out.println("  主机: " + host);
        System.out.println("  端口: " + port);
        System.out.println("  数据库: " + database);
        System.out.println("  用户: " + username);
    }
    
    /**
     * 显示配置信息
     */
    public static void printConfig() {
        loadConfig();
        System.out.println("\n========== 当前配置 ==========");
        System.out.println("数据库主机: " + getString("db.host", "localhost"));
        System.out.println("数据库端口: " + getInt("db.port", 5432));
        System.out.println("数据库名: " + getString("db.name", "interview_system"));
        System.out.println("数据库用户: " + getString("db.user", "postgres"));
        System.out.println("==============================\n");
    }
}
