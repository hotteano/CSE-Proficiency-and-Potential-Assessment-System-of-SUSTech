package com.interview.view;

import com.interview.model.LLMConfig;
import com.interview.service.AuthService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

/**
 * 大模型API配置管理界面（管理员）
 */
public class LLMConfigView extends BorderPane {
    
    private final AuthService authService;
    private TableView<LLMConfig> configTable;
    
    public LLMConfigView(AuthService authService) {
        this.authService = authService;
        
        setPadding(new Insets(10));
        setStyle("-fx-background-color: white;");
        
        initComponents();
    }
    
    private void initComponents() {
        // 顶部标题
        Label titleLabel = new Label("大模型API配置管理");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 20));
        setTop(titleLabel);
        
        // 中心：配置列表
        setCenter(createConfigTablePanel());
        
        // 底部：操作按钮
        setBottom(createButtonPanel());
    }
    
    private VBox createConfigTablePanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10, 0, 10, 0));
        
        configTable = new TableView<>();
        
        TableColumn<LLMConfig, String> nameCol = new TableColumn<>("配置名称");
        nameCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getName()));
        nameCol.setPrefWidth(150);
        
        TableColumn<LLMConfig, String> providerCol = new TableColumn<>("提供商");
        providerCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getProviderDisplayName()));
        providerCol.setPrefWidth(120);
        
        TableColumn<LLMConfig, String> modelCol = new TableColumn<>("模型");
        modelCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getModelName()));
        modelCol.setPrefWidth(150);
        
        TableColumn<LLMConfig, String> defaultCol = new TableColumn<>("默认");
        defaultCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().isDefault() ? "是" : "否"));
        defaultCol.setPrefWidth(60);
        
        TableColumn<LLMConfig, String> statusCol = new TableColumn<>("状态");
        statusCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().isEnabled() ? "启用" : "禁用"));
        statusCol.setPrefWidth(60);
        
        configTable.getColumns().addAll(nameCol, providerCol, modelCol, defaultCol, statusCol);
        
        // 加载配置（简化版，实际需要调用服务）
        loadConfigs();
        
        panel.getChildren().add(configTable);
        VBox.setVgrow(configTable, Priority.ALWAYS);
        
        return panel;
    }
    
    private HBox createButtonPanel() {
        HBox panel = new HBox(15);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(10, 0, 0, 0));
        
        Button addBtn = new Button("添加配置");
        addBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white;");
        addBtn.setOnAction(e -> showAddConfigDialog());
        
        Button editBtn = new Button("编辑配置");
        editBtn.setOnAction(e -> showEditConfigDialog());
        
        Button setDefaultBtn = new Button("设为默认");
        setDefaultBtn.setOnAction(e -> setAsDefault());
        
        Button deleteBtn = new Button("删除");
        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> deleteConfig());
        
        Button testBtn = new Button("测试连接");
        testBtn.setOnAction(e -> testConnection());
        
        panel.getChildren().addAll(addBtn, editBtn, setDefaultBtn, deleteBtn, testBtn);
        
        return panel;
    }
    
    private void loadConfigs() {
        // 实际应从LLMManager加载
        // configTable.getItems().addAll(llmManager.getAllConfigs());
    }
    
    private void showAddConfigDialog() {
        Dialog<LLMConfig> dialog = new Dialog<>();
        dialog.setTitle("添加LLM配置");
        dialog.setHeaderText("配置新的大模型API");
        
        ButtonType saveBtn = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField nameField = new TextField();
        nameField.setPromptText("配置名称");
        
        ComboBox<LLMConfig.LLMProvider> providerCombo = new ComboBox<>();
        providerCombo.getItems().addAll(LLMConfig.LLMProvider.values());
        providerCombo.setValue(LLMConfig.LLMProvider.DEEPSEEK_THINKING);
        
        TextField modelField = new TextField();
        modelField.setText("deepseek-reasoner");
        
        TextField endpointField = new TextField();
        endpointField.setText("https://api.deepseek.com/v1/chat/completions");
        
        PasswordField apiKeyField = new PasswordField();
        apiKeyField.setPromptText("输入API Key");
        
        TextField timeoutField = new TextField("60");
        
        grid.addRow(0, new Label("配置名称:"), nameField);
        grid.addRow(1, new Label("提供商:"), providerCombo);
        grid.addRow(2, new Label("模型名称:"), modelField);
        grid.addRow(3, new Label("API端点:"), endpointField);
        grid.addRow(4, new Label("API Key:"), apiKeyField);
        grid.addRow(5, new Label("超时(秒):"), timeoutField);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                LLMConfig config = new LLMConfig();
                config.setName(nameField.getText());
                config.setProvider(providerCombo.getValue());
                config.setModelName(modelField.getText());
                config.setApiEndpoint(endpointField.getText());
                config.setApiKey(apiKeyField.getText());
                try {
                    config.setTimeout(Integer.parseInt(timeoutField.getText()));
                } catch (NumberFormatException e) {
                    config.setTimeout(60);
                }
                return config;
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(config -> {
            // 保存配置
            // llmManager.addConfig(config);
            loadConfigs();
        });
    }
    
    private void showEditConfigDialog() {
        LLMConfig selected = configTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("提示", "请先选择要编辑的配置", Alert.AlertType.WARNING);
            return;
        }
        // 类似添加对话框，填充现有值
    }
    
    private void setAsDefault() {
        LLMConfig selected = configTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("提示", "请先选择配置", Alert.AlertType.WARNING);
            return;
        }
        // llmManager.setDefaultConfig(selected.getId());
        loadConfigs();
        showAlert("成功", "已设为默认配置", Alert.AlertType.INFORMATION);
    }
    
    private void deleteConfig() {
        LLMConfig selected = configTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("提示", "请先选择配置", Alert.AlertType.WARNING);
            return;
        }
        // 确认删除
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("确认删除");
        confirm.setContentText("确定要删除配置 [" + selected.getName() + "] 吗？");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                // llmManager.deleteConfig(selected.getId());
                loadConfigs();
            }
        });
    }
    
    private void testConnection() {
        LLMConfig selected = configTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("提示", "请先选择配置", Alert.AlertType.WARNING);
            return;
        }
        // 测试API连接
        showAlert("测试", "连接测试中...", Alert.AlertType.INFORMATION);
    }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
