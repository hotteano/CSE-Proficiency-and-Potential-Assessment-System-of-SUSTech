package com.interview.view;

import com.interview.model.User;
import com.interview.service.UserService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * 用户管理视图（JavaFX）
 * 应用新 CSS 设计
 */
public class UserManageView extends BorderPane {
    
    private final UserService userService;
    
    private TableView<User> userTable;
    
    public UserManageView(UserService userService) {
        this.userService = userService;
        
        setPadding(new Insets(20));
        getStyleClass().add("bg-secondary");
        
        setTop(createButtonPanel());
        setCenter(createTablePanel());
        
        loadUsers();
    }
    
    private VBox createButtonPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(0, 0, 15, 0));
        
        // 标题栏
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("👥 用户管理");
        titleLabel.getStyleClass().add("heading-label");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        titleBox.getChildren().addAll(titleLabel, spacer);
        
        // 按钮栏卡片
        HBox buttonCard = new HBox(12);
        buttonCard.getStyleClass().addAll("card-flat", "p-3");
        buttonCard.setAlignment(Pos.CENTER_LEFT);
        
        Button editBtn = new Button("✏️ 编辑用户");
        editBtn.getStyleClass().addAll("button", "button-small");
        editBtn.setOnAction(e -> showEditDialog());
        
        Button toggleBtn = new Button("🔓 启用/禁用");
        toggleBtn.getStyleClass().addAll("button", "button-secondary", "button-small");
        toggleBtn.setOnAction(e -> toggleUserStatus());
        
        Button resetPassBtn = new Button("🔐 重置密码");
        resetPassBtn.getStyleClass().addAll("button", "button-warning", "button-small");
        resetPassBtn.setOnAction(e -> resetPassword());
        
        Button deleteBtn = new Button("🗑️ 删除用户");
        deleteBtn.getStyleClass().addAll("button", "button-danger", "button-small");
        deleteBtn.setOnAction(e -> deleteUser());
        
        Region btnSpacer = new Region();
        HBox.setHgrow(btnSpacer, Priority.ALWAYS);
        
        Button refreshBtn = new Button("🔄 刷新");
        refreshBtn.getStyleClass().addAll("button", "button-secondary", "button-small");
        refreshBtn.setOnAction(e -> loadUsers());
        
        buttonCard.getChildren().addAll(
            editBtn, toggleBtn, resetPassBtn, deleteBtn, 
            btnSpacer, refreshBtn
        );
        
        panel.getChildren().addAll(titleBox, buttonCard);
        
        return panel;
    }
    
    private VBox createTablePanel() {
        VBox panel = new VBox(10);
        panel.getStyleClass().addAll("card", "p-3");
        panel.setPadding(new Insets(15));
        
        userTable = new TableView<>();
        userTable.getStyleClass().add("table-view");
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<User, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(String.valueOf(cell.getValue().getId())));
        idCol.setPrefWidth(50);
        idCol.setStyle("-fx-alignment: CENTER;");
        
        TableColumn<User, String> usernameCol = new TableColumn<>("用户名");
        usernameCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getUsername()));
        usernameCol.setPrefWidth(120);
        
        TableColumn<User, String> realNameCol = new TableColumn<>("真实姓名");
        realNameCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getRealName()));
        realNameCol.setPrefWidth(120);
        
        TableColumn<User, String> emailCol = new TableColumn<>("邮箱");
        emailCol.setCellValueFactory(cell -> {
            String email = cell.getValue().getEmail();
            return new SimpleStringProperty(email != null ? email : "-");
        });
        emailCol.setPrefWidth(150);
        
        TableColumn<User, String> roleCol = new TableColumn<>("角色");
        roleCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getRoleDisplayName()));
        roleCol.setPrefWidth(100);
        
        TableColumn<User, String> statusCol = new TableColumn<>("状态");
        statusCol.setCellValueFactory(cell -> {
            boolean active = cell.getValue().isActive();
            return new SimpleStringProperty(active ? "✅ 启用" : "❌ 禁用");
        });
        statusCol.setPrefWidth(80);
        
        TableColumn<User, String> createdCol = new TableColumn<>("创建时间");
        createdCol.setCellValueFactory(cell -> {
            var date = cell.getValue().getCreatedAt();
            return new SimpleStringProperty(date != null ? date.toLocalDate().toString() : "");
        });
        createdCol.setPrefWidth(100);
        
        userTable.getColumns().addAll(
            idCol, usernameCol, realNameCol, emailCol, 
            roleCol, statusCol, createdCol
        );
        
        panel.getChildren().add(userTable);
        VBox.setVgrow(userTable, Priority.ALWAYS);
        
        return panel;
    }
    
    private void loadUsers() {
        List<User> users = userService.getAllUsers();
        userTable.getItems().clear();
        userTable.getItems().addAll(users);
    }
    
    private void showEditDialog() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("⚠️ 提示", "请先选择要编辑的用户", Alert.AlertType.WARNING);
            return;
        }
        
        UserEditDialog dialog = new UserEditDialog(userService, selected);
        dialog.getDialogPane().getStyleClass().add("dialog-pane");
        dialog.showAndWait().ifPresent(result -> {
            if (result) {
                loadUsers();
            }
        });
    }
    
    private void toggleUserStatus() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("⚠️ 提示", "请先选择要操作的用户", Alert.AlertType.WARNING);
            return;
        }
        
        String result = userService.toggleUserStatus(selected.getId());
        showAlert(
            result.contains("成功") ? "✅ 成功" : "❌ 错误",
            result,
            result.contains("成功") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR
        );
        loadUsers();
    }
    
    private void resetPassword() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("⚠️ 提示", "请先选择要重置密码的用户", Alert.AlertType.WARNING);
            return;
        }
        
        // 密码输入对话框
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("🔐 重置密码");
        dialog.setHeaderText("为用户 [" + selected.getUsername() + "] 设置新密码");
        dialog.getDialogPane().getStyleClass().add("dialog-pane");
        
        ButtonType confirmType = new ButtonType("确认", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        
        PasswordField newPass = new PasswordField();
        newPass.setPromptText("输入新密码");
        newPass.getStyleClass().add("password-field");
        
        PasswordField confirmPass = new PasswordField();
        confirmPass.setPromptText("确认新密码");
        confirmPass.getStyleClass().add("password-field");
        
        grid.add(new Label("新密码:"), 0, 0);
        grid.add(newPass, 1, 0);
        grid.add(new Label("确认密码:"), 0, 1);
        grid.add(confirmPass, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmType) {
                return new String[]{newPass.getText(), confirmPass.getText()};
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(result -> {
            if (result[0].isEmpty()) {
                showAlert("❌ 错误", "密码不能为空", Alert.AlertType.ERROR);
                return;
            }
            if (!result[0].equals(result[1])) {
                showAlert("❌ 错误", "两次输入的密码不一致", Alert.AlertType.ERROR);
                return;
            }
            
            com.interview.service.AuthService authService = new com.interview.service.AuthService();
            String msg = authService.resetPassword(selected.getId(), result[0]);
            showAlert(
                msg.contains("成功") ? "✅ 成功" : "❌ 错误",
                msg,
                msg.contains("成功") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR
            );
        });
    }
    
    private void deleteUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("⚠️ 提示", "请先选择要删除的用户", Alert.AlertType.WARNING);
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("⚠️ 确认删除");
        confirm.setHeaderText("删除用户");
        confirm.setContentText("确定要删除用户 [" + selected.getUsername() + "] 吗？\n此操作不可恢复！");
        confirm.getDialogPane().getStyleClass().add("dialog-pane");
        
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                String msg = userService.deleteUser(selected.getId());
                showAlert(
                    msg.contains("成功") ? "✅ 成功" : "❌ 错误",
                    msg,
                    msg.contains("成功") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR
                );
                loadUsers();
            }
        });
    }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().getStyleClass().add("dialog-pane");
        alert.showAndWait();
    }
}
