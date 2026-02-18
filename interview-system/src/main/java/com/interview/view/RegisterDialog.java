package com.interview.view;

import com.interview.model.Role;
import com.interview.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;

/**
 * 用户注册对话框（JavaFX）
 * 应用新 CSS 设计
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
        
        // 应用对话框样式
        getDialogPane().getStyleClass().add("dialog-pane");
        
        // 创建内容
        initComponents();
        
        // 设置按钮
        ButtonType registerButtonType = new ButtonType("注册", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(registerButtonType, cancelButtonType);
        
        // 处理注册按钮
        Button registerButton = (Button) getDialogPane().lookupButton(registerButtonType);
        registerButton.getStyleClass().addAll("button", "button-success");
        registerButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!performRegister()) {
                event.consume();
            }
        });
        
        // 处理取消按钮样式
        Button cancelButton = (Button) getDialogPane().lookupButton(cancelButtonType);
        cancelButton.getStyleClass().addAll("button", "button-secondary");
        
        // 设置结果转换
        setResultConverter(dialogButton -> {
            if (dialogButton == registerButtonType) {
                return "注册成功";
            }
            return null;
        });
    }
    
    private void initComponents() {
        VBox mainBox = new VBox(15);
        mainBox.setPadding(new Insets(10));
        mainBox.setAlignment(Pos.CENTER);
        
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setAlignment(Pos.CENTER);
        
        int row = 0;
        
        // 用户名
        Label userLabel = createFormLabel("用户名", true);
        usernameField = createFormField("请输入用户名");
        grid.add(userLabel, 0, row);
        grid.add(usernameField, 1, row++);
        
        // 密码
        Label passLabel = createFormLabel("密码", true);
        passwordField = createFormPasswordField("至少6位密码");
        grid.add(passLabel, 0, row);
        grid.add(passwordField, 1, row++);
        
        // 确认密码
        Label confirmLabel = createFormLabel("确认密码", true);
        confirmPasswordField = createFormPasswordField("再次输入密码");
        grid.add(confirmLabel, 0, row);
        grid.add(confirmPasswordField, 1, row++);
        
        // 真实姓名
        Label nameLabel = createFormLabel("真实姓名", true);
        realNameField = createFormField("请输入真实姓名");
        grid.add(nameLabel, 0, row);
        grid.add(realNameField, 1, row++);
        
        // 邮箱
        Label emailLabel = createFormLabel("邮箱", false);
        emailField = createFormField("选填");
        grid.add(emailLabel, 0, row);
        grid.add(emailField, 1, row++);
        
        // 角色
        Label roleLabel = createFormLabel("角色", true);
        roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll(
            new RoleItem(Role.CANDIDATE),
            new RoleItem(Role.EXAMINER),
            new RoleItem(Role.QUESTION_CREATOR)
        );
        roleComboBox.setValue(roleComboBox.getItems().get(0));
        roleComboBox.getStyleClass().add("combo-box");
        roleComboBox.setPrefWidth(250);
        grid.add(roleLabel, 0, row);
        grid.add(roleComboBox, 1, row++);
        
        // 角色说明卡片
        VBox roleCard = new VBox(8);
        roleCard.getStyleClass().addAll("card-flat", "alert-info");
        roleCard.setPadding(new Insets(12));
        
        Label roleDescTitle = new Label("📋 角色说明");
        roleDescTitle.getStyleClass().add("heading-label");
        
        Label roleDesc = new Label(
            "👤 考生：可录入语音、查看自己的面试记录\n" +
            "👨‍💼 考官：可抽取题目、查看面试记录、评分\n" +
            "✍️ 出题人：可管理题目库"
        );
        roleDesc.getStyleClass().add("caption-label");
        roleDesc.setStyle("-fx-line-spacing: 5px;");
        
        roleCard.getChildren().addAll(roleDescTitle, roleDesc);
        grid.add(roleCard, 1, row++);
        
        // 消息标签
        messageLabel = new Label();
        messageLabel.getStyleClass().add("label-danger");
        messageLabel.setVisible(false);
        messageLabel.setWrapText(true);
        grid.add(messageLabel, 1, row);
        
        mainBox.getChildren().add(grid);
        getDialogPane().setContent(mainBox);
    }
    
    private Label createFormLabel(String text, boolean required) {
        Label label = new Label(text + (required ? " *" : " "));
        label.getStyleClass().add("text-secondary");
        return label;
    }
    
    private TextField createFormField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefWidth(250);
        field.getStyleClass().add("text-field");
        return field;
    }
    
    private PasswordField createFormPasswordField(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        field.setPrefWidth(250);
        field.getStyleClass().add("password-field");
        return field;
    }
    
    private boolean performRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String realName = realNameField.getText().trim();
        String email = emailField.getText().trim();
        RoleItem selectedRole = roleComboBox.getValue();
        
        // 清除之前的错误样式
        clearFieldErrors();
        
        // 验证输入
        if (username.isEmpty()) {
            showError("请输入用户名", usernameField);
            return false;
        }
        
        if (password.isEmpty()) {
            showError("请输入密码", passwordField);
            return false;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("两次输入的密码不一致", confirmPasswordField);
            confirmPasswordField.clear();
            return false;
        }
        
        if (realName.isEmpty()) {
            showError("请输入真实姓名", realNameField);
            return false;
        }
        
        // 执行注册
        String result = authService.register(username, password, realName, 
                                             email, selectedRole.getRole());
        
        if (result.equals("注册成功")) {
            return true;
        } else {
            showError(result, null);
            return false;
        }
    }
    
    private void showError(String message, Control field) {
        messageLabel.setText("⚠️ " + message);
        messageLabel.setVisible(true);
        if (field != null) {
            field.getStyleClass().add("field-error");
        }
    }
    
    private void clearFieldErrors() {
        messageLabel.setVisible(false);
        usernameField.getStyleClass().remove("field-error");
        passwordField.getStyleClass().remove("field-error");
        confirmPasswordField.getStyleClass().remove("field-error");
        realNameField.getStyleClass().remove("field-error");
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
