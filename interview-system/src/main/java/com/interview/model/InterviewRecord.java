package com.interview.model;

import java.time.LocalDateTime;

/**
 * 面试记录实体类
 * 存储面试过程记录，包括语音文件路径、面试状态等
 */
public class InterviewRecord {
    
    private Integer id;                  // 记录ID
    private String candidateUsername;    // 考生用户名
    private String examinerUsername;     // 考官用户名（可为空）
    private LocalDateTime interviewTime; // 面试时间
    private InterviewStatus status;      // 面试状态
    private String voiceFilePath;        // 语音文件路径
    private String voiceFileName;        // 语音文件名
    private Long voiceFileSize;          // 语音文件大小（字节）
    private String notes;                // 备注/面试评价
    private LocalDateTime createdAt;     // 创建时间
    private LocalDateTime updatedAt;     // 更新时间
    
    // 新增字段：语音识别和AI分析
    private String transcribedText;      // 语音识别转写文本
    private String refinedText;          // 精修后的文本
    private String aiAnalysisResult;     // AI分析结果（JSON格式）
    private String aiRawResponse;        // AI原始返回
    private LocalDateTime aiAnalysisTime;// AI分析时间
    private boolean isRecording;         // 是否正在录音
    private LocalDateTime recordingStartTime; // 录音开始时间
    private Long recordingDuration;      // 录音时长（毫秒）
    private String questionIds;          // 关联的题目ID列表（逗号分隔）
    
    // 面试状态枚举
    public enum InterviewStatus {
        SCHEDULED("已安排", "面试已安排，等待进行"),
        IN_PROGRESS("进行中", "面试正在进行中"),
        COMPLETED("已完成", "面试已完成"),
        CANCELLED("已取消", "面试已取消");
        
        private final String displayName;
        private final String description;
        
        InterviewStatus(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // 构造方法
    public InterviewRecord() {
        this.createdAt = LocalDateTime.now();
        this.status = InterviewStatus.SCHEDULED;
    }
    
    public InterviewRecord(String candidateUsername, String examinerUsername) {
        this();
        this.candidateUsername = candidateUsername;
        this.examinerUsername = examinerUsername;
        this.interviewTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getCandidateUsername() {
        return candidateUsername;
    }
    
    public void setCandidateUsername(String candidateUsername) {
        this.candidateUsername = candidateUsername;
    }
    
    public String getExaminerUsername() {
        return examinerUsername;
    }
    
    public void setExaminerUsername(String examinerUsername) {
        this.examinerUsername = examinerUsername;
    }
    
    public LocalDateTime getInterviewTime() {
        return interviewTime;
    }
    
    public void setInterviewTime(LocalDateTime interviewTime) {
        this.interviewTime = interviewTime;
    }
    
    public InterviewStatus getStatus() {
        return status;
    }
    
    public void setStatus(InterviewStatus status) {
        this.status = status;
    }
    
    public String getVoiceFilePath() {
        return voiceFilePath;
    }
    
    public void setVoiceFilePath(String voiceFilePath) {
        this.voiceFilePath = voiceFilePath;
    }
    
    public String getVoiceFileName() {
        return voiceFileName;
    }
    
    public void setVoiceFileName(String voiceFileName) {
        this.voiceFileName = voiceFileName;
    }
    
    public Long getVoiceFileSize() {
        return voiceFileSize;
    }
    
    public void setVoiceFileSize(Long voiceFileSize) {
        this.voiceFileSize = voiceFileSize;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * 获取状态显示名称
     */
    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : "";
    }
    
    /**
     * 获取格式化文件大小
     */
    public String getFormattedFileSize() {
        if (voiceFileSize == null || voiceFileSize == 0) {
            return "-";
        }
        
        long size = voiceFileSize;
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.2f %s", (double) voiceFileSize / Math.pow(1024, unitIndex), units[unitIndex]);
    }
    
    // 新增字段的 Getters and Setters
    public String getTranscribedText() { return transcribedText; }
    public void setTranscribedText(String transcribedText) { this.transcribedText = transcribedText; }
    
    public String getRefinedText() { return refinedText; }
    public void setRefinedText(String refinedText) { this.refinedText = refinedText; }
    
    public String getAiAnalysisResult() { return aiAnalysisResult; }
    public void setAiAnalysisResult(String aiAnalysisResult) { this.aiAnalysisResult = aiAnalysisResult; }
    
    public String getAiRawResponse() { return aiRawResponse; }
    public void setAiRawResponse(String aiRawResponse) { this.aiRawResponse = aiRawResponse; }
    
    public LocalDateTime getAiAnalysisTime() { return aiAnalysisTime; }
    public void setAiAnalysisTime(LocalDateTime aiAnalysisTime) { this.aiAnalysisTime = aiAnalysisTime; }
    
    public boolean isRecording() { return isRecording; }
    public void setRecording(boolean recording) { isRecording = recording; }
    
    public LocalDateTime getRecordingStartTime() { return recordingStartTime; }
    public void setRecordingStartTime(LocalDateTime recordingStartTime) { this.recordingStartTime = recordingStartTime; }
    
    public Long getRecordingDuration() { return recordingDuration; }
    public void setRecordingDuration(Long recordingDuration) { this.recordingDuration = recordingDuration; }
    
    public String getQuestionIds() { return questionIds; }
    public void setQuestionIds(String questionIds) { this.questionIds = questionIds; }
    
    /**
     * 获取录音时长显示
     */
    public String getRecordingDurationDisplay() {
        if (recordingDuration == null || recordingDuration == 0) {
            return "-";
        }
        long seconds = recordingDuration / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    @Override
    public String toString() {
        return String.format("InterviewRecord{id=%d, candidate='%s', examiner='%s', status=%s}", 
                id, candidateUsername, examinerUsername, status);
    }
}
