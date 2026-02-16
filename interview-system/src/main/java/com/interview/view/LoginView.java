package com.interview.view;

import com.interview.JavaFXApp;
import com.interview.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * 登录界面（JavaFX）
 */
public class LoginView extends VBox {
    
    private final AuthService authService;
    
    private TextField usernameField;
    private PasswordField passwordField;
    private Label messageLabel;
    
    public LoginView(AuthService authService) {
        this.authService = authService;
        
        setSpacing(20);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(40));
        setStyle("-fx-background-color: white;");
        
        initComponents();
    }
    
    private void initComponents() {
        // 标题
        Label titleLabel = new Label("计算机科学与工程能力与潜力测评系统");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web("#4682b4"));
        
        // 副标题
        Label subtitleLabel = new Label("用户登录");
        subtitleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.NORMAL, 16));
        subtitleLabel.setTextFill(Color.web("#666"));
        
        // 表单容器
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(15);
        formGrid.setAlignment(Pos.CENTER);
        
        // 用户名
        Label userLabel = new Label("用户名:");
        userLabel.setFont(Font.font(14));
        usernameField = new TextField();
        usernameField.setPromptText("请输入用户名");
        usernameField.setPrefWidth(250);
        usernameField.setPrefHeight(35);
        
        formGrid.add(userLabel, 0, 0);
        formGrid.add(usernameField, 1, 0);
        
        // 密码
        Label passLabel = new Label("密码:");
        passLabel.setFont(Font.font(14));
        passwordField = new PasswordField();
        passwordField.setPromptText("请输入密码");
        passwordField.setPrefWidth(250);
        passwordField.setPrefHeight(35);
        
        formGrid.add(passLabel, 0, 1);
        formGrid.add(passwordField, 1, 1);
        
        // 消息标签
        messageLabel = new Label();
        messageLabel.setTextFill(Color.web("#dc3545"));
        messageLabel.setFont(Font.font(12));
        
        // 按钮容器
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button loginButton = new Button("登录");
        loginButton.setPrefWidth(120);
        loginButton.setPrefHeight(40);
        loginButton.setStyle("-fx-background-color: #4682b4; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        loginButton.setOnAction(e -> performLogin());
        
        Button registerButton = new Button("注册");
        registerButton.setPrefWidth(120);
        registerButton.setPrefHeight(40);
        registerButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        registerButton.setOnAction(e -> showRegisterDialog());
        
        buttonBox.getChildren().addAll(loginButton, registerButton);
        
        // 回车键登录
        passwordField.setOnAction(e -> performLogin());
        usernameField.setOnAction(e -> performLogin());
        
        // 提示信息
        Label tipLabel = new Label("默认账号: admin / admin123");
        tipLabel.setFont(Font.font(11));
        tipLabel.setTextFill(Color.web("#888"));
        
        // 添加到主容器
        getChildren().addAll(
            titleLabel,
            subtitleLabel,
            new Separator(),
            formGrid,
            messageLabel,
            buttonBox,
            new Separator(),
            tipLabel
        );
    }
    
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // 验证输入
        if (username.isEmpty()) {
            messageLabel.setText("请输入用户名");
            return;
        }
        
        if (password.isEmpty()) {
            messageLabel.setText("请输入密码");
            return;
        }
        
        // 执行登录
        String result = authService.login(username, password);
        
        if (result.equals("登录成功")) {
            // 跳转到主界面
            JavaFXApp.showMainView();
        } else {
            messageLabel.setText(result);
            passwordField.clear();
            passwordField.requestFocus();
        }
    }
    
    private void showRegisterDialog() {
        RegisterDialog dialog = new RegisterDialog(authService);
        dialog.showAndWait();
    }
}
