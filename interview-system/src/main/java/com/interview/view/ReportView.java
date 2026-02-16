package com.interview.view;

import com.interview.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Map;

/**
 * 评测报告界面（JavaFX）
 */
public class ReportView extends BorderPane {
    
    private VBox reportContent;
    private Label gradeLabel;
    private Label scoreLabel;
    private BarChart<String, Number> categoryChart;
    
    public ReportView() {
        setPadding(new Insets(10));
        setStyle("-fx-background-color: white;");
        
        initComponents();
    }
    
    private void initComponents() {
        // 顶部标题
        Label titleLabel = new Label("计算机科学能力与潜力评测报告");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 22));
        titleLabel.setStyle("-fx-text-fill: #1565c0;");
        setTop(titleLabel);
        
        // 中心：报告内容
        ScrollPane scrollPane = new ScrollPane(createReportPanel());
        scrollPane.setFitToWidth(true);
        setCenter(scrollPane);
        
        // 底部：导出按钮
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));
        
        Button exportBtn = new Button("导出报告");
        exportBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold;");
        exportBtn.setOnAction(e -> exportReport());
        
        buttonBox.getChildren().add(exportBtn);
        setBottom(buttonBox);
    }
    
    private VBox createReportPanel() {
        reportContent = new VBox(20);
        reportContent.setPadding(new Insets(20));
        
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
        section.setStyle("-fx-background-color: #e3f2fd; -fx-background-radius: 10px;");
        
        // 等级显示
        VBox gradeBox = new VBox(5);
        gradeBox.setAlignment(Pos.CENTER);
        Label gradeTitle = new Label("评级");
        gradeTitle.setFont(Font.font(14));
        gradeLabel = new Label("-");
        gradeLabel.setFont(Font.font(null, FontWeight.BOLD, 48));
        gradeLabel.setStyle("-fx-text-fill: #ff6f00;");
        gradeBox.getChildren().addAll(gradeTitle, gradeLabel);
        
        // 分数显示
        VBox scoreBox = new VBox(5);
        scoreBox.setAlignment(Pos.CENTER);
        Label scoreTitle = new Label("综合评分");
        scoreTitle.setFont(Font.font(14));
        scoreLabel = new Label("0.0");
        scoreLabel.setFont(Font.font(null, FontWeight.BOLD, 48));
        scoreLabel.setStyle("-fx-text-fill: #1565c0;");
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
        
        // TODO: 添加雷达图
        
        section.getChildren().add(categoryChart);
        
        return section;
    }
    
    private VBox createAnalysisSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 5px;");
        
        Label titleLabel = new Label("详细分析");
        titleLabel.setFont(Font.font(null, FontWeight.BOLD, 16));
        
        // 优势维度
        Label strengthLabel = new Label("优势维度:");
        strengthLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
        strengthLabel.setStyle("-fx-text-fill: #4caf50;");
        
        // 待提升维度
        Label weaknessLabel = new Label("待提升维度:");
        weaknessLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
        weaknessLabel.setStyle("-fx-text-fill: #f44336;");
        
        section.getChildren().addAll(titleLabel, strengthLabel, weaknessLabel);
        
        return section;
    }
    
    private VBox createSuggestionsSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: #fff3e0; -fx-background-radius: 5px;");
        
        Label titleLabel = new Label("发展建议");
        titleLabel.setFont(Font.font(null, FontWeight.BOLD, 16));
        titleLabel.setStyle("-fx-text-fill: #e65100;");
        
        section.getChildren().add(titleLabel);
        
        return section;
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
        // 清空并重新添加分析内容
        VBox analysisSection = (VBox) reportContent.getChildren().get(2);
        analysisSection.getChildren().clear();
        
        Label titleLabel = new Label("详细分析");
        titleLabel.setFont(Font.font(null, FontWeight.BOLD, 16));
        analysisSection.getChildren().add(titleLabel);
        
        // 优势维度
        if (!report.getStrengths().isEmpty()) {
            Label strengthLabel = new Label("优势维度:");
            strengthLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
            strengthLabel.setStyle("-fx-text-fill: #4caf50;");
            analysisSection.getChildren().add(strengthLabel);
            
            for (EvaluationDimension dim : report.getStrengths()) {
                Double score = report.getCompositeDimensionScores().get(dim);
                Label dimLabel = new Label("  • " + dim.getDisplayName() + 
                    " (" + String.format("%.1f", score) + "分)");
                analysisSection.getChildren().add(dimLabel);
            }
        }
        
        // 待提升维度
        if (!report.getWeaknesses().isEmpty()) {
            Label weaknessLabel = new Label("待提升维度:");
            weaknessLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
            weaknessLabel.setStyle("-fx-text-fill: #f44336;");
            analysisSection.getChildren().add(weaknessLabel);
            
            for (EvaluationDimension dim : report.getWeaknesses()) {
                Double score = report.getCompositeDimensionScores().get(dim);
                Label dimLabel = new Label("  • " + dim.getDisplayName() + 
                    " (" + String.format("%.1f", score) + "分)");
                analysisSection.getChildren().add(dimLabel);
            }
        }
        
        // 综合评价
        if (report.getOverallComment() != null) {
            Label commentTitle = new Label("综合评价:");
            commentTitle.setFont(Font.font(null, FontWeight.BOLD, 14));
            analysisSection.getChildren().add(commentTitle);
            
            Label commentLabel = new Label(report.getOverallComment());
            commentLabel.setWrapText(true);
            analysisSection.getChildren().add(commentLabel);
        }
        
        // 更新建议
        updateSuggestions(report);
    }
    
    private void updateSuggestions(EvaluationReport report) {
        VBox suggestionSection = (VBox) reportContent.getChildren().get(3);
        suggestionSection.getChildren().clear();
        
        Label titleLabel = new Label("发展建议");
        titleLabel.setFont(Font.font(null, FontWeight.BOLD, 16));
        titleLabel.setStyle("-fx-text-fill: #e65100;");
        suggestionSection.getChildren().add(titleLabel);
        
        for (String suggestion : report.getDevelopmentSuggestions()) {
            Label suggestionLabel = new Label("• " + suggestion);
            suggestionLabel.setWrapText(true);
            suggestionSection.getChildren().add(suggestionLabel);
        }
        
        // 适合岗位
        if (!report.getSuitablePositions().isEmpty()) {
            Label positionTitle = new Label("适合的岗位方向:");
            positionTitle.setFont(Font.font(null, FontWeight.BOLD, 14));
            positionTitle.setStyle("-fx-text-fill: #1565c0;");
            suggestionSection.getChildren().add(positionTitle);
            
            StringBuilder positions = new StringBuilder();
            for (int i = 0; i < report.getSuitablePositions().size(); i++) {
                if (i > 0) positions.append("、");
                positions.append(report.getSuitablePositions().get(i));
            }
            Label positionLabel = new Label(positions.toString());
            positionLabel.setWrapText(true);
            suggestionSection.getChildren().add(positionLabel);
        }
    }
    
    private void exportReport() {
        // TODO: 实现报告导出功能
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("导出");
        alert.setHeaderText(null);
        alert.setContentText("报告导出功能开发中...");
        alert.showAndWait();
    }
}
