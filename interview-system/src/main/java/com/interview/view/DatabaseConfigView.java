package com.interview.view;

import com.interview.config.DatabaseConfig;
import com.interview.util.DatabaseConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * 数据库连接配置界面
 * 支持配置远程数据库连接，适用于出题人远程出题
 */
public class DatabaseConfigView extends BorderPane {
    
    private TextField hostField;
    private TextField portField;
    private TextField databaseField;
    private TextField usernameField;
    private PasswordField passwordField;
    private Label statusLabel;
    private Label currentConfigLabel;
    
    public DatabaseConfigView() {
        setPadding(new Insets(20));
        setStyle("-fx-background-color: #f5f5f5;");
        
        initComponents();
        loadCurrentConfig();
    }
    
    private void initComponents() {
        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-border-radius: 10px;");
        
        // 标题
        Label titleLabel = new Label("数据库连接配置");
        titleLabel.setFont(Font.font(null, FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#1565c0"));
        
        // 说明文字
        Label descLabel = new Label("配置远程数据库连接，支持出题人远程出题\n" +
            "默认连接本地数据库，如需连接远程服务器请修改以下配置");
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);
        descLabel.setStyle("-fx-text-fill: #666;");
        
        // 当前配置显示
        currentConfigLabel = new Label();
        currentConfigLabel.setStyle("-fx-text-fill: #2196f3; -fx-font-weight: bold;");
        
        // 配置表单
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setAlignment(Pos.CENTER);
        formGrid.setPadding(new Insets(20));
        
        // 主机地址
        formGrid.add(new Label("主机地址:*"), 0, 0);
        hostField = new TextField();
        hostField.setPromptText("例如: 192.168.1.100 或 db.example.com");
        hostField.setPrefWidth(300);
        formGrid.add(hostField, 1, 0);
        
        // 端口
        formGrid.add(new Label("端口:*"), 0, 1);
        portField = new TextField("5432");
        portField.setPrefWidth(300);
        formGrid.add(portField, 1, 1);
        
        // 数据库名
        formGrid.add(new Label("数据库名:*"), 0, 2);
        databaseField = new TextField("interview_system");
        databaseField.setPrefWidth(300);
        formGrid.add(databaseField, 1, 2);
        
        // 用户名
        formGrid.add(new Label("用户名:*"), 0, 3);
        usernameField = new TextField("postgres");
        usernameField.setPrefWidth(300);
        formGrid.add(usernameField, 1, 3);
        
        // 密码
        formGrid.add(new Label("密码:*"), 0, 4);
        passwordField = new PasswordField();
        passwordField.setPromptText("数据库密码");
        passwordField.setPrefWidth(300);
        formGrid.add(passwordField, 1, 4);
        
        // 按钮区域
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));
        
        Button testBtn = new Button("测试连接");
        testBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold;");
        testBtn.setPrefWidth(120);
        testBtn.setOnAction(e -> testConnection());
        
        Button saveBtn = new Button("保存配置");
        saveBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold;");
        saveBtn.setPrefWidth(120);
        saveBtn.setOnAction(e -> saveConfig());
        
        Button resetBtn = new Button("恢复默认");
        resetBtn.setStyle("-fx-background-color: #9e9e9e; -fx-text-fill: white;");
        resetBtn.setPrefWidth(120);
        resetBtn.setOnAction(e -> resetToDefault());
        
        buttonBox.getChildren().addAll(testBtn, saveBtn, resetBtn);
        
        // 状态标签
        statusLabel = new Label();
        statusLabel.setAlignment(Pos.CENTER);
        statusLabel.setWrapText(true);
        
        // 帮助信息
        TitledPane helpPane = createHelpPane();
        
        content.getChildren().addAll(
            titleLabel, 
            descLabel, 
            currentConfigLabel,
            formGrid, 
            buttonBox, 
            statusLabel,
            helpPane
        );
        
