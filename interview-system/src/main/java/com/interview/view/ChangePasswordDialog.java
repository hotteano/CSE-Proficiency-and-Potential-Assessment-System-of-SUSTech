package com.interview.view;

import com.interview.service.AuthService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

/**
 * 修改密码对话框（JavaFX）
 */
public class ChangePasswordDialog extends Dialog<Boolean> {
    
    private final AuthService authService;
    
    private PasswordField oldPasswordField;
    private PasswordField newPasswordField;
    private PasswordField confirmPasswordField;
    
    public ChangePasswordDialog(AuthService authService) {
        this.authService = authService;
        
        setTitle("修改密码");
        setHeaderText("修改登录密码");
        
        initComponents();
        
        ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);
        
        Button saveButton = (Button) getDialogPane().lookupButton(saveButtonType);
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
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        // 旧密码
        grid.add(new Label("旧密码:"), 0, 0);
        oldPasswordField = new PasswordField();
        oldPasswordField.setPromptText("请输入旧密码");
        grid.add(oldPasswordField, 1, 0);
        
        // 新密码
        grid.add(new Label("新密码:"), 0, 1);
        newPasswordField = new PasswordField();
        newPasswordField.setPromptText("至少6位");
        grid.add(newPasswordField, 1, 1);
        
        // 确认密码
        grid.add(new Label("确认密码:"), 0, 2);
        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("再次输入新密码");
        grid.add(confirmPasswordField, 1, 2);
        
        // 提示
        Label tipLabel = new Label("提示: 密码长度至少6位");
        tipLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");
        grid.add(tipLabel, 1, 3);
        
        getDialogPane().setContent(grid);
        getDialogPane().setPrefWidth(350);
    }
    
    private boolean changePassword() {
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        if (oldPassword.isEmpty()) {
            showError("请输入旧密码");
            return false;
        }
        
        if (newPassword.isEmpty()) {
            showError("请输入新密码");
            return false;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            showError("两次输入的新密码不一致");
            confirmPasswordField.clear();
            return false;
        }
        
        String result = authService.changePassword(oldPassword, newPassword);
        
        if (result.contains("成功")) {
            showInfo(result);
            return true;
        } else {
            showError(result);
            return false;
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("成功");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
