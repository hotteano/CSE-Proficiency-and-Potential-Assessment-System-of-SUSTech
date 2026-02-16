package com.interview.config;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.net.URL;
import java.security.KeyStore;

/**
 * SSL/TLS 配置管理类
 * 支持数据库SSL连接和HTTPS通信
 */
public class SSLConfig {
    
    // SSL配置
    private static boolean sslEnabled = false;
    private static String keystorePath = "";
    private static String keystorePassword = "";
    private static String truststorePath = "";
    private static String truststorePassword = "";
    
    // PostgreSQL SSL模式
    public enum SSLMode {
        DISABLE("disable", "禁用SSL"),
        ALLOW("allow", "优先非SSL，如不可用则使用SSL"),
        PREFER("prefer", "优先SSL，如不可用则使用非SSL"),
        REQUIRE("require", "必须使用SSL，不验证证书"),
        VERIFY_CA("verify-ca", "使用SSL并验证CA证书"),
        VERIFY_FULL("verify-full", "使用SSL并验证主机名和证书");
        
        private final String mode;
        private final String description;
        
        SSLMode(String mode, String description) {
            this.mode = mode;
            this.description = description;
        }
        
        public String getMode() { return mode; }
        public String getDescription() { return description; }
    }
    
    private static SSLMode currentSSLMode = SSLMode.PREFER;
    
    /**
     * 获取PostgreSQL SSL连接URL参数
     */
    public static String getSSLUrlParams() {
        StringBuilder params = new StringBuilder();
        params.append("&sslmode=").append(currentSSLMode.getMode());
        
        if (currentSSLMode == SSLMode.VERIFY_CA || currentSSLMode == SSLMode.VERIFY_FULL) {
            if (!truststorePath.isEmpty()) {
                params.append("&sslrootcert=").append(truststorePath);
            }
        }
        
        return params.toString();
    }
    
    /**
     * 创建SSLContext（用于HTTPS客户端）
     */
    public static SSLContext createSSLContext() throws Exception {
        if (!sslEnabled || keystorePath.isEmpty()) {
            // 返回默认的SSLContext
            return SSLContext.getDefault();
        }
        
        // 加载密钥库
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            keyStore.load(fis, keystorePassword.toCharArray());
        }
        
        // 初始化KeyManager
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keystorePassword.toCharArray());
        
        // 加载信任库
        TrustManager[] trustManagers = null;
        if (!truststorePath.isEmpty()) {
            KeyStore trustStore = KeyStore.getInstance("JKS");
            try (FileInputStream fis = new FileInputStream(truststorePath)) {
                trustStore.load(fis, truststorePassword.toCharArray());
            }
            
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            trustManagers = trustManagerFactory.getTrustManagers();
        }
        
        // 创建SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagers, null);
        
        return sslContext;
    }
    
    /**
     * 创建HTTPS连接（用于调用API）
     */
    public static HttpsURLConnection createHTTPSConnection(String urlStr) throws Exception {
        SSLContext sslContext = createSSLContext();
        
        URL url = new java.net.URL(urlStr);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setSSLSocketFactory(sslContext.getSocketFactory());
        conn.setHostnameVerifier((hostname, session) -> true); // 生产环境应该验证主机名
        
        return conn;
    }
    
    // Getters and Setters
    public static boolean isSSLEnabled() { return sslEnabled; }
    public static void setSSLEnabled(boolean enabled) { sslEnabled = enabled; }
    
    public static SSLMode getCurrentSSLMode() { return currentSSLMode; }
    public static void setCurrentSSLMode(SSLMode mode) { currentSSLMode = mode; }
    
    public static String getKeystorePath() { return keystorePath; }
    public static void setKeystorePath(String path) { keystorePath = path; }
    
    public static String getKeystorePassword() { return keystorePassword; }
    public static void setKeystorePassword(String password) { keystorePassword = password; }
    
    public static String getTruststorePath() { return truststorePath; }
    public static void setTruststorePath(String path) { truststorePath = path; }
    
    public static String getTruststorePassword() { return truststorePassword; }
    public static void setTruststorePassword(String password) { truststorePassword = password; }
}
