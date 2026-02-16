package com.interview;

import com.interview.config.AppConfig;
import javafx.application.Application;

/**
 * 系统入口类
 * 启动 JavaFX 应用
 */
public class Main {
    
    public static void main(String[] args) {
        // 加载配置文件
        AppConfig.applyDatabaseConfig();
        
        // 启动 JavaFX 应用
        Application.launch(JavaFXApp.class, args);
    }
}
