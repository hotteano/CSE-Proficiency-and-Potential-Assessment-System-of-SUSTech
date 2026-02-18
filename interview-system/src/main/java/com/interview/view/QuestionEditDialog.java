package com.interview.view;

import com.interview.model.Question;
import com.interview.model.Question.QuestionLevel;
import com.interview.model.Question.QuestionType;
import com.interview.model.Question.SpecializationType;
import com.interview.service.QuestionService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * 题目编辑对话框（JavaFX）
 * 应用新 CSS 设计
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
    private Label messageLabel;
    
    public QuestionEditDialog(QuestionService questionService, Question question) {
        this.questionService = questionService;
        this.question = question != null ? question : new Question();
        this.isEdit = question != null;
        
        setTitle(isEdit ? "✏️ 编辑题目" : "➕ 新增题目");
        setHeaderText(isEdit ? "修改题目信息" : "创建新题目");
        
        // 应用对话框样式
        getDialogPane().getStyleClass().add("dialog-pane");
        
        initComponents();
        if (isEdit) {
            loadQuestionData();
        }
        
        // 按钮
        ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);
        
        // 样式化按钮
        Button saveButton = (Button) getDialogPane().lookupButton(saveButtonType);
        saveButton.getStyleClass().addAll("button", "button-success");
        
        Button cancelButton = (Button) getDialogPane().lookupButton(cancelButtonType);
        cancelButton.getStyleClass().addAll("button", "button-secondary");
        
        // 处理保存
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
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));
        content.setAlignment(Pos.CENTER);
        
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setAlignment(Pos.CENTER);
        
        int row = 0;
        
        // 标题
        grid.add(createFormLabel("标题", true), 0, row);
        titleField = new TextField();
        titleField.setPromptText("请输入题目标题");
        titleField.getStyleClass().add("text-field");
        GridPane.setHgrow(titleField, Priority.ALWAYS);
        grid.add(titleField, 1, row++);
        
        // 类型
        grid.add(createFormLabel("类型", true), 0, row);
        typeComboBox = new ComboBox<>();
        for (QuestionType type : QuestionType.values()) {
            typeComboBox.getItems().add(type.getDisplayName());
        }
        typeComboBox.setValue(QuestionType.TECHNICAL.getDisplayName());
        typeComboBox.getStyleClass().add("combo-box");
        grid.add(typeComboBox, 1, row++);
        
        // 等级
        grid.add(createFormLabel("等级", true), 0, row);
        levelComboBox = new ComboBox<>();
        for (QuestionLevel level : QuestionLevel.getBasicLevels()) {
            levelComboBox.getItems().add(level.getDisplayName());
        }
        levelComboBox.getItems().add(QuestionLevel.SPECIALIZATION_THREE.getDisplayName());
        levelComboBox.setValue(QuestionLevel.BASIC.getDisplayName());
        levelComboBox.getStyleClass().add("combo-box");
        levelComboBox.setOnAction(e -> updateSpecializationVisibility());
        grid.add(levelComboBox, 1, row++);
        
        // 专精类型（仅专精三等需要）
        specializationLabel = createFormLabel("专精类型", true);
        grid.add(specializationLabel, 0, row);
        specializationComboBox = new ComboBox<>();
        for (SpecializationType spec : SpecializationType.getValidTypes()) {
            specializationComboBox.getItems().add(spec.getDisplayName());
        }
        specializationComboBox.setValue(SpecializationType.ALGORITHM.getDisplayName());
        specializationComboBox.getStyleClass().add("combo-box");
        grid.add(specializationComboBox, 1, row++);
        
        // 默认隐藏专精类型
        updateSpecializationVisibility();
        
        // 分类
        grid.add(createFormLabel("分类", false), 0, row);
        categoryField = new TextField();
        categoryField.setPromptText("选填，如：Java、数据库、网络等");
        categoryField.getStyleClass().add("text-field");
        grid.add(categoryField, 1, row++);
        
        // 题目内容
        grid.add(createFormLabel("内容", true), 0, row);
        contentArea = new TextArea();
        contentArea.setPromptText("请输入题目内容");
        contentArea.setPrefRowCount(6);
        contentArea.setWrapText(true);
        contentArea.getStyleClass().add("text-area");
        GridPane.setHgrow(contentArea, Priority.ALWAYS);
        grid.add(contentArea, 1, row++);
        
        // 参考答案
        grid.add(createFormLabel("参考答案", false), 0, row);
        answerArea = new TextArea();
        answerArea.setPromptText("选填");
        answerArea.setPrefRowCount(4);
        answerArea.setWrapText(true);
        answerArea.getStyleClass().add("text-area");
        GridPane.setHgrow(answerArea, Priority.ALWAYS);
        grid.add(answerArea, 1, row++);
        
        // 消息标签
        messageLabel = new Label();
        messageLabel.getStyleClass().add("label-danger");
        messageLabel.setVisible(false);
        grid.add(messageLabel, 1, row++);
        
        // 等级说明卡片
        VBox levelCard = new VBox(8);
        levelCard.getStyleClass().addAll("card-flat", "alert-info");
        levelCard.setPadding(new Insets(12));
        
        Label levelTitle = new Label("📊 等级说明");
        levelTitle.getStyleClass().add("caption-label");
        
        Label levelInfoLabel = new Label(
            "• 初级：基本程序编写能力\n" +
            "• 中级：独立完成中等复杂度任务\n" +
            "• 高级：复杂算法和系统设计\n" +
            "• 专精三等：特定领域深度能力（算法/系统/商业/科研）"
        );
        levelInfoLabel.getStyleClass().add("caption-label");
        levelInfoLabel.setStyle("-fx-line-spacing: 3px;");
        
        levelCard.getChildren().addAll(levelTitle, levelInfoLabel);
        grid.add(levelCard, 1, row);
        
        content.getChildren().add(grid);
        getDialogPane().setContent(content);
        getDialogPane().setPrefWidth(700);
    }
    
    private Label createFormLabel(String text, boolean required) {
        Label label = new Label(text + (required ? " *" : ""));
        label.getStyleClass().add("text-secondary");
        return label;
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
        clearErrors();
        
        // 验证输入
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();
        
        if (title.isEmpty()) {
            showError("请输入题目标题", titleField);
            return false;
        }
        
        if (content.isEmpty()) {
            showError("请输入题目内容", contentArea);
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
            question.setLevel(QuestionLevel.SPECIALIZATION_THREE);
            
            int specIndex = specializationComboBox.getSelectionModel().getSelectedIndex();
            if (specIndex < 0) {
                showError("请选择专精类型", specializationComboBox);
                return false;
            }
            question.setSpecialization(SpecializationType.getValidTypes()[specIndex]);
        } else {
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
        titleField.getStyleClass().remove("field-error");
        contentArea.getStyleClass().remove("field-error");
        specializationComboBox.getStyleClass().remove("field-error");
    }
}
