package com.interview.view;

import com.interview.model.Role;
import com.interview.model.User;
import com.interview.service.UserService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

/**
 * 用户编辑对话框（JavaFX）
 */
public class UserEditDialog extends Dialog<Boolean> {
    
    private final UserService userService;
    private final User user;
    
    private TextField realNameField;
    private TextField emailField;
    private ComboBox<RoleItem> roleComboBox;
    private CheckBox activeCheckBox;
    
    public UserEditDialog(UserService userService, User user) {
        this.userService = userService;
        this.user = user;
        
        setTitle("编辑用户");
        setHeaderText("编辑用户: " + user.getUsername());
        
        initComponents();
        loadUserData();
        
        ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);
        
        Button saveButton = (Button) getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!saveUser()) {
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
        
        // 用户名（只读）
        grid.add(new Label("用户名:"), 0, 0);
        Label usernameLabel = new Label(user.getUsername());
        usernameLabel.setStyle("-fx-font-weight: bold;");
        grid.add(usernameLabel, 1, 0);
        
        // 真实姓名
        grid.add(new Label("真实姓名:"), 0, 1);
        realNameField = new TextField();
        grid.add(realNameField, 1, 1);
        
        // 邮箱
        grid.add(new Label("邮箱:"), 0, 2);
        emailField = new TextField();
        grid.add(emailField, 1, 2);
        
        // 角色
        grid.add(new Label("角色:"), 0, 3);
        roleComboBox = new ComboBox<>();
        for (Role role : Role.values()) {
            roleComboBox.getItems().add(new RoleItem(role));
        }
        grid.add(roleComboBox, 1, 3);
        
        // 启用状态
        grid.add(new Label("账号状态:"), 0, 4);
        activeCheckBox = new CheckBox("启用账号");
        grid.add(activeCheckBox, 1, 4);
        
        getDialogPane().setContent(grid);
        getDialogPane().setPrefWidth(350);
    }
    
    private void loadUserData() {
        realNameField.setText(user.getRealName());
        emailField.setText(user.getEmail());
        activeCheckBox.setSelected(user.isActive());
        
        // 设置角色
        for (RoleItem item : roleComboBox.getItems()) {
            if (item.getRole() == user.getRole()) {
                roleComboBox.setValue(item);
                break;
            }
        }
    }
    
    private boolean saveUser() {
        String realName = realNameField.getText().trim();
        
        if (realName.isEmpty()) {
            showError("真实姓名不能为空");
            return false;
        }
        
        user.setRealName(realName);
        user.setEmail(emailField.getText().trim());
        user.setActive(activeCheckBox.isSelected());
        
        RoleItem selectedRole = roleComboBox.getValue();
        if (selectedRole != null) {
            user.setRole(selectedRole.getRole());
        }
        
        String result = userService.updateUser(user);
        
        if (result.contains("成功")) {
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
