package com.interview.view;

import com.interview.model.Question;
import com.interview.model.Question.QuestionLevel;
import com.interview.model.Question.QuestionType;
import com.interview.model.Question.SpecializationType;
import com.interview.service.QuestionService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * 题目编辑对话框（JavaFX）
 * 
 * 支持等级体系：
 * - 基础等级：初级、中级、高级
 * - 专精三等：配合专精类型（算法、系统设计、商业、科研）
 */
public class QuestionEditDialog extends Dialog<Boolean> {
    
    private final QuestionService questionService;
    private final Question question;
    private final boolean isEdit;
    
    private TextField titleField;
    private TextArea contentArea;
    private TextArea answerArea;
    private ComboBox<String> typeComboBox;
    private ComboBox<String> levelComboBox;
    private ComboBox<String> specializationComboBox;
    private TextField categoryField;
    private Label specializationLabel;
    
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
        
        // 等级
        grid.add(new Label("等级:*"), 0, 2);
        levelComboBox = new ComboBox<>();
        // 添加基础等级
        for (QuestionLevel level : QuestionLevel.getBasicLevels()) {
            levelComboBox.getItems().add(level.getDisplayName());
        }
        // 添加专精三等
        levelComboBox.getItems().add(QuestionLevel.SPECIALIZATION_THREE.getDisplayName());
        
        levelComboBox.setValue(QuestionLevel.BASIC.getDisplayName());
        
        // 等级选择变化时更新专精类型可见性
        levelComboBox.setOnAction(e -> updateSpecializationVisibility());
        
        grid.add(levelComboBox, 1, 2);
        
        // 专精类型（仅专精三等需要）
        specializationLabel = new Label("专精类型:*");
        grid.add(specializationLabel, 0, 3);
        specializationComboBox = new ComboBox<>();
        for (SpecializationType spec : SpecializationType.getValidTypes()) {
            specializationComboBox.getItems().add(spec.getDisplayName());
        }
        specializationComboBox.setValue(SpecializationType.ALGORITHM.getDisplayName());
        grid.add(specializationComboBox, 1, 3);
        
        // 默认隐藏专精类型
        updateSpecializationVisibility();
        
        // 分类
        grid.add(new Label("分类:"), 0, 4);
        categoryField = new TextField();
        categoryField.setPromptText("选填，如：Java、数据库、网络等");
        grid.add(categoryField, 1, 4);
        
        // 题目内容
        grid.add(new Label("内容:*"), 0, 5);
        contentArea = new TextArea();
        contentArea.setPromptText("请输入题目内容");
        contentArea.setPrefRowCount(6);
        contentArea.setWrapText(true);
        grid.add(contentArea, 1, 5);
        
        // 参考答案
        grid.add(new Label("参考答案:"), 0, 6);
        answerArea = new TextArea();
        answerArea.setPromptText("选填");
        answerArea.setPrefRowCount(4);
        answerArea.setWrapText(true);
        grid.add(answerArea, 1, 6);
        
        // 等级说明
        Label levelInfoLabel = new Label("等级说明：\n" +
            "• 初级：基本程序编写能力，简单编程任务\n" +
            "• 中级：独立完成中等复杂度任务，具备故障排除能力\n" +
            "• 高级：复杂算法和系统设计，科研能力\n" +
            "• 专精三等：特定领域的深度能力（算法/系统/商业/科研）");
        levelInfoLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
        levelInfoLabel.setWrapText(true);
        grid.add(levelInfoLabel, 1, 7);
        
        GridPane.setHgrow(titleField, Priority.ALWAYS);
        GridPane.setHgrow(contentArea, Priority.ALWAYS);
        GridPane.setHgrow(answerArea, Priority.ALWAYS);
        
        content.getChildren().add(grid);
        getDialogPane().setContent(content);
        getDialogPane().setPrefWidth(650);
    }
    
    private void updateSpecializationVisibility() {
        String selectedLevel = levelComboBox.getValue();
        boolean isSpecialization = QuestionLevel.SPECIALIZATION_THREE.getDisplayName().equals(selectedLevel);
        specializationLabel.setVisible(isSpecialization);
        specializationLabel.setManaged(isSpecialization);
        specializationComboBox.setVisible(isSpecialization);
        specializationComboBox.setManaged(isSpecialization);
    }
    
    private void loadQuestionData() {
        titleField.setText(question.getTitle());
        contentArea.setText(question.getContent());
        answerArea.setText(question.getAnswer());
        categoryField.setText(question.getCategory());
        
        if (question.getType() != null) {
            typeComboBox.setValue(question.getType().getDisplayName());
        }
        if (question.getLevel() != null) {
            levelComboBox.setValue(question.getLevel().getDisplayName());
        }
        if (question.getSpecialization() != null && question.getSpecialization() != SpecializationType.NONE) {
            specializationComboBox.setValue(question.getSpecialization().getDisplayName());
        }
        
        updateSpecializationVisibility();
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
        
        // 设置类型
        int typeIndex = typeComboBox.getSelectionModel().getSelectedIndex();
        if (typeIndex >= 0) {
            question.setType(QuestionType.values()[typeIndex]);
        }
        
        // 设置等级
        String selectedLevel = levelComboBox.getValue();
        if (QuestionLevel.SPECIALIZATION_THREE.getDisplayName().equals(selectedLevel)) {
            // 专精三等
            question.setLevel(QuestionLevel.SPECIALIZATION_THREE);
            
            // 验证并设置专精类型
            int specIndex = specializationComboBox.getSelectionModel().getSelectedIndex();
            if (specIndex < 0) {
                showError("请选择专精类型");
                return false;
            }
            question.setSpecialization(SpecializationType.getValidTypes()[specIndex]);
        } else {
            // 基础等级
            for (QuestionLevel level : QuestionLevel.getBasicLevels()) {
                if (level.getDisplayName().equals(selectedLevel)) {
                    question.setLevel(level);
                    break;
                }
            }
            question.setSpecialization(SpecializationType.NONE);
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
