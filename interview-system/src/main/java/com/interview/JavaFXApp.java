package com.interview;

import com.interview.config.AppConfig;
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
        // 应用已经在 Main 中加载了配置
        // 这里只解析额外的命令行参数覆盖配置
        parseArguments(getParameters().getRaw().toArray(new String[0]));
        
        // 初始化数据库
        if (!initializeDatabase()) {
            System.err.println("数据库初始化失败，程序退出");
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
        
        // 加载样式表
        var cssUrl = JavaFXApp.class.getResource("/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
        
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
        
        // 加载样式表
        var cssUrl = JavaFXApp.class.getResource("/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
        
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
     * 解析命令行参数（覆盖配置文件）
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
                        String user = args[++i];
                        DatabaseConfig.setPgCredentials(user, DatabaseConfig.getPassword());
                    }
                    break;
                case "--db-password":
                    if (i + 1 < args.length) {
                        String password = args[++i];
                        DatabaseConfig.setPgCredentials(DatabaseConfig.getUsername(), password);
                    }
                    break;
                case "--help":
                    printHelp();
                    System.exit(0);
                    break;
            }
        }
    }
    
    /**
     * 初始化数据库
     */
    private boolean initializeDatabase() {
        try {
            // 显示当前配置
            AppConfig.printConfig();
            
            // 测试数据库连接
            if (!DatabaseConnection.testConnection()) {
                System.err.println("无法连接到 PostgreSQL 数据库，请检查配置");
                System.err.println("连接信息: " + DatabaseConfig.getCurrentUrl());
                System.err.println("\n请检查:");
                System.err.println("1. PostgreSQL 服务是否已启动");
                System.err.println("2. 用户名和密码是否正确");
                System.err.println("3. 数据库 interview_system 是否存在");
                System.err.println("\n您可以修改 config.properties 文件来配置正确的连接信息");
                return false;
            }
            
            System.out.println("数据库连接成功!");
            
            // 初始化数据库表结构
            DatabaseInitializer initializer = new DatabaseInitializer();
            initializer.initialize();
            
            return true;
            
        } catch (SQLException e) {
            System.err.println("数据库初始化失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 打印帮助信息
     */
    private void printHelp() {
        System.out.println("面试题目抽取系统 - JavaFX 版本");
        System.out.println("用法: java -jar interview-system.jar [选项]");
        System.out.println();
        System.out.println("配置文件: config.properties (放在程序同目录)");
        System.out.println();
        System.out.println("命令行选项(会覆盖配置文件):");
        System.out.println("  --db-host <host>     PostgreSQL 主机地址 (默认: localhost)");
        System.out.println("  --db-port <port>     PostgreSQL 端口 (默认: 5432)");
        System.out.println("  --db-name <name>     PostgreSQL 数据库名 (默认: interview_system)");
        System.out.println("  --db-user <user>     PostgreSQL 用户名 (默认: postgres)");
        System.out.println("  --db-password <pass> PostgreSQL 密码 (默认: postgres)");
        System.out.println("  --help               显示此帮助信息");
        System.out.println();
        System.out.println("示例:");
        System.out.println("  # 使用配置文件运行");
        System.out.println("  java -jar interview-system.jar");
        System.out.println();
        System.out.println("  # 使用命令行参数覆盖配置");
        System.out.println("  java -jar interview-system.jar --db-password 1234");
    }
}
