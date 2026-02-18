package com.interview.view;

import com.interview.model.*;
import com.interview.service.AuthService;
import com.interview.service.EvaluationService;
import com.interview.service.InterviewRecordService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Map;

/**
 * 评测报告界面（JavaFX）
 * 选择面试记录并查看/生成评测报告
 */
public class ReportView extends BorderPane {
    
    private final EvaluationService evaluationService;
    private final InterviewRecordService recordService;
    
    private VBox reportContent;
    private Label gradeLabel;
    private Label scoreLabel;
    private BarChart<String, Number> categoryChart;
    private TableView<InterviewRecord> recordTable;
    private Label selectedRecordLabel;
    private Button generateReportBtn;
    
    public ReportView(AuthService authService) {
        this.evaluationService = new EvaluationService(authService);
        this.recordService = new InterviewRecordService(authService);
        
        setPadding(new Insets(10));
        getStyleClass().add("bg-secondary");
        
        initComponents();
        loadInterviewRecords();
    }
    
    private void initComponents() {
        // 左侧：面试记录选择
        setLeft(createRecordSelectionPanel());
        
        // 中心：报告内容
        ScrollPane scrollPane = new ScrollPane(createReportPanel());
        scrollPane.setFitToWidth(true);
        setCenter(scrollPane);
    }
    
    /**
     * 创建面试记录选择面板
     */
    private VBox createRecordSelectionPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(300);
        panel.getStyleClass().addAll("card", "p-3");
        
        Label titleLabel = new Label("选择面试记录");
        titleLabel.getStyleClass().add("heading-label");
        
        // 刷新按钮
        Button refreshBtn = new Button("🔄 刷新");
        refreshBtn.getStyleClass().addAll("button", "button-secondary", "button-small");
        refreshBtn.setOnAction(e -> loadInterviewRecords());
        
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().addAll(titleLabel, refreshBtn);
        
        // 面试记录表格
        recordTable = new TableView<>();
        recordTable.getStyleClass().add("table-view");
        
