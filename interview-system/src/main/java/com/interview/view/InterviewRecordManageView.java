package com.interview.view;

import com.interview.model.InterviewRecord;
import com.interview.model.InterviewRecord.InterviewStatus;
import com.interview.service.InterviewRecordService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.awt.Desktop;
import java.io.File;
import java.util.List;

/**
 * 面试记录管理视图（JavaFX）
 */
public class InterviewRecordManageView extends BorderPane {
    
    private final InterviewRecordService recordService;
    private final boolean canViewAll;
    
    private TableView<InterviewRecord> recordTable;
    private TextArea detailArea;
    
    public InterviewRecordManageView(InterviewRecordService recordService, boolean canViewAll) {
        this.recordService = recordService;
        this.canViewAll = canViewAll;
        
        setPadding(new Insets(10));
        setStyle("-fx-background-color: white;");
        
        // 顶部标题
        setTop(createTitlePanel());
        
        // 中心分割面板
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.6);
        splitPane.getItems().addAll(createRecordListPanel(), createDetailPanel());
        
        setCenter(splitPane);
        
        loadRecords();
    }
    
    private HBox createTitlePanel() {
        HBox panel = new HBox(10);
        panel.setPadding(new Insets(0, 0, 10, 0));
        panel.setAlignment(Pos.CENTER_LEFT);
        
        String title = canViewAll ? "面试记录管理" : "我的面试记录";
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
        
        panel.getChildren().add(titleLabel);
        
        return panel;
    }
    
    private VBox createRecordListPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(5));
        
        Label titleLabel = new Label("记录列表");
        titleLabel.setFont(Font.font(FontWeight.BOLD, 14));
        
        recordTable = new TableView<>();
        
        TableColumn<InterviewRecord, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(String.valueOf(cell.getValue().getId())));
        idCol.setPrefWidth(50);
        
        if (canViewAll) {
            TableColumn<InterviewRecord, String> candidateCol = new TableColumn<>("考生");
            candidateCol.setCellValueFactory(cell -> 
                new SimpleStringProperty(cell.getValue().getCandidateUsername()));
            candidateCol.setPrefWidth(100);
            
            TableColumn<InterviewRecord, String> examinerCol = new TableColumn<>("考官");
            examinerCol.setCellValueFactory(cell -> {
                String examiner = cell.getValue().getExaminerUsername();
                return new SimpleStringProperty(examiner != null ? examiner : "-");
            });
            examinerCol.setPrefWidth(100);
            
            recordTable.getColumns().addAll(idCol, candidateCol, examinerCol);
        } else {
            TableColumn<InterviewRecord, String> timeCol = new TableColumn<>("面试时间");
            timeCol.setCellValueFactory(cell -> {
                var date = cell.getValue().getInterviewTime();
                return new SimpleStringProperty(date != null ? date.toLocalDate().toString() : "-");
            });
            timeCol.setPrefWidth(120);
            
            recordTable.getColumns().addAll(idCol, timeCol);
        }
        
        TableColumn<InterviewRecord, String> statusCol = new TableColumn<>("状态");
        statusCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getStatusDisplayName()));
        statusCol.setPrefWidth(100);
        
        TableColumn<InterviewRecord, String> voiceCol = new TableColumn<>("语音");
        voiceCol.setCellValueFactory(cell -> {
            String name = cell.getValue().getVoiceFileName();
            return new SimpleStringProperty(name != null ? "有" : "无");
        });
        voiceCol.setPrefWidth(60);
        
        recordTable.getColumns().addAll(statusCol, voiceCol);
        
        // 选择事件
        recordTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> showRecordDetail(newVal));
        
        // 按钮栏
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        
        Button refreshBtn = new Button("刷新");
        refreshBtn.setOnAction(e -> loadRecords());
        buttonBox.getChildren().add(refreshBtn);
        
        if (canViewAll) {
            Button playBtn = new Button("播放语音");
            playBtn.setOnAction(e -> playVoiceFile());
            
            Button statusBtn = new Button("更新状态");
            statusBtn.setOnAction(e -> updateStatus());
            
            Button notesBtn = new Button("添加评价");
            notesBtn.setOnAction(e -> addNotes());
            
            Button deleteBtn = new Button("删除");
            deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
            deleteBtn.setOnAction(e -> deleteRecord());
            
            buttonBox.getChildren().addAll(playBtn, statusBtn, notesBtn, deleteBtn);
        } else {
            Button playBtn = new Button("播放语音");
            playBtn.setOnAction(e -> playVoiceFile());
            buttonBox.getChildren().add(playBtn);
        }
        
        panel.getChildren().addAll(titleLabel, recordTable, buttonBox);
        VBox.setVgrow(recordTable, Priority.ALWAYS);
        
        return panel;
    }
    
    private VBox createDetailPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(5));
        
        Label titleLabel = new Label("详细信息");
        titleLabel.setFont(Font.font(FontWeight.BOLD, 14));
        
        detailArea = new TextArea();
        detailArea.setEditable(false);
        detailArea.setWrapText(true);
        detailArea.setText("请选择一条记录查看详情...");
        
        panel.getChildren().addAll(titleLabel, detailArea);
        VBox.setVgrow(detailArea, Priority.ALWAYS);
        
        return panel;
    }
    
    private void loadRecords() {
        List<InterviewRecord> records = canViewAll ? 
            recordService.getAllRecords() : recordService.getMyRecords();
        recordTable.getItems().clear();
        recordTable.getItems().addAll(records);
    }
    
    private void showRecordDetail(InterviewRecord record) {
        if (record == null) {
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("【面试记录详情】\n\n");
        sb.append("记录ID: ").append(record.getId()).append("\n");
        sb.append("考生: ").append(record.getCandidateUsername()).append("\n");
        
        if (canViewAll) {
            sb.append("考官: ").append(record.getExaminerUsername() != null ? 
                record.getExaminerUsername() : "未分配").append("\n");
        }
        
        sb.append("面试时间: ").append(record.getInterviewTime() != null ? 
            record.getInterviewTime() : "未设置").append("\n");
        sb.append("状态: ").append(record.getStatusDisplayName()).append("\n");
        sb.append("创建时间: ").append(record.getCreatedAt() != null ? 
            record.getCreatedAt() : "-").append("\n");
        
        if (record.getVoiceFileName() != null) {
            sb.append("\n【语音文件】\n");
            sb.append("文件名: ").append(record.getVoiceFileName()).append("\n");
            sb.append("文件大小: ").append(record.getFormattedFileSize()).append("\n");
        }
        
        if (record.getNotes() != null && !record.getNotes().isEmpty()) {
            sb.append("\n【面试评价】\n");
            sb.append(record.getNotes()).append("\n");
        }
        
        detailArea.setText(sb.toString());
    }
    
    private void playVoiceFile() {
        InterviewRecord selected = recordTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("提示", "请先选择一条记录", Alert.AlertType.WARNING);
            return;
        }
        
        File voiceFile = recordService.getVoiceFile(selected.getId());
        if (voiceFile == null || !voiceFile.exists()) {
            showAlert("提示", "该记录没有语音文件或文件不存在", Alert.AlertType.WARNING);
            return;
        }
        
        try {
            Desktop.getDesktop().open(voiceFile);
        } catch (Exception e) {
            showAlert("错误", "无法打开语音文件: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void updateStatus() {
        if (!canViewAll) return;
        
        InterviewRecord selected = recordTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("提示", "请先选择一条记录", Alert.AlertType.WARNING);
            return;
        }
        
        ChoiceDialog<InterviewStatus> dialog = new ChoiceDialog<>(
            selected.getStatus(), InterviewStatus.values());
        dialog.setTitle("更新状态");
        dialog.setHeaderText("选择新状态");
        
        dialog.showAndWait().ifPresent(newStatus -> {
            String result = recordService.updateStatus(selected.getId(), newStatus);
            showAlert("提示", result,
                result.contains("成功") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
            loadRecords();
        });
    }
    
    private void addNotes() {
        if (!canViewAll) return;
        
        InterviewRecord selected = recordTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("提示", "请先选择一条记录", Alert.AlertType.WARNING);
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("添加面试评价");
        dialog.setHeaderText("为考生 [" + selected.getCandidateUsername() + "] 添加评价");
        dialog.setContentText("评价内容:");
        
        dialog.showAndWait().ifPresent(notes -> {
            if (!notes.trim().isEmpty()) {
                String result = recordService.updateNotes(selected.getId(), notes);
                showAlert("提示", result,
                    result.contains("成功") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
                loadRecords();
            }
        });
    }
    
    private void deleteRecord() {
        if (!canViewAll) return;
        
        InterviewRecord selected = recordTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("提示", "请先选择一条记录", Alert.AlertType.WARNING);
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("确认删除");
        confirm.setHeaderText("删除面试记录");
        confirm.setContentText("确定要删除考生 [" + selected.getCandidateUsername() + "] 的面试记录吗？");
        
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                String msg = recordService.deleteRecord(selected.getId());
                showAlert("提示", msg,
                    msg.contains("成功") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
                loadRecords();
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
