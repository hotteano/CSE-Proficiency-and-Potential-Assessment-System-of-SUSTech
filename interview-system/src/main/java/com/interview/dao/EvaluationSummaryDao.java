package com.interview.dao;

import com.interview.model.EvaluationDimension;
import com.interview.model.EvaluationSummary;
import com.interview.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 多评委评分汇总数据访问对象
 */
public class EvaluationSummaryDao {
    
    /**
     * 创建汇总评分表
     */
    public void createTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS evaluation_summaries (
                id SERIAL PRIMARY KEY,
                interview_record_id INTEGER NOT NULL UNIQUE,
                candidate_username VARCHAR(50) NOT NULL,
                raw_total_score DOUBLE PRECISION DEFAULT 0,
                normalized_total_score DOUBLE PRECISION DEFAULT 0,
                grade_level VARCHAR(10),
                evaluator_count INTEGER DEFAULT 0,
                evaluator_usernames TEXT,
                lead_evaluator_username VARCHAR(50),
                summary_comments TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
        
        // 创建维度均分表
        String dimensionAvgSql = """
            CREATE TABLE IF NOT EXISTS evaluation_summary_dimensions (
                id SERIAL PRIMARY KEY,
                summary_id INTEGER NOT NULL REFERENCES evaluation_summaries(id) ON DELETE CASCADE,
                dimension_name VARCHAR(50) NOT NULL,
                average_score DOUBLE PRECISION NOT NULL,
                std_dev DOUBLE PRECISION DEFAULT 0,
                ai_score INTEGER,
                final_score INTEGER
            )
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(dimensionAvgSql);
        }
    }
    
    /**
     * 插入或更新汇总记录
     */
    public boolean save(EvaluationSummary summary) throws SQLException {
        // 先检查是否已存在
        EvaluationSummary existing = findByInterviewRecordId(summary.getInterviewRecordId());
        
        if (existing != null) {
            summary.setId(existing.getId());
            return update(summary);
        } else {
            return insert(summary);
        }
    }
    
    /**
     * 插入新汇总记录
     */
    public boolean insert(EvaluationSummary summary) throws SQLException {
        String sql = """
            INSERT INTO evaluation_summaries 
            (interview_record_id, candidate_username, raw_total_score, normalized_total_score,
             grade_level, evaluator_count, evaluator_usernames, lead_evaluator_username,
             summary_comments, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, summary.getInterviewRecordId());
            pstmt.setString(2, summary.getCandidateUsername());
            pstmt.setDouble(3, summary.getRawTotalScore());
            pstmt.setDouble(4, summary.getNormalizedTotalScore());
            pstmt.setString(5, summary.getGradeLevel());
            pstmt.setInt(6, summary.getEvaluatorCount());
            pstmt.setString(7, summary.getEvaluatorUsernames());
            pstmt.setString(8, summary.getLeadEvaluatorUsername());
            pstmt.setString(9, summary.getSummaryComments());
            pstmt.setTimestamp(10, Timestamp.valueOf(summary.getCreatedAt()));
            pstmt.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    summary.setId(rs.getInt(1));
                    // 保存维度分数
                    saveDimensionScores(summary);
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 更新汇总记录
     */
    public boolean update(EvaluationSummary summary) throws SQLException {
        String sql = """
            UPDATE evaluation_summaries 
            SET raw_total_score = ?, normalized_total_score = ?, grade_level = ?,
                evaluator_count = ?, evaluator_usernames = ?, lead_evaluator_username = ?,
                summary_comments = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, summary.getRawTotalScore());
            pstmt.setDouble(2, summary.getNormalizedTotalScore());
            pstmt.setString(3, summary.getGradeLevel());
            pstmt.setInt(4, summary.getEvaluatorCount());
            pstmt.setString(5, summary.getEvaluatorUsernames());
            pstmt.setString(6, summary.getLeadEvaluatorUsername());
            pstmt.setString(7, summary.getSummaryComments());
            pstmt.setInt(8, summary.getId());
            
            boolean updated = pstmt.executeUpdate() > 0;
            if (updated) {
                // 删除旧维度分数，保存新的
                deleteDimensionScores(summary.getId());
                saveDimensionScores(summary);
            }
            return updated;
        }
    }
    
    /**
     * 保存维度分数
     */
    private void saveDimensionScores(EvaluationSummary summary) throws SQLException {
        String sql = """
            INSERT INTO evaluation_summary_dimensions 
            (summary_id, dimension_name, average_score, std_dev, ai_score, final_score)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            for (EvaluationDimension dim : EvaluationDimension.values()) {
                pstmt.setInt(1, summary.getId());
                pstmt.setString(2, dim.name());
                pstmt.setDouble(3, summary.getDimensionAverage(dim));
                
                Double stdDev = summary.getDimensionStdDevs().get(dim);
                pstmt.setDouble(4, stdDev != null ? stdDev : 0);
                
                Integer aiScore = summary.getAiDimensionScores().get(dim);
                pstmt.setObject(5, aiScore);
                
                Integer finalScore = summary.getFinalDimensionScores().get(dim);
                pstmt.setObject(6, finalScore);
                
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }
    
    /**
     * 删除维度分数
     */
    private void deleteDimensionScores(int summaryId) throws SQLException {
        String sql = "DELETE FROM evaluation_summary_dimensions WHERE summary_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, summaryId);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * 根据面试记录ID查找汇总记录
     */
    public EvaluationSummary findByInterviewRecordId(int interviewRecordId) throws SQLException {
        String sql = "SELECT * FROM evaluation_summaries WHERE interview_record_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, interviewRecordId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSummary(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * 获取所有汇总记录
     */
    public List<EvaluationSummary> findAll() throws SQLException {
        List<EvaluationSummary> summaries = new ArrayList<>();
        String sql = "SELECT * FROM evaluation_summaries ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                summaries.add(mapResultSetToSummary(rs));
            }
        }
        return summaries;
    }
    
    /**
     * 根据考生用户名查找汇总记录
     */
    public List<EvaluationSummary> findByCandidate(String candidateUsername) throws SQLException {
        List<EvaluationSummary> summaries = new ArrayList<>();
        String sql = "SELECT * FROM evaluation_summaries WHERE candidate_username = ? ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, candidateUsername);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    summaries.add(mapResultSetToSummary(rs));
                }
            }
        }
        return summaries;
    }
    
    /**
     * 加载维度分数到汇总对象
     */
    private void loadDimensionScores(EvaluationSummary summary) throws SQLException {
        String sql = "SELECT * FROM evaluation_summary_dimensions WHERE summary_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, summary.getId());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String dimName = rs.getString("dimension_name");
                    try {
                        EvaluationDimension dim = EvaluationDimension.valueOf(dimName);
                        summary.setDimensionAverage(dim, rs.getDouble("average_score"));
                        summary.setDimensionStdDev(dim, rs.getDouble("std_dev"));
                        
                        int aiScore = rs.getInt("ai_score");
                        if (!rs.wasNull()) {
                            summary.getAiDimensionScores().put(dim, aiScore);
                        }
                        
                        int finalScore = rs.getInt("final_score");
                        if (!rs.wasNull()) {
                            summary.getFinalDimensionScores().put(dim, finalScore);
                        }
                    } catch (IllegalArgumentException e) {
                        // 忽略未知的维度
                    }
                }
            }
        }
    }
    
    /**
     * 将 ResultSet 映射到 EvaluationSummary
     */
    private EvaluationSummary mapResultSetToSummary(ResultSet rs) throws SQLException {
        EvaluationSummary summary = new EvaluationSummary();
        summary.setId(rs.getInt("id"));
        summary.setInterviewRecordId(rs.getInt("interview_record_id"));
        summary.setCandidateUsername(rs.getString("candidate_username"));
        summary.setRawTotalScore(rs.getDouble("raw_total_score"));
        summary.setNormalizedTotalScore(rs.getDouble("normalized_total_score"));
        summary.setGradeLevel(rs.getString("grade_level"));
        summary.setEvaluatorCount(rs.getInt("evaluator_count"));
        summary.setEvaluatorUsernames(rs.getString("evaluator_usernames"));
        summary.setLeadEvaluatorUsername(rs.getString("lead_evaluator_username"));
        summary.setSummaryComments(rs.getString("summary_comments"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            summary.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            summary.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        // 加载维度分数
        loadDimensionScores(summary);
        
        return summary;
    }
}