        TableColumn<InterviewRecord, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(String.valueOf(cell.getValue().getId())));
        idCol.setPrefWidth(50);
        
        TableColumn<InterviewRecord, String> candidateCol = new TableColumn<>("考生");
        candidateCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getCandidateUsername()));
        candidateCol.setPrefWidth(100);
        
        TableColumn<InterviewRecord, String> statusCol = new TableColumn<>("状态");
        statusCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getStatusDisplayName()));
        statusCol.setPrefWidth(80);
        
        TableColumn<InterviewRecord, String> timeCol = new TableColumn<>("时间");
        timeCol.setCellValueFactory(cell -> {
            var time = cell.getValue().getInterviewTime();
            return new SimpleStringProperty(time != null ? 
                time.toLocalDate().toString() : "-");
        });
        timeCol.setPrefWidth(80);
        
        recordTable.getColumns().addAll(idCol, candidateCol, statusCol, timeCol);
        recordTable.setPrefHeight(350);
        
        // 选择事件
        recordTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> onRecordSelected(newVal));
        
        // 已选记录信息
        selectedRecordLabel = new Label("请从上方选择一条记录\n查看或生成评测报告");
        selectedRecordLabel.setWrapText(true);
        selectedRecordLabel.getStyleClass().add("subtitle-label");
        
        // 生成报告按钮
        generateReportBtn = new Button("📊 生成评测报告");
        generateReportBtn.getStyleClass().addAll("button", "button-success");
        generateReportBtn.setPrefWidth(200);
        generateReportBtn.setDisable(true);
        generateReportBtn.setOnAction(e -> generateReport());
        
        panel.getChildren().addAll(headerBox, recordTable, selectedRecordLabel, generateReportBtn);
        VBox.setVgrow(recordTable, Priority.ALWAYS);
        
        return panel;
    }
    
    /**
     * 创建报告面板
     */
    private VBox createReportPanel() {
        reportContent = new VBox(20);
        reportContent.setPadding(new Insets(20));
        reportContent.getStyleClass().addAll("card", "p-3");
        
        // 总体评分区域
        reportContent.getChildren().add(createOverallSection());
        
        // 图表区域
        reportContent.getChildren().add(createChartsSection());
        
        // 详细分析
        reportContent.getChildren().add(createAnalysisSection());
        
        // 发展建议
        reportContent.getChildren().add(createSuggestionsSection());
        
        return reportContent;
    }
    
    private HBox createOverallSection() {
        HBox section = new HBox(30);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(20));
        section.getStyleClass().addAll("card-flat", "p-3");
        
        // 等级显示
        VBox gradeBox = new VBox(5);
        gradeBox.setAlignment(Pos.CENTER);
        Label gradeTitle = new Label("评级");
        gradeTitle.getStyleClass().add("subtitle-label");
        gradeLabel = new Label("-");
        gradeLabel.setFont(Font.font(null, FontWeight.BOLD, 48));
        gradeLabel.getStyleClass().add("badge-warning");
        gradeBox.getChildren().addAll(gradeTitle, gradeLabel);
        
        // 分数显示
        VBox scoreBox = new VBox(5);
        scoreBox.setAlignment(Pos.CENTER);
        Label scoreTitle = new Label("综合评分");
        scoreTitle.getStyleClass().add("subtitle-label");
        scoreLabel = new Label("0.0");
        scoreLabel.setFont(Font.font(null, FontWeight.BOLD, 48));
        scoreLabel.getStyleClass().add("badge-info");
        scoreBox.getChildren().addAll(scoreTitle, scoreLabel);
        
        section.getChildren().addAll(gradeBox, new Separator(javafx.geometry.Orientation.VERTICAL), scoreBox);
        
        return section;
    }
    
    private HBox createChartsSection() {
        HBox section = new HBox(20);
        section.setAlignment(Pos.CENTER);
        
        // 分类柱状图
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        categoryChart = new BarChart<>(xAxis, yAxis);
        categoryChart.setTitle("各维度得分");
        categoryChart.setPrefWidth(500);
        categoryChart.setPrefHeight(300);
        
        section.getChildren().add(categoryChart);
        
        return section;
    }
    
    private VBox createAnalysisSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));
        section.getStyleClass().addAll("card-flat", "p-3");
        
        Label titleLabel = new Label("详细分析");
        titleLabel.getStyleClass().add("heading-label");
        
        section.getChildren().add(titleLabel);
        
        return section;
    }
    
    private VBox createSuggestionsSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));
        section.getStyleClass().addAll("card-flat", "p-3");
        
        Label titleLabel = new Label("发展建议");
        titleLabel.getStyleClass().add("heading-label");
        
        section.getChildren().add(titleLabel);
        
        return section;
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
        if (record == null) {
            generateReportBtn.setDisable(true);
            return;
        }
        
        // 更新已选记录信息
        String info = String.format("已选择记录 #%d\n考生: %s\n状态: %s",
            record.getId(),
            record.getCandidateUsername(),
            record.getStatusDisplayName()
        );
        selectedRecordLabel.setText(info);
        generateReportBtn.setDisable(false);
        
        // 尝试加载已有报告
        loadExistingReport(record.getId());
    }
    
    /**
     * 生成评测报告
     */
    private void generateReport() {
        InterviewRecord selected = recordTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("提示", "请先选择面试记录", Alert.AlertType.WARNING);
            return;
        }
        
        // 显示确认对话框
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("生成报告");
        confirm.setHeaderText("生成评测报告");
        confirm.setContentText("将为考生 [" + selected.getCandidateUsername() + "] 生成综合评测报告。\n" +
            "如果已有报告将被覆盖。是否继续？");
        confirm.getDialogPane().getStyleClass().add("dialog-pane");
        
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                // 调用服务生成报告
                EvaluationReport report = evaluationService.generateReport(selected.getId(), selected);
                if (report != null) {
                    setReport(report);
                    showAlert("成功", "评测报告生成完成！", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("错误", "生成报告失败，请确保已有评分数据", Alert.AlertType.ERROR);
                }
            }
        });
    }
    
    /**
     * 加载已有报告
     */
    private void loadExistingReport(int recordId) {
        // 尝试获取已有报告（如果有保存的话）
        // 这里简化处理，实际应该从数据库加载
        // 如果没有，显示空报告
        clearReport();
    }
    
    /**
     * 清空报告内容
     */
    private void clearReport() {
        gradeLabel.setText("-");
        scoreLabel.setText("0.0");
        categoryChart.getData().clear();
        
        // 清空分析内容
        VBox analysisSection = (VBox) reportContent.getChildren().get(2);
        analysisSection.getChildren().clear();
        Label titleLabel = new Label("详细分析");
        titleLabel.getStyleClass().add("heading-label");
        analysisSection.getChildren().add(titleLabel);
        
        // 清空建议内容
        VBox suggestionSection = (VBox) reportContent.getChildren().get(3);
        suggestionSection.getChildren().clear();
        Label suggestTitle = new Label("发展建议");
        suggestTitle.getStyleClass().add("heading-label");
        suggestionSection.getChildren().add(suggestTitle);
    }
    
    public void setReport(EvaluationReport report) {
        if (report == null) {
            return;
        }
        
        // 更新总评分
        gradeLabel.setText(report.getGradeLevel());
        scoreLabel.setText(String.format("%.1f", report.getTotalScore()));
        
        // 更新图表
        updateCategoryChart(report.getCategoryScores());
        
        // 更新分析内容
        updateAnalysis(report);
        
        // 更新建议
        updateSuggestions(report);
    }
    
    private void updateCategoryChart(Map<EvaluationDimension.Category, Double> categoryScores) {
        categoryChart.getData().clear();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("得分");
        
        for (Map.Entry<EvaluationDimension.Category, Double> entry : categoryScores.entrySet()) {
            series.getData().add(new XYChart.Data<>(
                entry.getKey().getDisplayName(),
                entry.getValue()
            ));
        }
        
        categoryChart.getData().add(series);
    }
    
    private void updateAnalysis(EvaluationReport report) {
        VBox analysisSection = (VBox) reportContent.getChildren().get(2);
        analysisSection.getChildren().clear();
        
        Label titleLabel = new Label("详细分析");
        titleLabel.getStyleClass().add("heading-label");
        analysisSection.getChildren().add(titleLabel);
        
        // 优势维度
        if (!report.getStrengths().isEmpty()) {
            Label strengthLabel = new Label("优势维度:");
            strengthLabel.getStyleClass().addAll("subtitle-label", "badge-success");
            analysisSection.getChildren().add(strengthLabel);
            
            for (EvaluationDimension dim : report.getStrengths()) {
                Double score = report.getCompositeDimensionScores().get(dim);
                Label dimLabel = new Label("  • " + dim.getDisplayName() + 
                    " (" + String.format("%.1f", score) + "分)");
                dimLabel.getStyleClass().add("badge-success");
                analysisSection.getChildren().add(dimLabel);
            }
        }
        
        // 待提升维度
        if (!report.getWeaknesses().isEmpty()) {
            Label weaknessLabel = new Label("待提升维度:");
            weaknessLabel.getStyleClass().addAll("subtitle-label", "badge-warning");
            analysisSection.getChildren().add(weaknessLabel);
            
            for (EvaluationDimension dim : report.getWeaknesses()) {
                Double score = report.getCompositeDimensionScores().get(dim);
                Label dimLabel = new Label("  • " + dim.getDisplayName() + 
                    " (" + String.format("%.1f", score) + "分)");
                dimLabel.getStyleClass().add("badge-warning");
                analysisSection.getChildren().add(dimLabel);
            }
        }
        
        // 综合评价
        if (report.getOverallComment() != null) {
            Label commentTitle = new Label("综合评价:");
            commentTitle.getStyleClass().add("subtitle-label");
            analysisSection.getChildren().add(commentTitle);
            
            Label commentLabel = new Label(report.getOverallComment());
            commentLabel.setWrapText(true);
            analysisSection.getChildren().add(commentLabel);
        }
    }
    
    private void updateSuggestions(EvaluationReport report) {
        VBox suggestionSection = (VBox) reportContent.getChildren().get(3);
        suggestionSection.getChildren().clear();
        
        Label titleLabel = new Label("发展建议");
        titleLabel.getStyleClass().add("heading-label");
        suggestionSection.getChildren().add(titleLabel);
        
        for (String suggestion : report.getDevelopmentSuggestions()) {
            Label suggestionLabel = new Label("• " + suggestion);
            suggestionLabel.setWrapText(true);
            suggestionSection.getChildren().add(suggestionLabel);
        }
        
        // 适合岗位
        if (!report.getSuitablePositions().isEmpty()) {
            Label positionTitle = new Label("适合的岗位方向:");
            positionTitle.getStyleClass().add("subtitle-label");
            suggestionSection.getChildren().add(positionTitle);
            
            StringBuilder positions = new StringBuilder();
            for (int i = 0; i < report.getSuitablePositions().size(); i++) {
                if (i > 0) positions.append("、");
                positions.append(report.getSuitablePositions().get(i));
            }
            Label positionLabel = new Label(positions.toString());
            positionLabel.setWrapText(true);
            positionLabel.getStyleClass().add("badge-info");
            suggestionSection.getChildren().add(positionLabel);
        }
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
