package com.interview.view;

import com.interview.llm.LLMManager;
import com.interview.model.*;
import com.interview.service.AuthService;
import com.interview.service.EvaluationService;
import com.interview.service.InterviewRecordService;
import com.interview.service.LLMConfigService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.HashMap;
import java.util.Map;

/**
 * 评测界面（JavaFX）
 * 提供多维度评分、AI分析和提交功能
 */
public class EvaluationView extends BorderPane {
    
    private final EvaluationService evaluationService;
    private final InterviewRecordService recordService;
    private final LLMManager llmManager;
    
    // 左侧面板组件
    private TableView<InterviewRecord> recordTable;
    private Label selectedRecordLabel;
    private TextArea aiResultArea;
    private Button aiAnalyzeBtn;
    private Button useAiResultBtn;
    
    // 右侧评分面板组件
    private Map<EvaluationDimension, Slider> dimensionSliders;
    private Map<EvaluationDimension, Label> dimensionScoreLabels;
    private TextArea commentsArea;
    private TextArea reasoningArea;
    private Label overallScoreLabel;
    private Button submitBtn;
    
    private InterviewRecord selectedRecord;
    private InterviewAnalysisResult currentAiResult;
    
    public EvaluationView(AuthService authService) {
        this.evaluationService = new EvaluationService(authService);
        this.recordService = new InterviewRecordService(authService);
        this.llmManager = new LLMManager(new LLMConfigService());
        
        setPadding(new Insets(10));
        setStyle("-fx-background-color: #f5f5f5;");
        
        initComponents();
        loadInterviewRecords();
    }
    
    private void initComponents() {
        // 左侧：面试记录选择面板
        setLeft(createRecordSelectionPanel());
        
        // 右侧：评分面板（带滚动）
        ScrollPane scrollPane = new ScrollPane(createScoringPanel());
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setCenter(scrollPane);
    }
    
    /**
     * 创建面试记录选择面板
     */
    private VBox createRecordSelectionPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(320);
        panel.setStyle("-fx-background-color: white; -fx-border-radius: 5px;");
        
        // 标题
        Label titleLabel = new Label("面试记录选择");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        
        // 刷新按钮
        Button refreshBtn = new Button("刷新列表");
        refreshBtn.setOnAction(e -> loadInterviewRecords());
        
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().addAll(titleLabel, refreshBtn);
        
        // 面试记录表格
        recordTable = new TableView<>();
        
