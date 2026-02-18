package com.interview.view;

import com.interview.JavaFXApp;
import com.interview.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * 登录界面（JavaFX）
 * 应用新 CSS 设计
 */
public class LoginView extends VBox {
    
    private final AuthService authService;
    
    private TextField usernameField;
    private PasswordField passwordField;
    private Label messageLabel;
    
    public LoginView(AuthService authService) {
        this.authService = authService;
        
        // 使用 CSS 类替代内联样式
        getStyleClass().addAll("bg-secondary");
        setSpacing(20);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(40));
        
        initComponents();
    }
    
    private void initComponents() {
        // 创建登录面板卡片
        VBox loginCard = new VBox(20);
        loginCard.getStyleClass().addAll("login-panel");
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setMaxWidth(450);
        
        // 标题 - 使用 CSS 类
        Label titleLabel = new Label("计算机科学与工程能力与潜力测评系统");
        titleLabel.getStyleClass().add("title-label");
        
        // 副标题
        Label subtitleLabel = new Label("用户登录");
        subtitleLabel.getStyleClass().add("subtitle-label");
        
        // 表单容器
        VBox formBox = new VBox(15);
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(10, 0, 10, 0));
        
        // 用户名
        usernameField = new TextField();
        usernameField.setPromptText("请输入用户名");
        usernameField.setPrefWidth(280);
        usernameField.getStyleClass().add("text-field");
        
        // 密码
        passwordField = new PasswordField();
        passwordField.setPromptText("请输入密码");
        passwordField.setPrefWidth(280);
        passwordField.getStyleClass().add("password-field");
        
        formBox.getChildren().addAll(usernameField, passwordField);
        
        // 消息标签 - 使用错误样式
        messageLabel = new Label();
        messageLabel.getStyleClass().add("label-danger");
        messageLabel.setVisible(false);
        
        // 按钮容器
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button loginButton = new Button("登录");
        loginButton.setPrefWidth(130);
        loginButton.getStyleClass().addAll("button", "button-large");
        loginButton.setOnAction(e -> performLogin());
        
        Button registerButton = new Button("注册账号");
        registerButton.setPrefWidth(130);
        registerButton.getStyleClass().addAll("button", "button-secondary", "button-large");
        registerButton.setOnAction(e -> showRegisterDialog());
        
        buttonBox.getChildren().addAll(loginButton, registerButton);
        
        // 回车键登录
        passwordField.setOnAction(e -> performLogin());
        usernameField.setOnAction(e -> performLogin());
        
        // 提示信息 - 使用徽章样式
        HBox tipBox = new HBox(10);
        tipBox.setAlignment(Pos.CENTER);
        
        Label tipBadge = new Label("提示");
        tipBadge.getStyleClass().addAll("badge", "badge-info");
        
        Label tipLabel = new Label("默认账号: admin / admin123");
        tipLabel.getStyleClass().add("caption-label");
        
        tipBox.getChildren().addAll(tipBadge, tipLabel);
        
        // 添加到登录卡片
        loginCard.getChildren().addAll(
            titleLabel,
            subtitleLabel,
            new Separator(),
            formBox,
            messageLabel,
            buttonBox,
            new Separator(),
            tipBox
        );
        
        // 添加到主容器
        getChildren().add(loginCard);
    }
    
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // 验证输入
        if (username.isEmpty()) {
            showError("请输入用户名");
            return;
        }
        
        if (password.isEmpty()) {
            showError("请输入密码");
            return;
        }
        
        // 清除错误样式
        clearError();
        
        // 执行登录
        String result = authService.login(username, password);
        
        if (result.equals("登录成功")) {
            // 跳转到主界面
            JavaFXApp.showMainView();
        } else {
            showError(result);
            passwordField.clear();
            passwordField.requestFocus();
        }
    }
    
    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);
        usernameField.getStyleClass().add("field-error");
        passwordField.getStyleClass().add("field-error");
    }
    
    private void clearError() {
        messageLabel.setVisible(false);
        usernameField.getStyleClass().remove("field-error");
        passwordField.getStyleClass().remove("field-error");
    }
    
    private void showRegisterDialog() {
        RegisterDialog dialog = new RegisterDialog(authService);
        dialog.showAndWait();
    }
}
