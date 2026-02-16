package com.interview.view;

import com.interview.model.InterviewRecord;
import com.interview.service.InterviewRecordService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

/**
 * 考生面试视图（JavaFX）
 */
public class CandidateInterviewView extends BorderPane {
    
    private final InterviewRecordService recordService;
    
    private TableView<InterviewRecord> recordTable;
    private Label statusLabel;
    
    public CandidateInterviewView(InterviewRecordService recordService) {
        this.recordService = recordService;
        
        setPadding(new Insets(10));
        setStyle("-fx-background-color: white;");
        
        // 顶部信息面板
        setTop(createInfoPanel());
        
        // 中心记录列表
        setCenter(createRecordListPanel());
        
        // 底部操作按钮
        setBottom(createButtonPanel());
        
        loadMyRecords();
    }
    
    private VBox createInfoPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(0, 0, 10, 0));
        panel.setStyle("-fx-background-color: #e3f2fd; -fx-padding: 15px; -fx-background-radius: 5px;");
        
        Label titleLabel = new Label("考生面试中心");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 20));
        titleLabel.setStyle("-fx-text-fill: #1565c0;");
        
        Label infoLabel = new Label(
            "欢迎使用面试中心！\n" +
            "1. 点击「开始新面试」创建面试记录\n" +
            "2. 面试过程中可以上传语音文件\n" +
            "3. 在「我的面试记录」中查看历史"
        );
        infoLabel.setFont(Font.font(13));
        
        panel.getChildren().addAll(titleLabel, infoLabel);
        
        return panel;
    }
    
    private VBox createRecordListPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10, 0, 10, 0));
        
        Label titleLabel = new Label("我的面试记录");
        titleLabel.setFont(Font.font(FontWeight.BOLD, 14));
        
        recordTable = new TableView<>();
        
        TableColumn<InterviewRecord, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(String.valueOf(cell.getValue().getId())));
        idCol.setPrefWidth(50);
        
        TableColumn<InterviewRecord, String> timeCol = new TableColumn<>("面试时间");
        timeCol.setCellValueFactory(cell -> {
            var date = cell.getValue().getInterviewTime();
            return new SimpleStringProperty(date != null ? date.toLocalDate().toString() : "-");
        });
        timeCol.setPrefWidth(120);
        
        TableColumn<InterviewRecord, String> statusCol = new TableColumn<>("状态");
        statusCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getStatusDisplayName()));
        statusCol.setPrefWidth(100);
        
        TableColumn<InterviewRecord, String> voiceCol = new TableColumn<>("语音文件");
        voiceCol.setCellValueFactory(cell -> {
            String name = cell.getValue().getVoiceFileName();
            return new SimpleStringProperty(name != null ? name : "未上传");
        });
        voiceCol.setPrefWidth(200);
        
        TableColumn<InterviewRecord, String> createdCol = new TableColumn<>("创建时间");
        createdCol.setCellValueFactory(cell -> {
            var date = cell.getValue().getCreatedAt();
            return new SimpleStringProperty(date != null ? date.toLocalDate().toString() : "-");
        });
        createdCol.setPrefWidth(120);
        
        recordTable.getColumns().addAll(idCol, timeCol, statusCol, voiceCol, createdCol);
        
        panel.getChildren().addAll(titleLabel, recordTable);
        VBox.setVgrow(recordTable, Priority.ALWAYS);
        
        return panel;
    }
    
    private HBox createButtonPanel() {
        HBox panel = new HBox(15);
        panel.setPadding(new Insets(10, 0, 0, 0));
        panel.setAlignment(Pos.CENTER);
        
        Button newInterviewBtn = new Button("开始新面试");
        newInterviewBtn.setPrefWidth(150);
        newInterviewBtn.setPrefHeight(45);
        newInterviewBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        newInterviewBtn.setOnAction(e -> startNewInterview());
        
        Button uploadVoiceBtn = new Button("上传语音文件");
        uploadVoiceBtn.setPrefWidth(150);
        uploadVoiceBtn.setPrefHeight(45);
        uploadVoiceBtn.setStyle("-fx-background-color: #4682b4; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        uploadVoiceBtn.setOnAction(e -> uploadVoiceFile());
        
        Button refreshBtn = new Button("刷新");
        refreshBtn.setPrefWidth(100);
        refreshBtn.setPrefHeight(45);
        refreshBtn.setOnAction(e -> loadMyRecords());
        
        panel.getChildren().addAll(newInterviewBtn, uploadVoiceBtn, refreshBtn);
        
        return panel;
    }
    
    private void loadMyRecords() {
        List<InterviewRecord> records = recordService.getMyRecords();
        recordTable.getItems().clear();
        recordTable.getItems().addAll(records);
    }
    
    private void startNewInterview() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("确认");
        confirm.setHeaderText("开始新面试");
        confirm.setContentText("确定要开始新的面试吗？");
        
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                String msg = recordService.createRecordForSelf();
                showAlert(msg.contains("成功") ? "成功" : "错误", msg, 
                    msg.contains("成功") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
                loadMyRecords();
            }
        });
    }
    
    private void uploadVoiceFile() {
        InterviewRecord selected = recordTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("提示", "请先选择要上传语音的面试记录", Alert.AlertType.WARNING);
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择语音文件");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("音频文件", "*.mp3", "*.wav", "*.ogg", "*.m4a")
        );
        
        File selectedFile = fileChooser.showOpenDialog(getScene().getWindow());
        if (selectedFile == null) {
            return;
        }
        
        // 确认上传
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("确认上传");
        confirm.setHeaderText("上传语音文件");
        confirm.setContentText("文件名: " + selectedFile.getName() + "\n大小: " + formatFileSize(selectedFile.length()));
        
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                String msg = recordService.saveVoiceFile(selected.getId(), selectedFile);
                showAlert(msg.contains("成功") ? "成功" : "错误", msg,
                    msg.contains("成功") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
                loadMyRecords();
            }
        });
    }
    
    private String formatFileSize(long size) {
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double fileSize = size;
        
        while (fileSize >= 1024 && unitIndex < units.length - 1) {
            fileSize /= 1024;
            unitIndex++;
        }
        
        return String.format("%.2f %s", fileSize, units[unitIndex]);
    }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
