package com.interview.view;

import com.interview.model.Question;
import com.interview.service.QuestionService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * 题目管理视图（JavaFX）
 * 应用新 CSS 设计
 */
public class QuestionManageView extends BorderPane {
    
    private final QuestionService questionService;
    
    private TableView<Question> questionTable;
    
    public QuestionManageView(QuestionService questionService) {
        this.questionService = questionService;
        
        setPadding(new Insets(20));
        getStyleClass().add("bg-secondary");
        
        // 顶部按钮栏
        setTop(createButtonPanel());
        
        // 中心表格
        setCenter(createTablePanel());
        
        // 加载数据
        loadQuestions();
    }
    
    private VBox createButtonPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(0, 0, 15, 0));
        
        // 标题栏
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("✏️ 题目管理");
        titleLabel.getStyleClass().add("heading-label");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        titleBox.getChildren().addAll(titleLabel, spacer);
        
        // 按钮栏卡片
        HBox buttonCard = new HBox(12);
        buttonCard.getStyleClass().addAll("card-flat", "p-3");
        buttonCard.setAlignment(Pos.CENTER_LEFT);
        
        Button addBtn = new Button("➕ 新增题目");
        addBtn.getStyleClass().addAll("button", "button-success", "button-small");
        addBtn.setOnAction(e -> showAddDialog());
        
        Button editBtn = new Button("✏️ 编辑题目");
        editBtn.getStyleClass().addAll("button", "button-small");
        editBtn.setOnAction(e -> showEditDialog());
        
        Button deleteBtn = new Button("🗑️ 删除题目");
        deleteBtn.getStyleClass().addAll("button", "button-danger", "button-small");
        deleteBtn.setOnAction(e -> deleteQuestion());
        
        Region btnSpacer = new Region();
        HBox.setHgrow(btnSpacer, Priority.ALWAYS);
        
        Button refreshBtn = new Button("🔄 刷新");
        refreshBtn.getStyleClass().addAll("button", "button-secondary", "button-small");
        refreshBtn.setOnAction(e -> loadQuestions());
        
        buttonCard.getChildren().addAll(addBtn, editBtn, deleteBtn, btnSpacer, refreshBtn);
        
        panel.getChildren().addAll(titleBox, buttonCard);
        
        return panel;
    }
    
    private VBox createTablePanel() {
        VBox panel = new VBox(10);
        panel.getStyleClass().addAll("card", "p-3");
        panel.setPadding(new Insets(15));
        
        questionTable = new TableView<>();
        questionTable.getStyleClass().add("table-view");
        questionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Question, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(String.valueOf(cell.getValue().getId())));
        idCol.setPrefWidth(50);
        idCol.setStyle("-fx-alignment: CENTER;");
        
        TableColumn<Question, String> titleCol = new TableColumn<>("标题");
        titleCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getTitle()));
        titleCol.setPrefWidth(250);
        
        TableColumn<Question, String> typeCol = new TableColumn<>("类型");
        typeCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getTypeDisplayName()));
        typeCol.setPrefWidth(100);
        
        TableColumn<Question, String> diffCol = new TableColumn<>("难度");
        diffCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getLevelDisplayName()));
        diffCol.setPrefWidth(90);
        
        TableColumn<Question, String> catCol = new TableColumn<>("分类");
        catCol.setCellValueFactory(cell -> {
            String cat = cell.getValue().getCategory();
            return new SimpleStringProperty(cat != null ? cat : "-");
        });
        catCol.setPrefWidth(120);
        
        TableColumn<Question, String> creatorCol = new TableColumn<>("创建者");
        creatorCol.setCellValueFactory(cell -> {
            String creator = cell.getValue().getCreatedBy();
            return new SimpleStringProperty(creator != null ? creator : "-");
        });
        creatorCol.setPrefWidth(100);
        
        questionTable.getColumns().addAll(idCol, titleCol, typeCol, diffCol, catCol, creatorCol);
        
        panel.getChildren().add(questionTable);
        VBox.setVgrow(questionTable, Priority.ALWAYS);
        
        return panel;
    }
    
    private void loadQuestions() {
        List<Question> questions = questionService.getAllQuestions();
        questionTable.getItems().clear();
        questionTable.getItems().addAll(questions);
    }
    
    private void showAddDialog() {
        QuestionEditDialog dialog = new QuestionEditDialog(questionService, null);
        dialog.getDialogPane().getStyleClass().add("dialog-pane");
        dialog.showAndWait().ifPresent(result -> {
            if (result) {
                loadQuestions();
            }
        });
    }
    
    private void showEditDialog() {
        Question selected = questionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("⚠️ 提示", "请先选择要编辑的题目", Alert.AlertType.WARNING);
            return;
        }
        
        QuestionEditDialog dialog = new QuestionEditDialog(questionService, selected);
        dialog.getDialogPane().getStyleClass().add("dialog-pane");
        dialog.showAndWait().ifPresent(result -> {
            if (result) {
                loadQuestions();
            }
        });
    }
    
    private void deleteQuestion() {
        Question selected = questionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("⚠️ 提示", "请先选择要删除的题目", Alert.AlertType.WARNING);
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("⚠️ 确认删除");
        confirm.setHeaderText("删除题目");
        confirm.setContentText("确定要删除题目 [" + selected.getTitle() + "] 吗？\n此操作不可恢复！");
        confirm.getDialogPane().getStyleClass().add("dialog-pane");
        
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                String msg = questionService.deleteQuestion(selected.getId());
                showAlert(
                    msg.contains("成功") ? "✅ 成功" : "❌ 错误", 
                    msg, 
                    msg.contains("成功") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR
                );
                loadQuestions();
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
