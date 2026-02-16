package com.interview.view;

import com.interview.model.*;
import com.interview.service.EvaluationService;
import com.interview.service.InterviewRecordService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评委评分界面（JavaFX）
 */
public class EvaluationView extends BorderPane {
    
    private final EvaluationService evaluationService;
    private final InterviewRecordService recordService;
    
    private InterviewRecord currentRecord;
    private Map<EvaluationDimension, Slider> scoreSliders;
    private Map<EvaluationDimension, Label> scoreLabels;
    private TextArea commentsArea;
    private TextArea reasoningArea;
    private TextArea suggestionsArea;
    private Label totalScoreLabel;
    
    public EvaluationView(EvaluationService evaluationService, InterviewRecordService recordService) {
        this.evaluationService = evaluationService;
        this.recordService = recordService;
        this.scoreSliders = new HashMap<>();
        this.scoreLabels = new HashMap<>();
        
        setPadding(new Insets(10));
        setStyle("-fx-background-color: white;");
        
        initComponents();
    }
    
    private void initComponents() {
        // 顶部标题
        Label titleLabel = new Label("面试评分系统");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 20));
        titleLabel.setStyle("-fx-text-fill: #1565c0;");
        setTop(titleLabel);
        
        // 中心：评分面板
        ScrollPane scrollPane = new ScrollPane(createScoringPanel());
        scrollPane.setFitToWidth(true);
        setCenter(scrollPane);
        
        // 底部：操作按钮
        setBottom(createButtonPanel());
    }
    
    private VBox createScoringPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(10));
        
        // 总分显示
        HBox totalBox = new HBox(10);
        totalBox.setAlignment(Pos.CENTER_LEFT);
        totalScoreLabel = new Label("综合评分: 0.0");
        totalScoreLabel.setFont(Font.font(null, FontWeight.BOLD, 18));
        totalScoreLabel.setStyle("-fx-text-fill: #e65100;");
        totalBox.getChildren().add(totalScoreLabel);
        panel.getChildren().add(totalBox);
        
        // 按分类创建评分区域
        for (EvaluationDimension.Category category : evaluationService.getAllCategories()) {
            panel.getChildren().add(createCategorySection(category));
        }
        
        // 评语区域
        panel.getChildren().add(createCommentsSection());
        
        return panel;
    }
    
    private TitledPane createCategorySection(EvaluationDimension.Category category) {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        List<EvaluationDimension> dimensions = evaluationService.getDimensionsByCategory(category);
        
        for (EvaluationDimension dim : dimensions) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            
            // 维度名称
            Label nameLabel = new Label(dim.getDisplayName());
            nameLabel.setPrefWidth(200);
            nameLabel.setWrapText(true);
            
            // 滑块
            Slider slider = new Slider(0, 100, 70);
            slider.setPrefWidth(300);
            slider.setShowTickLabels(true);
            slider.setShowTickMarks(true);
            slider.setMajorTickUnit(20);
            
            // 分数显示
            Label scoreLabel = new Label("70");
            scoreLabel.setPrefWidth(40);
            scoreLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
            
            // 权重显示
            Label weightLabel = new Label("(" + dim.getDefaultWeight() + "%)");
            weightLabel.setPrefWidth(50);
            weightLabel.setStyle("-fx-text-fill: #666;");
            
            // 滑块值变化监听
            slider.valueProperty().addListener((obs, oldVal, newVal) -> {
                scoreLabel.setText(String.valueOf(newVal.intValue()));
                updateTotalScore();
            });
            
            scoreSliders.put(dim, slider);
            scoreLabels.put(dim, scoreLabel);
            
            row.getChildren().addAll(nameLabel, slider, scoreLabel, weightLabel);
            content.getChildren().add(row);
        }
        
        TitledPane titledPane = new TitledPane(category.getDisplayName(), content);
        titledPane.setExpanded(true);
        
        return titledPane;
    }
    
    private VBox createCommentsSection() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 5px;");
        
        Label titleLabel = new Label("评语与建议");
        titleLabel.setFont(Font.font(null, FontWeight.BOLD, 16));
        
        // 总体评价
        Label commentsLabel = new Label("总体评价:");
        commentsArea = new TextArea();
        commentsArea.setPrefRowCount(3);
        commentsArea.setPromptText("对面试者的总体评价...");
        
        // 评分理由
        Label reasoningLabel = new Label("评分理由:");
        reasoningArea = new TextArea();
        reasoningArea.setPrefRowCount(3);
        reasoningArea.setPromptText("说明评分的依据...");
        
        // 发展建议
        Label suggestionsLabel = new Label("发展建议:");
        suggestionsArea = new TextArea();
        suggestionsArea.setPrefRowCount(3);
        suggestionsArea.setPromptText("给面试者的发展建议...");
        
        panel.getChildren().addAll(
            titleLabel,
            commentsLabel, commentsArea,
            reasoningLabel, reasoningArea,
            suggestionsLabel, suggestionsArea
        );
        
        return panel;
    }
    
    private HBox createButtonPanel() {
        HBox panel = new HBox(15);
        panel.setPadding(new Insets(10, 0, 0, 0));
        panel.setAlignment(Pos.CENTER);
        
        Button calculateBtn = new Button("计算分数");
        calculateBtn.setOnAction(e -> updateTotalScore());
        
        Button submitBtn = new Button("提交评分");
        submitBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold;");
        submitBtn.setPrefWidth(120);
        submitBtn.setOnAction(e -> submitScore());
        
        Button aiAnalysisBtn = new Button("触发AI分析");
        aiAnalysisBtn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
        aiAnalysisBtn.setOnAction(e -> triggerAIAnalysis());
        
        panel.getChildren().addAll(calculateBtn, submitBtn, aiAnalysisBtn);
        
        return panel;
    }
    
    private void updateTotalScore() {
        double totalScore = 0;
        int totalWeight = 0;
        
        for (Map.Entry<EvaluationDimension, Slider> entry : scoreSliders.entrySet()) {
            int score = (int) entry.getValue().getValue();
            int weight = entry.getKey().getDefaultWeight();
            totalScore += score * weight;
            totalWeight += weight;
        }
        
        double weightedScore = totalWeight > 0 ? totalScore / totalWeight : 0;
        totalScoreLabel.setText(String.format("综合评分: %.1f", weightedScore));
    }
    
    private void submitScore() {
        if (currentRecord == null) {
            showAlert("错误", "请先选择面试记录", Alert.AlertType.ERROR);
            return;
        }
        
        EvaluationScore score = new EvaluationScore();
        score.setInterviewRecordId(currentRecord.getId());
        score.setCandidateUsername(currentRecord.getCandidateUsername());
        
        // 设置各维度分数
        for (Map.Entry<EvaluationDimension, Slider> entry : scoreSliders.entrySet()) {
            int dimScore = (int) entry.getValue().getValue();
            score.setDimensionScore(entry.getKey(), dimScore);
        }
        
        score.setComments(commentsArea.getText());
        score.setReasoning(reasoningArea.getText());
        score.setSuggestions(suggestionsArea.getText());
        
        String result = evaluationService.submitHumanScore(score);
        showAlert("提示", result, 
            result.contains("成功") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
    }
    
    private void triggerAIAnalysis() {
        if (currentRecord == null) {
            showAlert("错误", "请先选择面试记录", Alert.AlertType.ERROR);
            return;
        }
        
        // 获取面试相关的题目
        List<Question> questions = List.of(); // 从服务获取
        
        String result = evaluationService.triggerAIAnalysis(
            currentRecord.getId(), currentRecord, questions);
        showAlert("AI分析", result,
            result.contains("完成") ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
    }
    
    public void setCurrentRecord(InterviewRecord record) {
        this.currentRecord = record;
        // 清空表单
        for (Slider slider : scoreSliders.values()) {
            slider.setValue(70);
        }
        commentsArea.clear();
        reasoningArea.clear();
        suggestionsArea.clear();
        updateTotalScore();
    }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
