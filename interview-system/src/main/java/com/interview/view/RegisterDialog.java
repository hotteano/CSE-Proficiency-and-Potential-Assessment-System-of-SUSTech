package com.interview.view;

import com.interview.model.Role;
import com.interview.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * 用户注册对话框（JavaFX）
 */
public class RegisterDialog extends Dialog<String> {
    
    private final AuthService authService;
    
    private TextField usernameField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private TextField realNameField;
    private TextField emailField;
    private ComboBox<RoleItem> roleComboBox;
    private Label messageLabel;
    
    public RegisterDialog(AuthService authService) {
        this.authService = authService;
        
        setTitle("用户注册");
        setHeaderText("创建新账号");
        
        // 设置对话框模态
        initModality(Modality.APPLICATION_MODAL);
        
        // 创建内容
        initComponents();
        
        // 设置按钮
        ButtonType registerButtonType = new ButtonType("注册", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(registerButtonType, cancelButtonType);
        
        // 处理注册按钮
        Button registerButton = (Button) getDialogPane().lookupButton(registerButtonType);
        registerButton.setStyle("-fx-background-color: #4682b4; -fx-text-fill: white; -fx-font-weight: bold;");
        registerButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!performRegister()) {
                event.consume();
            }
        });
        
        // 设置结果转换
        setResultConverter(dialogButton -> {
            if (dialogButton == registerButtonType) {
                return "注册成功";
            }
            return null;
        });
    }
    
    private void initComponents() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        
        // 用户名
        grid.add(new Label("用户名:*"), 0, 0);
        usernameField = new TextField();
        usernameField.setPromptText("请输入用户名");
        usernameField.setPrefWidth(250);
        grid.add(usernameField, 1, 0);
        
        // 密码
        grid.add(new Label("密码:*"), 0, 1);
        passwordField = new PasswordField();
        passwordField.setPromptText("至少6位密码");
        grid.add(passwordField, 1, 1);
        
        // 确认密码
        grid.add(new Label("确认密码:*"), 0, 2);
        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("再次输入密码");
        grid.add(confirmPasswordField, 1, 2);
        
        // 真实姓名
        grid.add(new Label("真实姓名:*"), 0, 3);
        realNameField = new TextField();
        realNameField.setPromptText("请输入真实姓名");
        grid.add(realNameField, 1, 3);
        
        // 邮箱
        grid.add(new Label("邮箱:"), 0, 4);
        emailField = new TextField();
        emailField.setPromptText("选填");
        grid.add(emailField, 1, 4);
        
        // 角色
        grid.add(new Label("角色:*"), 0, 5);
        roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll(
            new RoleItem(Role.CANDIDATE),
            new RoleItem(Role.EXAMINER),
            new RoleItem(Role.QUESTION_CREATOR)
        );
        roleComboBox.setValue(roleComboBox.getItems().get(0));
        grid.add(roleComboBox, 1, 5);
        
        // 角色说明
        TextArea roleDesc = new TextArea(
            "考生：可录入语音、查看自己的面试记录\n" +
            "考官：可抽取题目、查看面试记录\n" +
            "试题编制者：可管理题目"
        );
        roleDesc.setEditable(false);
        roleDesc.setWrapText(true);
        roleDesc.setPrefRowCount(3);
        roleDesc.setStyle("-fx-control-inner-background: #f5f5f5; -fx-font-size: 11px;");
        grid.add(roleDesc, 1, 6);
        
        // 消息标签
        messageLabel = new Label();
        messageLabel.setTextFill(Color.web("#dc3545"));
        messageLabel.setWrapText(true);
        grid.add(messageLabel, 1, 7);
        
        getDialogPane().setContent(grid);
    }
    
    private boolean performRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String realName = realNameField.getText().trim();
        String email = emailField.getText().trim();
        RoleItem selectedRole = roleComboBox.getValue();
        
        // 验证输入
        if (username.isEmpty()) {
            messageLabel.setText("请输入用户名");
            return false;
        }
        
        if (password.isEmpty()) {
            messageLabel.setText("请输入密码");
            return false;
        }
        
        if (!password.equals(confirmPassword)) {
            messageLabel.setText("两次输入的密码不一致");
            confirmPasswordField.clear();
            return false;
        }
        
        if (realName.isEmpty()) {
            messageLabel.setText("请输入真实姓名");
            return false;
        }
        
        // 执行注册
        String result = authService.register(username, password, realName, 
                                             email, selectedRole.getRole());
        
        if (result.equals("注册成功")) {
            return true;
        } else {
            messageLabel.setText(result);
            return false;
        }
    }
    
    /**
     * 角色包装类
     */
    private static class RoleItem {
        private final Role role;
        
        public RoleItem(Role role) {
            this.role = role;
        }
        
        public Role getRole() {
            return role;
        }
        
        @Override
        public String toString() {
            return role.getDisplayName();
        }
    }
}
