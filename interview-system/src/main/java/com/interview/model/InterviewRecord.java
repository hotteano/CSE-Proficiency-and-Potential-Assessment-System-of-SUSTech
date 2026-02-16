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
    
    @Override
    public String toString() {
        return String.format("InterviewRecord{id=%d, candidate='%s', examiner='%s', status=%s}", 
                id, candidateUsername, examinerUsername, status);
    }
}