        TableColumn<InterviewRecord, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(String.valueOf(cell.getValue().getId())));
        idCol.setPrefWidth(40);
        
        TableColumn<InterviewRecord, String> candidateCol = new TableColumn<>("考生");
        candidateCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getCandidateUsername()));
        candidateCol.setPrefWidth(80);
        
        TableColumn<InterviewRecord, String> statusCol = new TableColumn<>("状态");
        statusCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getStatusDisplayName()));
        statusCol.setPrefWidth(70);
        
        TableColumn<InterviewRecord, String> timeCol = new TableColumn<>("时间");
        timeCol.setCellValueFactory(cell -> {
            var time = cell.getValue().getInterviewTime();
            return new SimpleStringProperty(time != null ? 
                time.toLocalDate().toString() : "-");
        });
        timeCol.setPrefWidth(80);
        
        recordTable.getColumns().addAll(idCol, candidateCol, statusCol, timeCol);
        recordTable.setPrefHeight(250);
        
        // 选择事件
        recordTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> onRecordSelected(newVal));
        
        // 已选记录信息
        selectedRecordLabel = new Label("请从上方选择一条面试记录\n开始评测");
        selectedRecordLabel.setWrapText(true);
        selectedRecordLabel.setStyle("-fx-text-fill: #666;");
        
        // AI分析区域
        TitledPane aiPane = createAIAnalysisPanel();
        
        panel.getChildren().addAll(headerBox, recordTable, selectedRecordLabel, aiPane);
        VBox.setVgrow(recordTable, Priority.ALWAYS);
        
        return panel;
    }
    
    /**
     * 创建AI分析面板
     */
    private TitledPane createAIAnalysisPanel() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(5));
        
        // AI分析按钮
        aiAnalyzeBtn = new Button("🤖 AI分析面试（使用预设文本测试）");
        aiAnalyzeBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold;");
        aiAnalyzeBtn.setPrefWidth(300);
        aiAnalyzeBtn.setDisable(true);
        aiAnalyzeBtn.setOnAction(e -> performAIAnalysis());
        
        // AI结果展示区域
        aiResultArea = new TextArea();
        aiResultArea.setEditable(false);
        aiResultArea.setPrefHeight(200);
        aiResultArea.setWrapText(true);
        aiResultArea.setPromptText("AI分析结果将显示在这里...");
        
        // 应用AI结果按钮
        useAiResultBtn = new Button("📋 应用AI分析结果到评分");
        useAiResultBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        useAiResultBtn.setPrefWidth(300);
        useAiResultBtn.setDisable(true);
        useAiResultBtn.setOnAction(e -> applyAIResults());
        
        content.getChildren().addAll(aiAnalyzeBtn, aiResultArea, useAiResultBtn);
        
        TitledPane pane = new TitledPane("AI 智能分析", content);
        pane.setExpanded(false);
        return pane;
    }
    
    /**
     * 创建评分面板
     */
    private VBox createScoringPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: white; -fx-border-radius: 5px;");
        
        // 总体评分
        overallScoreLabel = new Label("综合评分: 0.0");
        overallScoreLabel.setFont(Font.font(null, FontWeight.BOLD, 24));
        overallScoreLabel.setStyle("-fx-text-fill: #2196f3;");
        
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().addAll(overallScoreLabel);
        
        // 初始化维度滑块
        dimensionSliders = new HashMap<>();
        dimensionScoreLabels = new HashMap<>();
        
        // 分类创建维度面板
        for (EvaluationDimension.Category category : EvaluationDimension.Category.values()) {
            panel.getChildren().add(createCategoryPanel(category));
        }
        
        // 评语和理由
        commentsArea = new TextArea();
        commentsArea.setPromptText("请输入综合评价评语...");
        commentsArea.setPrefHeight(100);
        
        reasoningArea = new TextArea();
        reasoningArea.setPromptText("请输入评分理由...");
        reasoningArea.setPrefHeight(100);
        
        TitledPane commentsPane = new TitledPane("综合评价评语", commentsArea);
        TitledPane reasoningPane = new TitledPane("评分理由", reasoningArea);
        
        // 提交按钮
        submitBtn = new Button("提交评分");
        submitBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        submitBtn.setPrefWidth(200);
        submitBtn.setPrefHeight(50);
        submitBtn.setDisable(true);
        submitBtn.setOnAction(e -> submitEvaluation());
        
        HBox submitBox = new HBox(submitBtn);
        submitBox.setAlignment(Pos.CENTER);
        submitBox.setPadding(new Insets(20));
        
        panel.getChildren().addAll(headerBox, commentsPane, reasoningPane, submitBox);
        
        // 更新总体分数
        updateOverallScore();
        
        return panel;
    }
    
    /**
     * 创建分类面板
     */
    private TitledPane createCategoryPanel(EvaluationDimension.Category category) {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        for (EvaluationDimension dim : EvaluationDimension.values()) {
            if (dim.getCategory() == category) {
                content.getChildren().add(createDimensionRow(dim));
            }
        }
        
        TitledPane pane = new TitledPane(category.getDisplayName(), content);
        return pane;
    }
    
    /**
     * 创建维度行
     */
    private HBox createDimensionRow(EvaluationDimension dimension) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        
        // 维度名称
        Label nameLabel = new Label(dimension.getDisplayName());
        nameLabel.setPrefWidth(120);
        nameLabel.setFont(Font.font(null, FontWeight.BOLD, 12));
        
        // 权重显示
        Label weightLabel = new Label(String.format("(权重%.0f%%)", dimension.getWeight() * 100));
        weightLabel.setPrefWidth(70);
        weightLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 10px;");
        
        // 滑块
        Slider slider = new Slider(0, 100, 0);
        slider.setBlockIncrement(1);
        slider.setMajorTickUnit(10);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setPrefWidth(300);
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            dimensionScoreLabels.get(dimension).setText(String.format("%.0f", newVal.doubleValue()));
            updateOverallScore();
        });
        dimensionSliders.put(dimension, slider);
        
        // 分数显示
        Label scoreLabel = new Label("0");
        scoreLabel.setPrefWidth(40);
        scoreLabel.setAlignment(Pos.CENTER);
        scoreLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
        dimensionScoreLabels.put(dimension, scoreLabel);
        
        row.getChildren().addAll(nameLabel, weightLabel, slider, scoreLabel);
        
        return row;
    }
    
    /**
     * 更新总体评分
     */
    private void updateOverallScore() {
        double total = 0;
        for (Map.Entry<EvaluationDimension, Slider> entry : dimensionSliders.entrySet()) {
            total += entry.getValue().getValue() * entry.getKey().getWeight();
        }
        overallScoreLabel.setText(String.format("综合评分: %.1f", total));
    }
    
    /**
     * 加载面试记录
     */
    private void loadInterviewRecords() {
        var records = recordService.getAllRecords();
        recordTable.getItems().clear();
        recordTable.getItems().addAll(records);
    }
    
    /**
     * 选择面试记录回调
     */
    private void onRecordSelected(InterviewRecord record) {
        this.selectedRecord = record;
        if (record == null) {
            aiAnalyzeBtn.setDisable(true);
            submitBtn.setDisable(true);
            return;
        }
        
        // 更新已选记录信息
        String info = String.format("已选择记录 #%d\n考生: %s\n状态: %s\n面试时间: %s",
            record.getId(),
            record.getCandidateUsername(),
            record.getStatusDisplayName(),
            record.getInterviewTime() != null ? record.getInterviewTime() : "未设置"
        );
        selectedRecordLabel.setText(info);
        
        aiAnalyzeBtn.setDisable(false);
        submitBtn.setDisable(false);
        
        // 清空之前的AI结果
        aiResultArea.clear();
        currentAiResult = null;
        useAiResultBtn.setDisable(true);
        
        // 尝试加载已有评分
        loadExistingScore(record.getId());
    }
    
    /**
     * 执行AI分析
     */
    private void performAIAnalysis() {
        if (selectedRecord == null) {
            showAlert("提示", "请先选择面试记录", Alert.AlertType.WARNING);
            return;
        }
        
        aiAnalyzeBtn.setDisable(true);
        aiAnalyzeBtn.setText("🔄 AI分析中...");
        aiResultArea.setText("正在进行AI分析，请稍候...\n\n" +
            "当前使用预定义面试文本进行测试。\n" +
            "后续将使用语音识别结果进行真实分析。");
        
        // 在后台线程执行AI分析
        new Thread(() -> {
            try {
                // 使用预定义文本测试API调用
                String testText = llmManager.getTestInterviewText();
                currentAiResult = llmManager.analyzeInterviewText(testText);
                
                javafx.application.Platform.runLater(() -> {
                    if (currentAiResult != null) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("✅ AI分析完成\n\n");
                        sb.append("各维度评分:\n");
                        for (EvaluationDimension dim : EvaluationDimension.values()) {
                            Double score = currentAiResult.getDimensionScore(dim);
                            if (score != null) {
                                sb.append(String.format("  %s: %.1f分\n", dim.getDisplayName(), score));
                            }
                        }
                        sb.append(String.format("\n总分: %.1f分\n", currentAiResult.calculateTotalScore()));
                        sb.append("\n整体评价:\n").append(currentAiResult.getOverallComment());
                        sb.append("\n\n优势: ").append(currentAiResult.getStrengths());
                        sb.append("\n待提升: ").append(currentAiResult.getWeaknesses());
                        sb.append("\n\n建议: ").append(currentAiResult.getSuggestions());
                        
                        aiResultArea.setText(sb.toString());
                        useAiResultBtn.setDisable(false);
                    } else {
                        aiResultArea.setText("❌ AI分析失败\n\n" +
                            "可能原因:\n" +
                            "1. API配置错误，请检查LLM配置\n" +
                            "2. 网络连接问题\n" +
                            "3. API密钥无效或余额不足\n\n" +
                            "请在系统设置中配置正确的DeepSeek API参数。");
                    }
                    aiAnalyzeBtn.setDisable(false);
                    aiAnalyzeBtn.setText("🤖 AI分析面试（使用预设文本测试）");
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    aiResultArea.setText("❌ AI分析异常: " + e.getMessage());
                    aiAnalyzeBtn.setDisable(false);
                    aiAnalyzeBtn.setText("🤖 AI分析面试（使用预设文本测试）");
                });
            }
        }).start();
    }
    
    /**
     * 应用AI分析结果到评分
     */
    private void applyAIResults() {
        if (currentAiResult == null) {
            return;
        }
        
        // 将AI评分应用到各个滑块
        for (EvaluationDimension dim : EvaluationDimension.values()) {
            Double score = currentAiResult.getDimensionScore(dim);
            if (score != null && dimensionSliders.containsKey(dim)) {
                dimensionSliders.get(dim).setValue(score);
            }
        }
        
        // 设置评语
        commentsArea.setText(currentAiResult.getOverallComment());
        
        // 构建评分理由
        StringBuilder reasoning = new StringBuilder();
        reasoning.append("AI分析结果:\n");
        reasoning.append("优势: ").append(String.join(", ", currentAiResult.getStrengths())).append("\n");
        reasoning.append("待提升: ").append(String.join(", ", currentAiResult.getWeaknesses())).append("\n\n");
        reasoning.append("发展建议:\n");
        for (String suggestion : currentAiResult.getSuggestions()) {
            reasoning.append("- ").append(suggestion).append("\n");
        }
        reasoningArea.setText(reasoning.toString());
        
        updateOverallScore();
        
        showAlert("成功", "AI分析结果已应用到评分", Alert.AlertType.INFORMATION);
    }
    
    /**
     * 加载已有评分
     */
    private void loadExistingScore(int recordId) {
        // 这里可以加载已有的评分数据
        // 简化处理：重置所有滑块
        for (Slider slider : dimensionSliders.values()) {
            slider.setValue(0);
        }
        commentsArea.clear();
        reasoningArea.clear();
    }
    
    /**
     * 提交评分
     */
    private void submitEvaluation() {
        if (selectedRecord == null) {
            showAlert("提示", "请先选择面试记录", Alert.AlertType.WARNING);
            return;
        }
        
        // 构建评分数据
        Map<EvaluationDimension, Double> scores = new HashMap<>();
        for (Map.Entry<EvaluationDimension, Slider> entry : dimensionSliders.entrySet()) {
            scores.put(entry.getKey(), entry.getValue().getValue());
        }
        
        // 确认提交
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("确认提交");
        confirm.setHeaderText("提交评分");
        confirm.setContentText(String.format("确定为考生 [%s] 提交评分吗？\n综合评分: %s",
            selectedRecord.getCandidateUsername(),
            overallScoreLabel.getText()));
        
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                boolean success = evaluationService.saveScore(
                    selectedRecord.getId(),
                    scores,
                    commentsArea.getText(),
                    reasoningArea.getText()
                );
                
                if (success) {
                    showAlert("成功", "评分提交成功！", Alert.AlertType.INFORMATION);
                    loadInterviewRecords();
                } else {
                    showAlert("错误", "评分提交失败，请重试", Alert.AlertType.ERROR);
                }
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
