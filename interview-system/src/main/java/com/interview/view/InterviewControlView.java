package com.interview.view;

import com.interview.model.InterviewRecord;
import com.interview.model.Question;
import com.interview.service.InterviewControlService;
import com.interview.service.QuestionService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.shape.Circle;
import javafx.animation.*;
import javafx.util.Duration;

import java.io.File;
import java.util.List;

/**
 * 面试控制界面（考官端）
 * 控制面试录音、显示实时状态、展示AI分析结果
 */
public class InterviewControlView extends BorderPane {
    
    private final InterviewControlService controlService;
    private final QuestionService questionService;
    
    private Label statusLabel;
    private Label timerLabel;
    private ProgressBar amplitudeBar;
    private Circle recordingIndicator;
    private TextArea logArea;
    private Button startBtn;
    private Button stopBtn;
    private Timeline recordingTimeline;
    private long recordingSeconds = 0;
    
    public InterviewControlView(InterviewControlService controlService, 
                                QuestionService questionService) {
        this.controlService = controlService;
        this.questionService = questionService;
        
        setPadding(new Insets(10));
        getStyleClass().add("bg-secondary");
        
        initComponents();
    }
    
    private void initComponents() {
        // 顶部：标题和状态
        setTop(createHeader());
        
        // 中心：控制面板和日志
        setCenter(createMainPanel());
        
        // 底部：题目列表
        setBottom(createQuestionPanel());
    }
    
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(0, 0, 10, 0));
        
        Label titleLabel = new Label("面试控制中心");
        titleLabel.getStyleClass().add("heading-label");
        
        statusLabel = new Label("等待开始面试...");
        statusLabel.getStyleClass().addAll("caption-label", "alert-info");
        
        header.getChildren().addAll(titleLabel, statusLabel);
        
        return header;
    }
    
    private VBox createMainPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(10));
        
        // 录音控制区域
        panel.getChildren().add(createRecordingPanel());
        
        // 日志区域
        panel.getChildren().add(createLogPanel());
        
        return panel;
    }
    
    private VBox createRecordingPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.getStyleClass().addAll("card", "card-flat", "p-3");
        panel.setAlignment(Pos.CENTER);
        
        // 录音指示器
        HBox indicatorBox = new HBox(10);
        indicatorBox.setAlignment(Pos.CENTER);
        
        recordingIndicator = new Circle(10);
        recordingIndicator.setFill(Color.GRAY);
        
        Label recordingLabel = new Label("录音状态");
        recordingLabel.getStyleClass().add("text-secondary");
        
        indicatorBox.getChildren().addAll(recordingIndicator, recordingLabel);
        
        // 计时器
        timerLabel = new Label("00:00");
        timerLabel.getStyleClass().add("heading-label");
        
        // 音量指示
        amplitudeBar = new ProgressBar(0);
        amplitudeBar.setPrefWidth(300);
        amplitudeBar.getStyleClass().add("progress-bar");
        
        // 按钮
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        
        startBtn = new Button("🎤 开始面试");
        startBtn.setPrefWidth(150);
        startBtn.setPrefHeight(50);
        startBtn.getStyleClass().addAll("button", "button-success", "button-large");
        startBtn.setOnAction(e -> startInterview());
        
        stopBtn = new Button("⏹️ 结束面试");
        stopBtn.setPrefWidth(150);
        stopBtn.setPrefHeight(50);
        stopBtn.getStyleClass().addAll("button", "button-danger", "button-large");
        stopBtn.setDisable(true);
        stopBtn.setOnAction(e -> stopInterview());
        
        buttonBox.getChildren().addAll(startBtn, stopBtn);
        
        panel.getChildren().addAll(indicatorBox, timerLabel, amplitudeBar, buttonBox);
        
        return panel;
    }
    
    private VBox createLogPanel() {
        VBox panel = new VBox(10);
        
        Label titleLabel = new Label("处理日志");
        titleLabel.getStyleClass().add("text-secondary");
        
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(8);
        logArea.getStyleClass().add("text-area");
        
        panel.getChildren().addAll(titleLabel, logArea);
        VBox.setVgrow(logArea, Priority.ALWAYS);
        
        return panel;
    }
    
    private VBox createQuestionPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10, 0, 0, 0));
        
        Label titleLabel = new Label("面试题目");
        titleLabel.getStyleClass().add("text-secondary");
        
        ListView<String> questionList = new ListView<>();
        questionList.setPrefHeight(100);
        questionList.getStyleClass().add("list-view");
        
        // 加载题目
        List<Question> questions = questionService.getAllQuestions();
        for (Question q : questions) {
            questionList.getItems().add(q.getId() + ". " + q.getTitle());
        }
        
        panel.getChildren().addAll(titleLabel, questionList);
        
        return panel;
    }
    
    private void startInterview() {
        // 选择考生（简化版，实际应从列表选择）
        String candidate = "candidate"; // 默认考生
        
        List<Question> questions = questionService.getAllQuestions();
        
        logArea.appendText("[" + getCurrentTime() + "] 开始面试，考生: " + candidate + "\n");
        
        controlService.startInterview(candidate, questions, new InterviewControlService.InterviewCallback() {
            @Override
            public void onRecordingStarted() {
                Platform.runLater(() -> {
                    statusLabel.setText("正在录音...");
                    statusLabel.getStyleClass().removeAll("alert-info");
                    statusLabel.getStyleClass().add("alert-danger");
                    recordingIndicator.setFill(Color.RED);
                    
                    // 闪烁动画
                    FadeTransition fade = new FadeTransition(Duration.millis(800), recordingIndicator);
                    fade.setFromValue(1.0);
                    fade.setToValue(0.3);
                    fade.setCycleCount(Animation.INDEFINITE);
                    fade.setAutoReverse(true);
                    fade.play();
                    recordingIndicator.setUserData(fade);
                    
                    startBtn.setDisable(true);
                    stopBtn.setDisable(false);
                    
                    startTimer();
                    logArea.appendText("[" + getCurrentTime() + "] 录音开始\n");
                });
            }
            
            @Override
            public void onRecordingStopped(File audioFile) {
                Platform.runLater(() -> {
                    statusLabel.setText("录音已停止，正在处理...");
                    statusLabel.getStyleClass().removeAll("alert-danger");
                    statusLabel.getStyleClass().add("alert-info");
                    recordingIndicator.setFill(Color.GRAY);
                    
                    // 停止闪烁
                    if (recordingIndicator.getUserData() instanceof Animation) {
                        ((Animation) recordingIndicator.getUserData()).stop();
                    }
                    
                    stopTimer();
                    logArea.appendText("[" + getCurrentTime() + "] 录音停止，文件: " + audioFile.getName() + "\n");
                });
            }
            
            @Override
            public void onAmplitudeUpdate(double amplitude) {
                Platform.runLater(() -> amplitudeBar.setProgress(amplitude));
            }
            
            @Override
            public void onStatusUpdate(String status) {
                Platform.runLater(() -> {
                    statusLabel.setText(status);
                    logArea.appendText("[" + getCurrentTime() + "] " + status + "\n");
                });
            }
            
            @Override
            public void onAnalysisComplete(String aiResult) {
                Platform.runLater(() -> {
                    statusLabel.setText("AI分析完成，等待评委评分");
                    statusLabel.getStyleClass().removeAll("alert-danger", "alert-info");
                    statusLabel.getStyleClass().add("alert-success");
                    logArea.appendText("[" + getCurrentTime() + "] AI分析完成\n");
                    logArea.appendText("[" + getCurrentTime() + "] 结果: " + aiResult.substring(0, Math.min(100, aiResult.length())) + "...\n");
                    
                    startBtn.setDisable(false);
                    stopBtn.setDisable(true);
                    amplitudeBar.setProgress(0);
                    timerLabel.setText("00:00");
                    
                    // 显示完成对话框
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("面试完成");
                    alert.setHeaderText("AI分析已完成");
                    alert.setContentText("请前往「面试评分」标签页进行人工评分");
                    alert.showAndWait();
                });
            }
            
            @Override
            public void onError(String error) {
                Platform.runLater(() -> {
                    statusLabel.setText("错误: " + error);
                    statusLabel.getStyleClass().removeAll("alert-info", "alert-success");
                    statusLabel.getStyleClass().add("alert-danger");
                    logArea.appendText("[" + getCurrentTime() + "] 错误: " + error + "\n");
                    
                    startBtn.setDisable(false);
                    stopBtn.setDisable(true);
                    stopTimer();
                });
            }
        });
    }
    
    private void stopInterview() {
        controlService.stopInterview();
        logArea.appendText("[" + getCurrentTime() + "] 正在结束面试...\n");
    }
    
    private void startTimer() {
        recordingSeconds = 0;
        recordingTimeline = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> {
                recordingSeconds++;
                long minutes = recordingSeconds / 60;
                long seconds = recordingSeconds % 60;
                timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
            })
        );
        recordingTimeline.setCycleCount(Animation.INDEFINITE);
        recordingTimeline.play();
    }
    
    private void stopTimer() {
        if (recordingTimeline != null) {
            recordingTimeline.stop();
        }
    }
    
    private String getCurrentTime() {
        return java.time.LocalTime.now().toString().substring(0, 8);
    }
}
