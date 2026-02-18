package com.interview.web.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "interview_records")
public class InterviewRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "candidate_username", nullable = false, length = 50)
    private String candidateUsername;
    
    @Column(name = "examiner_username", length = 50)
    private String examinerUsername;
    
    @Column(name = "interview_time")
    private LocalDateTime interviewTime;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InterviewStatus status = InterviewStatus.SCHEDULED;
    
    @Column(name = "voice_file_path", length = 500)
    private String voiceFilePath;
    
    @Column(name = "voice_file_name", length = 255)
    private String voiceFileName;
    
    @Column(name = "voice_file_size")
    private Long voiceFileSize;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public InterviewRecord() {}
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (interviewTime == null) {
            interviewTime = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getCandidateUsername() { return candidateUsername; }
    public void setCandidateUsername(String candidateUsername) { this.candidateUsername = candidateUsername; }
    
    public String getExaminerUsername() { return examinerUsername; }
    public void setExaminerUsername(String examinerUsername) { this.examinerUsername = examinerUsername; }
    
    public LocalDateTime getInterviewTime() { return interviewTime; }
    public void setInterviewTime(LocalDateTime interviewTime) { this.interviewTime = interviewTime; }
    
    public InterviewStatus getStatus() { return status; }
    public void setStatus(InterviewStatus status) { this.status = status; }
    
    public String getVoiceFilePath() { return voiceFilePath; }
    public void setVoiceFilePath(String voiceFilePath) { this.voiceFilePath = voiceFilePath; }
    
    public String getVoiceFileName() { return voiceFileName; }
    public void setVoiceFileName(String voiceFileName) { this.voiceFileName = voiceFileName; }
    
    public Long getVoiceFileSize() { return voiceFileSize; }
    public void setVoiceFileSize(Long voiceFileSize) { this.voiceFileSize = voiceFileSize; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : "";
    }
    
    public String getFormattedFileSize() {
        if (voiceFileSize == null) return "-";
        if (voiceFileSize < 1024) return voiceFileSize + " B";
        if (voiceFileSize < 1024 * 1024) return String.format("%.2f KB", voiceFileSize / 1024.0);
        return String.format("%.2f MB", voiceFileSize / (1024.0 * 1024.0));
    }
    
    public enum InterviewStatus {
        SCHEDULED("已预约"),
        IN_PROGRESS("进行中"),
        COMPLETED("已完成"),
        CANCELLED("已取消");
        
        private final String displayName;
        
        InterviewStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