        setCenter(content);
    }
    
    private TitledPane createHelpPane() {
        VBox helpContent = new VBox(10);
        helpContent.setPadding(new Insets(10));
        
        Label helpText = new Label(
            "1. 主机地址：数据库服务器的IP地址或域名\n" +
            "   - 本地连接: localhost 或 127.0.0.1\n" +
            "   - 远程连接: 服务器IP（如 192.168.1.100）\n\n" +
            "2. 端口：PostgreSQL 默认端口为 5432\n\n" +
            "3. 数据库名：默认为 interview_system\n\n" +
            "4. 用户名/密码：PostgreSQL 的登录凭据\n\n" +
            "远程连接注意事项：\n" +
            "• 确保数据库服务器允许远程连接\n" +
            "• 检查防火墙设置，开放5432端口\n" +
            "• 建议使用 VPN 或 SSH 隧道保证安全"
        );
        helpText.setWrapText(true);
        helpText.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        
        helpContent.getChildren().add(helpText);
        
        TitledPane pane = new TitledPane("连接帮助", helpContent);
        pane.setExpanded(false);
        return pane;
    }
    
    private void loadCurrentConfig() {
        // 从 DatabaseConfig 加载当前配置
        String currentUrl = DatabaseConfig.getCurrentUrl();
        currentConfigLabel.setText("当前连接: " + currentUrl);
        
        // 解析当前配置并填充表单（简化处理）
        hostField.setText("localhost");
        portField.setText("5432");
        databaseField.setText("interview_system");
        usernameField.setText(DatabaseConfig.getUsername());
    }
    
    private void testConnection() {
        if (!validateInput()) {
            return;
        }
        
        statusLabel.setText("正在测试连接...");
        statusLabel.setTextFill(Color.web("#ff9800"));
        
        // 保存当前配置
        String originalHost = getHostFromConfig();
        int originalPort = getPortFromConfig();
        
        // 临时设置新配置
        applyConfig();
        
        // 关闭现有连接
        DatabaseConnection.closeConnection();
        
        // 测试新连接
        boolean success = DatabaseConnection.testConnection();
        
        if (success) {
            statusLabel.setText("✓ 连接测试成功！数据库连接正常");
            statusLabel.setTextFill(Color.web("#4caf50"));
        } else {
            statusLabel.setText("✗ 连接测试失败，请检查配置和网络\n" +
                "常见问题：\n" +
                "• 主机地址或端口错误\n" +
                "• 数据库服务未启动\n" +
                "• 防火墙阻止连接\n" +
                "• 用户名或密码错误");
            statusLabel.setTextFill(Color.web("#f44336"));
            
            // 恢复原始配置
            DatabaseConfig.setPgHost(originalHost);
            DatabaseConfig.setPgPort(originalPort);
        }
    }
    
    private void saveConfig() {
        if (!validateInput()) {
            return;
        }
        
        applyConfig();
        
        // 关闭现有连接以强制使用新配置
        DatabaseConnection.closeConnection();
        
        statusLabel.setText("✓ 配置已保存！新连接将在下次操作时生效");
        statusLabel.setTextFill(Color.web("#4caf50"));
        
        // 更新当前配置显示
        currentConfigLabel.setText("当前连接: " + DatabaseConfig.getCurrentUrl());
    }
    
    private void resetToDefault() {
        hostField.setText("localhost");
        portField.setText("5432");
        databaseField.setText("interview_system");
        usernameField.setText("postgres");
        passwordField.clear();
        
        statusLabel.setText("已恢复默认配置");
        statusLabel.setTextFill(Color.web("#2196f3"));
    }
    
    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();
        
        if (hostField.getText().trim().isEmpty()) {
            errors.append("• 主机地址不能为空\n");
        }
        
        if (portField.getText().trim().isEmpty()) {
            errors.append("• 端口不能为空\n");
        } else {
            try {
                int port = Integer.parseInt(portField.getText().trim());
                if (port < 1 || port > 65535) {
                    errors.append("• 端口号必须在 1-65535 之间\n");
                }
            } catch (NumberFormatException e) {
                errors.append("• 端口号必须是数字\n");
            }
        }
        
        if (databaseField.getText().trim().isEmpty()) {
            errors.append("• 数据库名不能为空\n");
        }
        
        if (usernameField.getText().trim().isEmpty()) {
            errors.append("• 用户名不能为空\n");
        }
        
        if (passwordField.getText().isEmpty()) {
            errors.append("• 密码不能为空\n");
        }
        
        if (errors.length() > 0) {
            statusLabel.setText("请修正以下错误：\n" + errors.toString());
            statusLabel.setTextFill(Color.web("#f44336"));
            return false;
        }
        
        return true;
    }
    
    private void applyConfig() {
        DatabaseConfig.setPgHost(hostField.getText().trim());
        DatabaseConfig.setPgPort(Integer.parseInt(portField.getText().trim()));
        DatabaseConfig.setPgDatabase(databaseField.getText().trim());
        DatabaseConfig.setPgCredentials(
            usernameField.getText().trim(),
            passwordField.getText()
        );
    }
    
    private String getHostFromConfig() {
        // 从 URL 解析主机
        String url = DatabaseConfig.getCurrentUrl();
        // jdbc:postgresql://host:port/db
        try {
            String hostPart = url.substring(url.indexOf("://") + 3);
            return hostPart.substring(0, hostPart.indexOf(":"));
        } catch (Exception e) {
            return "localhost";
        }
    }
    
    private int getPortFromConfig() {
        String url = DatabaseConfig.getCurrentUrl();
        try {
            String hostPart = url.substring(url.indexOf("://") + 3);
            String portAndDb = hostPart.substring(hostPart.indexOf(":") + 1);
            String portStr = portAndDb.substring(0, portAndDb.indexOf("/"));
            return Integer.parseInt(portStr);
        } catch (Exception e) {
            return 5432;
        }
    }
}
