package com.interview.view;

import com.interview.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * 修改密码对话框（JavaFX）
 * 应用新 CSS 设计
 */
public class ChangePasswordDialog extends Dialog<Boolean> {
    
    private final AuthService authService;
    
    private PasswordField oldPasswordField;
    private PasswordField newPasswordField;
    private PasswordField confirmPasswordField;
    private Label messageLabel;
    
    public ChangePasswordDialog(AuthService authService) {
        this.authService = authService;
        
        setTitle("🔐 修改密码");
        setHeaderText("修改登录密码");
        
        // 应用对话框样式
        getDialogPane().getStyleClass().add("dialog-pane");
        
        initComponents();
        
        ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);
        
        // 样式化按钮
        Button saveButton = (Button) getDialogPane().lookupButton(saveButtonType);
        saveButton.getStyleClass().addAll("button", "button-success");
        
        Button cancelButton = (Button) getDialogPane().lookupButton(cancelButtonType);
        cancelButton.getStyleClass().addAll("button", "button-secondary");
        
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!changePassword()) {
                event.consume();
            }
        });
        
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return true;
            }
            return false;
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
        
        // 旧密码
        Label oldPassLabel = createFormLabel("旧密码", true);
        oldPasswordField = createPasswordField("请输入旧密码");
        grid.add(oldPassLabel, 0, 0);
        grid.add(oldPasswordField, 1, 0);
        
        // 新密码
        Label newPassLabel = createFormLabel("新密码", true);
        newPasswordField = createPasswordField("至少6位密码");
        grid.add(newPassLabel, 0, 1);
        grid.add(newPasswordField, 1, 1);
        
        // 确认密码
        Label confirmLabel = createFormLabel("确认密码", true);
        confirmPasswordField = createPasswordField("再次输入新密码");
        grid.add(confirmLabel, 0, 2);
        grid.add(confirmPasswordField, 1, 2);
        
        // 消息标签
        messageLabel = new Label();
        messageLabel.getStyleClass().add("label-danger");
        messageLabel.setVisible(false);
        grid.add(messageLabel, 1, 3);
        
        // 提示卡片
        VBox tipCard = new VBox(8);
        tipCard.getStyleClass().addAll("card-flat", "alert-info");
        tipCard.setPadding(new Insets(12));
        
        Label tipTitle = new Label("💡 密码要求");
        tipTitle.getStyleClass().add("caption-label");
        
        Label tipLabel = new Label("• 密码长度至少6位\n• 建议使用字母、数字组合\n• 定期更换密码可提高安全性");
        tipLabel.getStyleClass().add("caption-label");
        tipLabel.setStyle("-fx-line-spacing: 3px;");
        
        tipCard.getChildren().addAll(tipTitle, tipLabel);
        grid.add(tipCard, 1, 4);
        
        mainBox.getChildren().add(grid);
        getDialogPane().setContent(mainBox);
        getDialogPane().setPrefWidth(400);
    }
    
    private Label createFormLabel(String text, boolean required) {
        Label label = new Label(text + (required ? " *" : ""));
        label.getStyleClass().add("text-secondary");
        return label;
    }
    
    private PasswordField createPasswordField(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        field.setPrefWidth(220);
        field.getStyleClass().add("password-field");
        return field;
    }
    
    private boolean changePassword() {
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        clearErrors();
        
        if (oldPassword.isEmpty()) {
            showError("请输入旧密码", oldPasswordField);
            return false;
        }
        
        if (newPassword.isEmpty()) {
            showError("请输入新密码", newPasswordField);
            return false;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            showError("两次输入的新密码不一致", confirmPasswordField);
            confirmPasswordField.clear();
            return false;
        }
        
        String result = authService.changePassword(oldPassword, newPassword);
        
        if (result.contains("成功")) {
            showSuccess(result);
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
    
    private void clearErrors() {
        messageLabel.setVisible(false);
        oldPasswordField.getStyleClass().remove("field-error");
        newPasswordField.getStyleClass().remove("field-error");
        confirmPasswordField.getStyleClass().remove("field-error");
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("✅ 成功");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStyleClass().add("dialog-pane");
        alert.showAndWait();
    }
}
