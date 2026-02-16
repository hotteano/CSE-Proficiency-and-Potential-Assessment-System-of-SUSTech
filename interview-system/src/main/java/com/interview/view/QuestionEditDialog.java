package com.interview.view;

import com.interview.model.Question;
import com.interview.model.Question.Difficulty;
import com.interview.model.Question.QuestionType;
import com.interview.service.QuestionService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * 题目编辑对话框（JavaFX）
 */
public class QuestionEditDialog extends Dialog<Boolean> {
    
    private final QuestionService questionService;
    private final Question question;
    private final boolean isEdit;
    
    private TextField titleField;
    private TextArea contentArea;
    private TextArea answerArea;
    private ComboBox<String> typeComboBox;
    private ComboBox<String> difficultyComboBox;
    private TextField categoryField;
    
    public QuestionEditDialog(QuestionService questionService, Question question) {
        this.questionService = questionService;
        this.question = question != null ? question : new Question();
        this.isEdit = question != null;
        
        setTitle(isEdit ? "编辑题目" : "新增题目");
        setHeaderText(isEdit ? "修改题目信息" : "创建新题目");
        
        initComponents();
        if (isEdit) {
            loadQuestionData();
        }
        
        // 按钮
        ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);
        
        // 处理保存
        Button saveButton = (Button) getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!saveQuestion()) {
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
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        // 标题
        grid.add(new Label("标题:*"), 0, 0);
        titleField = new TextField();
        titleField.setPromptText("请输入题目标题");
        grid.add(titleField, 1, 0);
        
        // 类型
        grid.add(new Label("类型:*"), 0, 1);
        typeComboBox = new ComboBox<>();
        for (QuestionType type : QuestionType.values()) {
            typeComboBox.getItems().add(type.getDisplayName());
        }
        typeComboBox.setValue(QuestionType.TECHNICAL.getDisplayName());
        grid.add(typeComboBox, 1, 1);
        
        // 难度
        grid.add(new Label("难度:*"), 0, 2);
        difficultyComboBox = new ComboBox<>();
        for (Difficulty diff : Difficulty.values()) {
            difficultyComboBox.getItems().add(diff.getDisplayName());
        }
        difficultyComboBox.setValue(Difficulty.MEDIUM.getDisplayName());
        grid.add(difficultyComboBox, 1, 2);
        
        // 分类
        grid.add(new Label("分类:"), 0, 3);
        categoryField = new TextField();
        categoryField.setPromptText("选填");
        grid.add(categoryField, 1, 3);
        
        // 题目内容
        grid.add(new Label("内容:*"), 0, 4);
        contentArea = new TextArea();
        contentArea.setPromptText("请输入题目内容");
        contentArea.setPrefRowCount(6);
        contentArea.setWrapText(true);
        grid.add(contentArea, 1, 4);
        
        // 参考答案
        grid.add(new Label("参考答案:"), 0, 5);
        answerArea = new TextArea();
        answerArea.setPromptText("选填");
        answerArea.setPrefRowCount(4);
        answerArea.setWrapText(true);
        grid.add(answerArea, 1, 5);
        
        GridPane.setHgrow(titleField, Priority.ALWAYS);
        GridPane.setHgrow(contentArea, Priority.ALWAYS);
        GridPane.setHgrow(answerArea, Priority.ALWAYS);
        
        content.getChildren().add(grid);
        getDialogPane().setContent(content);
        getDialogPane().setPrefWidth(600);
    }
    
    private void loadQuestionData() {
        titleField.setText(question.getTitle());
        contentArea.setText(question.getContent());
        answerArea.setText(question.getAnswer());
        categoryField.setText(question.getCategory());
        
        if (question.getType() != null) {
            typeComboBox.setValue(question.getType().getDisplayName());
        }
        if (question.getDifficulty() != null) {
            difficultyComboBox.setValue(question.getDifficulty().getDisplayName());
        }
    }
    
    private boolean saveQuestion() {
        // 验证输入
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();
        
        if (title.isEmpty()) {
            showError("请输入题目标题");
            return false;
        }
        
        if (content.isEmpty()) {
            showError("请输入题目内容");
            return false;
        }
        
        // 设置数据
        question.setTitle(title);
        question.setContent(content);
        question.setAnswer(answerArea.getText().trim());
        question.setCategory(categoryField.getText().trim());
        
        // 设置类型和难度
        int typeIndex = typeComboBox.getSelectionModel().getSelectedIndex();
        if (typeIndex >= 0) {
            question.setType(QuestionType.values()[typeIndex]);
        }
        
        int diffIndex = difficultyComboBox.getSelectionModel().getSelectedIndex();
        if (diffIndex >= 0) {
            question.setDifficulty(Difficulty.values()[diffIndex]);
        }
        
        // 保存
        String result;
        if (isEdit) {
            result = questionService.updateQuestion(question);
        } else {
            result = questionService.createQuestion(question);
        }
        
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
}
