package com.interview.dao;

import com.interview.model.InterviewRecord;
import com.interview.model.InterviewRecord.InterviewStatus;
import com.interview.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 面试记录数据访问对象
 * 处理面试记录相关的数据库操作
 */
public class InterviewRecordDao {
    
    /**
     * 创建面试记录表
     */
    public void createTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS interview_records (
                id SERIAL PRIMARY KEY,
                candidate_username VARCHAR(50) NOT NULL,
                examiner_username VARCHAR(50),
                interview_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
                voice_file_path VARCHAR(500),
                voice_file_name VARCHAR(255),
                voice_file_size BIGINT,
                notes TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * 插入新面试记录
     */
    public boolean insert(InterviewRecord record) throws SQLException {
        String sql = """
            INSERT INTO interview_records 
            (candidate_username, examiner_username, interview_time, status, 
             voice_file_path, voice_file_name, voice_file_size, notes, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, record.getCandidateUsername());
            pstmt.setString(2, record.getExaminerUsername());
            pstmt.setTimestamp(3, record.getInterviewTime() != null ? 
                    Timestamp.valueOf(record.getInterviewTime()) : null);
            pstmt.setString(4, record.getStatus() != null ? record.getStatus().name() : "SCHEDULED");
            pstmt.setString(5, record.getVoiceFilePath());
            pstmt.setString(6, record.getVoiceFileName());
            pstmt.setObject(7, record.getVoiceFileSize(), Types.BIGINT);
            pstmt.setString(8, record.getNotes());
            pstmt.setTimestamp(9, record.getCreatedAt() != null ? 
                    Timestamp.valueOf(record.getCreatedAt()) : null);
            pstmt.setTimestamp(10, record.getUpdatedAt() != null ? 
                    Timestamp.valueOf(record.getUpdatedAt()) : null);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    record.setId(rs.getInt(1));
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 根据ID查找面试记录
     */
    public InterviewRecord findById(int id) throws SQLException {
        String sql = "SELECT * FROM interview_records WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRecord(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * 获取所有面试记录
     */
    public List<InterviewRecord> findAll() throws SQLException {
        List<InterviewRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM interview_records ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                records.add(mapResultSetToRecord(rs));
            }
        }
        return records;
    }
    
    /**
     * 根据考生用户名查找记录
     */
    public List<InterviewRecord> findByCandidate(String candidateUsername) throws SQLException {
        List<InterviewRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM interview_records WHERE candidate_username = ? ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, candidateUsername);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    records.add(mapResultSetToRecord(rs));
                }
            }
        }
        return records;
    }
    
    /**
     * 根据考官用户名查找记录
     */
    public List<InterviewRecord> findByExaminer(String examinerUsername) throws SQLException {
        List<InterviewRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM interview_records WHERE examiner_username = ? ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, examinerUsername);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    records.add(mapResultSetToRecord(rs));
                }
            }
        }
        return records;
    }
    
    /**
     * 更新面试记录
     */
    public boolean update(InterviewRecord record) throws SQLException {
        String sql = """
            UPDATE interview_records 
            SET candidate_username = ?, examiner_username = ?, interview_time = ?, 
                status = ?, voice_file_path = ?, voice_file_name = ?, 
                voice_file_size = ?, notes = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, record.getCandidateUsername());
            pstmt.setString(2, record.getExaminerUsername());
            pstmt.setTimestamp(3, record.getInterviewTime() != null ? 
                    Timestamp.valueOf(record.getInterviewTime()) : null);
            pstmt.setString(4, record.getStatus() != null ? record.getStatus().name() : "SCHEDULED");
            pstmt.setString(5, record.getVoiceFilePath());
            pstmt.setString(6, record.getVoiceFileName());
            pstmt.setObject(7, record.getVoiceFileSize(), Types.BIGINT);
            pstmt.setString(8, record.getNotes());
            pstmt.setInt(9, record.getId());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 更新语音文件信息
     */
    public boolean updateVoiceFile(int recordId, String filePath, String fileName, long fileSize) throws SQLException {
        String sql = """
            UPDATE interview_records 
            SET voice_file_path = ?, voice_file_name = ?, voice_file_size = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, filePath);
            pstmt.setString(2, fileName);
            pstmt.setLong(3, fileSize);
            pstmt.setInt(4, recordId);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 更新面试状态
     */
    public boolean updateStatus(int recordId, InterviewStatus status) throws SQLException {
        String sql = """
            UPDATE interview_records 
            SET status = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status.name());
            pstmt.setInt(2, recordId);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 更新面试评价
     */
    public boolean updateNotes(int recordId, String notes) throws SQLException {
        String sql = """
            UPDATE interview_records 
            SET notes = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, notes);
            pstmt.setInt(2, recordId);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 删除面试记录
     */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM interview_records WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 统计记录数量
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM interview_records";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    /**
     * 统计指定考生的记录数量
     */
    public int countByCandidate(String candidateUsername) throws SQLException {
        String sql = "SELECT COUNT(*) FROM interview_records WHERE candidate_username = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, candidateUsername);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
    
    /**
     * 将 ResultSet 映射到 InterviewRecord 对象
     */
    private InterviewRecord mapResultSetToRecord(ResultSet rs) throws SQLException {
        InterviewRecord record = new InterviewRecord();
        record.setId(rs.getInt("id"));
        record.setCandidateUsername(rs.getString("candidate_username"));
        record.setExaminerUsername(rs.getString("examiner_username"));
        record.setStatus(InterviewStatus.valueOf(rs.getString("status")));
        record.setVoiceFilePath(rs.getString("voice_file_path"));
        record.setVoiceFileName(rs.getString("voice_file_name"));
        
        long fileSize = rs.getLong("voice_file_size");
        if (!rs.wasNull()) {
            record.setVoiceFileSize(fileSize);
        }
        
        record.setNotes(rs.getString("notes"));
        
        Timestamp interviewTime = rs.getTimestamp("interview_time");
        if (interviewTime != null) {
            record.setInterviewTime(interviewTime.toLocalDateTime());
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            record.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            record.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return record;
    }
}
