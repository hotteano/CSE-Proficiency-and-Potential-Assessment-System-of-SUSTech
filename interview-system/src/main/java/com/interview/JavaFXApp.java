package com.interview;

import com.interview.config.DatabaseConfig;
import com.interview.service.AuthService;
import com.interview.util.DatabaseConnection;
import com.interview.util.DatabaseInitializer;
import com.interview.view.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.SQLException;

/**
 * JavaFX 应用入口
 */
public class JavaFXApp extends Application {
    
    private static Stage primaryStage;
    private static AuthService authService;
    
    @Override
    public void init() {
        // 解析命令行参数
        parseArguments(getParameters().getRaw().toArray(new String[0]));
        
        // 初始化数据库
        if (!initializeDatabase()) {
            System.err.println("数据库初始化失败");
            System.exit(1);
        }
        
        authService = new AuthService();
    }
    
    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        
        stage.setTitle("面试题目抽取系统");
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        
        // 显示登录界面
        showLoginView();
        
        stage.show();
    }
    
    /**
     * 显示登录界面
     */
    public static void showLoginView() {
        LoginView loginView = new LoginView(authService);
        Scene scene = new Scene(loginView, 400, 500);
        scene.getStylesheets().add(JavaFXApp.class.getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("面试题目抽取系统 - 登录");
        primaryStage.centerOnScreen();
    }
    
    /**
     * 显示主界面
     */
    public static void showMainView() {
        com.interview.view.MainView mainView = new com.interview.view.MainView(authService);
        Scene scene = new Scene(mainView, 1100, 750);
        scene.getStylesheets().add(JavaFXApp.class.getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("面试题目抽取系统 - " + authService.getCurrentUser().getRealName());
        primaryStage.centerOnScreen();
    }
    
    /**
     * 获取主舞台
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    /**
     * 获取认证服务
     */
    public static AuthService getAuthService() {
        return authService;
    }
    
    /**
     * 解析命令行参数
     */
    private void parseArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--db-host":
                    if (i + 1 < args.length) {
                        DatabaseConfig.setPgHost(args[++i]);
                    }
                    break;
                case "--db-port":
                    if (i + 1 < args.length) {
                        try {
                            DatabaseConfig.setPgPort(Integer.parseInt(args[++i]));
                        } catch (NumberFormatException e) {
                            System.err.println("无效的端口号");
                        }
                    }
                    break;
                case "--db-name":
                    if (i + 1 < args.length) {
                        DatabaseConfig.setPgDatabase(args[++i]);
                    }
                    break;
                case "--db-user":
                    if (i + 1 < args.length) {
                        DatabaseConfig.setPgCredentials(args[++i], DatabaseConfig.getPassword());
                    }
                    break;
                case "--db-password":
                    if (i + 1 < args.length) {
                        DatabaseConfig.setPgCredentials(DatabaseConfig.getUsername(), args[++i]);
                    }
                    break;
            }
        }
    }
    
    /**
     * 初始化数据库
     */
    private boolean initializeDatabase() {
        try {
            if (!DatabaseConnection.testConnection()) {
                System.err.println("无法连接到 PostgreSQL 数据库");
                return false;
            }
            
            DatabaseInitializer initializer = new DatabaseInitializer();
            initializer.initialize();
            return true;
            
        } catch (SQLException e) {
            System.err.println("数据库初始化失败: " + e.getMessage());
            return false;
        }
    }
}
