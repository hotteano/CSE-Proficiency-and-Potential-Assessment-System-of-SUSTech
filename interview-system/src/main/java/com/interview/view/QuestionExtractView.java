package com.interview.view;

import com.interview.model.Question;
import com.interview.model.Question.QuestionLevel;
import com.interview.model.Question.QuestionType;
import com.interview.service.QuestionService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.util.List;

/**
 * 题目抽取视图（JavaFX）
 */
public class QuestionExtractView extends BorderPane {
    
    private final QuestionService questionService;
    
    private Spinner<Integer> countSpinner;
    private ComboBox<String> typeComboBox;
    private ComboBox<String> difficultyComboBox;
    private ComboBox<String> categoryComboBox;
    private TextArea resultArea;
    
    public QuestionExtractView(QuestionService questionService) {
        this.questionService = questionService;
        
        setPadding(new Insets(10));
        setStyle("-fx-background-color: white;");
        
        // 左侧设置面板
        setLeft(createSettingsPanel());
        
        // 中心结果面板
        setCenter(createResultPanel());
    }
    
    private VBox createSettingsPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(250);
        panel.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 5px;");
        
        Label titleLabel = new Label("抽取设置");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        
        // 抽取数量
        Label countLabel = new Label("抽取数量:");
        countSpinner = new Spinner<>(1, 100, 5);
        countSpinner.setPrefWidth(200);
        
        // 题目类型
        Label typeLabel = new Label("题目类型:");
        typeComboBox = new ComboBox<>();
        typeComboBox.getItems().add("全部类型");
        for (QuestionType type : QuestionType.values()) {
            typeComboBox.getItems().add(type.getDisplayName());
        }
        typeComboBox.setValue("全部类型");
        typeComboBox.setPrefWidth(200);
        
        // 难度
        Label diffLabel = new Label("难度等级:");
        difficultyComboBox = new ComboBox<>();
        difficultyComboBox.getItems().add("全部");
        difficultyComboBox.getItems().add(QuestionLevel.BASIC.getDisplayName());
        difficultyComboBox.getItems().add(QuestionLevel.INTERMEDIATE.getDisplayName());
        difficultyComboBox.getItems().add(QuestionLevel.ADVANCED.getDisplayName());
        difficultyComboBox.getItems().add(QuestionLevel.SPECIALIZATION_THREE.getDisplayName());
        difficultyComboBox.setValue("全部");
        difficultyComboBox.setPrefWidth(200);
        
        // 分类
        Label catLabel = new Label("分类筛选:");
        categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().add("全部分类");
        categoryComboBox.setValue("全部分类");
        categoryComboBox.setPrefWidth(200);
        loadCategories();
        
        // 抽取按钮
        Button extractBtn = new Button("开始抽取");
        extractBtn.setPrefWidth(200);
        extractBtn.setPrefHeight(40);
        extractBtn.setStyle("-fx-background-color: #4682b4; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        extractBtn.setOnAction(e -> extractQuestions());
        
        panel.getChildren().addAll(
            titleLabel,
            new Separator(),
            countLabel, countSpinner,
            typeLabel, typeComboBox,
            diffLabel, difficultyComboBox,
            catLabel, categoryComboBox,
            new Separator(),
            extractBtn
        );
        
        return panel;
    }
    
    private VBox createResultPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(0, 0, 0, 10));
        
        Label titleLabel = new Label("抽取结果");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        
        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);
        resultArea.setText("请点击「开始抽取」按钮抽取题目\n\n抽取结果将显示在这里...");
        VBox.setVgrow(resultArea, Priority.ALWAYS);
        
        // 导出按钮
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button exportBtn = new Button("导出结果");
        exportBtn.setOnAction(e -> exportResult());
        
        buttonBox.getChildren().add(exportBtn);
        
        panel.getChildren().addAll(titleLabel, resultArea, buttonBox);
        
        return panel;
    }
    
    private void loadCategories() {
        List<String> categories = questionService.getAllCategories();
        categoryComboBox.getItems().addAll(categories);
    }
    
    private void extractQuestions() {
        int count = countSpinner.getValue();
        QuestionType type = typeComboBox.getSelectionModel().getSelectedIndex() > 0 ? 
            QuestionType.values()[typeComboBox.getSelectionModel().getSelectedIndex() - 1] : null;
        QuestionLevel questionLevel = difficultyComboBox.getSelectionModel().getSelectedIndex() > 0 ? 
            QuestionLevel.values()[difficultyComboBox.getSelectionModel().getSelectedIndex() - 1] : null;
        String category = categoryComboBox.getSelectionModel().getSelectedIndex() > 0 ? 
            categoryComboBox.getValue() : null;
        
        List<Question> questions = questionService.extractQuestions(count, type, questionLevel, category);
        
        if (questions.isEmpty()) {
            resultArea.setText("未能抽取到符合条件的题目，请调整筛选条件后重试。");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("================================\n");
            sb.append("       面试题目抽取结果\n");
            sb.append("================================\n\n");
            sb.append("抽取数量: ").append(questions.size()).append(" 题\n");
            sb.append("抽取时间: ").append(java.time.LocalDateTime.now()).append("\n\n");
            
            if (type != null) {
                sb.append("题目类型: ").append(type.getDisplayName()).append("\n");
            }
            if (questionLevel != null) {
                sb.append("难度等级: ").append(questionLevel.getDisplayName()).append("\n");
            }
            if (category != null) {
                sb.append("分类筛选: ").append(category).append("\n");
            }
            sb.append("\n================================\n\n");
            
            int i = 1;
            for (Question q : questions) {
                sb.append("【题目 ").append(i++).append("】\n");
                sb.append("标题: ").append(q.getTitle()).append("\n");
                sb.append("类型: ").append(q.getTypeDisplayName()).append("\n");
                sb.append("等级: ").append(q.getLevelDisplayName()).append("\n\n");
                sb.append("内容:\n").append(q.getContent()).append("\n\n");
                if (q.getAnswer() != null && !q.getAnswer().isEmpty()) {
                    sb.append("参考答案:\n").append(q.getAnswer()).append("\n");
                }
                sb.append("\n--------------------------------\n\n");
            }
            
            resultArea.setText(sb.toString());
            resultArea.positionCaret(0);
        }
    }
    
    private void exportResult() {
        String text = resultArea.getText();
        if (text.isEmpty() || text.startsWith("请点击")) {
            showAlert("提示", "请先抽取题目", Alert.AlertType.WARNING);
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出抽取结果");
        fileChooser.setInitialFileName("面试题目_" + LocalDate.now() + ".txt");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("文本文件", "*.txt")
        );
        
        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(text);
                showAlert("成功", "导出成功！", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("错误", "导出失败: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
