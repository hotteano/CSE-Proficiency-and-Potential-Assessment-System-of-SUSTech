package com.interview.view;

import com.interview.model.Question;
import com.interview.service.QuestionService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

/**
 * 题目管理视图（JavaFX）
 */
public class QuestionManageView extends BorderPane {
    
    private final QuestionService questionService;
    
    private TableView<Question> questionTable;
    
    public QuestionManageView(QuestionService questionService) {
        this.questionService = questionService;
        
        setPadding(new Insets(10));
        setStyle("-fx-background-color: white;");
        
        // 顶部按钮栏
        setTop(createButtonPanel());
        
        // 中心表格
        setCenter(createTablePanel());
        
        // 加载数据
        loadQuestions();
    }
    
    private HBox createButtonPanel() {
        HBox panel = new HBox(10);
        panel.setPadding(new Insets(0, 0, 10, 0));
        panel.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("题目管理");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
        
        Button addBtn = new Button("新增题目");
        addBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        addBtn.setOnAction(e -> showAddDialog());
        
        Button editBtn = new Button("编辑题目");
        editBtn.setStyle("-fx-background-color: #4682b4; -fx-text-fill: white;");
        editBtn.setOnAction(e -> showEditDialog());
        
        Button deleteBtn = new Button("删除题目");
        deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> deleteQuestion());
        
        Button refreshBtn = new Button("刷新");
        refreshBtn.setOnAction(e -> loadQuestions());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        panel.getChildren().addAll(titleLabel, spacer, addBtn, editBtn, deleteBtn, refreshBtn);
        
        return panel;
    }
    
    private VBox createTablePanel() {
        VBox panel = new VBox(10);
        
        questionTable = new TableView<>();
        
        TableColumn<Question, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(String.valueOf(cell.getValue().getId())));
        idCol.setPrefWidth(50);
        
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
        diffCol.setPrefWidth(80);
        
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
        dialog.showAndWait().ifPresent(result -> {
            if (result) {
                loadQuestions();
            }
        });
    }
    
    private void showEditDialog() {
        Question selected = questionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("提示", "请先选择要编辑的题目", Alert.AlertType.WARNING);
            return;
        }
        
        QuestionEditDialog dialog = new QuestionEditDialog(questionService, selected);
        dialog.showAndWait().ifPresent(result -> {
            if (result) {
                loadQuestions();
            }
        });
    }
    
    private void deleteQuestion() {
        Question selected = questionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("提示", "请先选择要删除的题目", Alert.AlertType.WARNING);
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("确认删除");
        confirm.setHeaderText("删除题目");
        confirm.setContentText("确定要删除题目 [" + selected.getTitle() + "] 吗？\n此操作不可恢复！");
        
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                String msg = questionService.deleteQuestion(selected.getId());
                showAlert("提示", msg, 
                    msg.contains("成功") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
                loadQuestions();
            }
        });
    }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
