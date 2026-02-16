package com.interview.dao;

import com.interview.model.EvaluationDimension;
import com.interview.model.EvaluationScore;
import com.interview.model.EvaluationScore.ScoreType;
import com.interview.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评分记录数据访问对象
 */
public class EvaluationScoreDao {
    
    /**
     * 创建评分记录表
     */
    public void createTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS evaluation_scores (
                id SERIAL PRIMARY KEY,
                interview_record_id INTEGER NOT NULL,
                candidate_username VARCHAR(50) NOT NULL,
                evaluator_username VARCHAR(50),
                score_type VARCHAR(20) NOT NULL,
                comments TEXT,
                reasoning TEXT,
                suggestions TEXT,
                scored_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                submitted BOOLEAN DEFAULT FALSE
            )
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
        
        // 创建维度分数表
        String dimensionSql = """
            CREATE TABLE IF NOT EXISTS evaluation_dimension_scores (
                id SERIAL PRIMARY KEY,
                evaluation_score_id INTEGER NOT NULL REFERENCES evaluation_scores(id) ON DELETE CASCADE,
                dimension_name VARCHAR(50) NOT NULL,
                score INTEGER NOT NULL
            )
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(dimensionSql);
        }
    }
    
    /**
     * 插入评分记录
     */
    public boolean insert(EvaluationScore score) throws SQLException {
        String sql = """
            INSERT INTO evaluation_scores 
            (interview_record_id, candidate_username, evaluator_username, score_type, 
             comments, reasoning, suggestions, scored_at, submitted)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, score.getInterviewRecordId());
            pstmt.setString(2, score.getCandidateUsername());
            pstmt.setString(3, score.getEvaluatorUsername());
            pstmt.setString(4, score.getScoreType().name());
            pstmt.setString(5, score.getComments());
            pstmt.setString(6, score.getReasoning());
            pstmt.setString(7, score.getSuggestions());
            pstmt.setTimestamp(8, score.getScoredAt() != null ? 
                    Timestamp.valueOf(score.getScoredAt()) : null);
            pstmt.setBoolean(9, score.isSubmitted());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    score.setId(rs.getInt(1));
                    // 保存维度分数
                    saveDimensionScores(score);
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 保存维度分数
     */
    private void saveDimensionScores(EvaluationScore score) throws SQLException {
        String sql = """
            INSERT INTO evaluation_dimension_scores 
            (evaluation_score_id, dimension_name, score)
            VALUES (?, ?, ?)
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            for (Map.Entry<EvaluationDimension, Integer> entry : score.getDimensionScores().entrySet()) {
                pstmt.setInt(1, score.getId());
                pstmt.setString(2, entry.getKey().name());
                pstmt.setInt(3, entry.getValue());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }
    
    /**
     * 根据面试记录ID查找所有评分
     */
    public List<EvaluationScore> findByInterviewRecordId(int interviewRecordId) throws SQLException {
        List<EvaluationScore> scores = new ArrayList<>();
        String sql = "SELECT * FROM evaluation_scores WHERE interview_record_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, interviewRecordId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    scores.add(mapResultSetToScore(rs));
                }
            }
        }
        return scores;
    }
    
    /**
     * 根据考生查找所有评分
     */
    public List<EvaluationScore> findByCandidate(String candidateUsername) throws SQLException {
        List<EvaluationScore> scores = new ArrayList<>();
        String sql = "SELECT * FROM evaluation_scores WHERE candidate_username = ? ORDER BY scored_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, candidateUsername);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    scores.add(mapResultSetToScore(rs));
                }
            }
        }
        return scores;
    }
    
    /**
     * 查找特定面试记录的评委评分
     */
    public EvaluationScore findHumanScoreByInterview(int interviewRecordId) throws SQLException {
        String sql = """
            SELECT * FROM evaluation_scores 
            WHERE interview_record_id = ? AND score_type = 'HUMAN'
            LIMIT 1
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, interviewRecordId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToScore(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * 查找特定面试记录的大模型评分
     */
    public EvaluationScore findAIScoreByInterview(int interviewRecordId) throws SQLException {
        String sql = """
            SELECT * FROM evaluation_scores 
            WHERE interview_record_id = ? AND score_type = 'AI'
            LIMIT 1
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, interviewRecordId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToScore(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * 查找特定面试记录的所有评委评分
     */
    public List<EvaluationScore> findHumanScoresByInterview(int interviewRecordId) throws SQLException {
        List<EvaluationScore> scores = new ArrayList<>();
        String sql = """
            SELECT * FROM evaluation_scores 
            WHERE interview_record_id = ? AND score_type = 'HUMAN'
            ORDER BY scored_at ASC
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, interviewRecordId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    scores.add(mapResultSetToScore(rs));
                }
            }
        }
        return scores;
    }
    
    /**
     * 更新评分记录
     */
    public boolean update(EvaluationScore score) throws SQLException {
        String sql = """
            UPDATE evaluation_scores 
            SET comments = ?, reasoning = ?, suggestions = ?, submitted = ?
            WHERE id = ?
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, score.getComments());
            pstmt.setString(2, score.getReasoning());
            pstmt.setString(3, score.getSuggestions());
            pstmt.setBoolean(4, score.isSubmitted());
            pstmt.setInt(5, score.getId());
            
            boolean updated = pstmt.executeUpdate() > 0;
            
            // 更新维度分数
            if (updated) {
                // 先删除旧分数
                deleteDimensionScores(score.getId());
                // 保存新分数
                saveDimensionScores(score);
            }
            
            return updated;
        }
    }
    
    /**
     * 删除维度分数
     */
    private void deleteDimensionScores(int evaluationScoreId) throws SQLException {
        String sql = "DELETE FROM evaluation_dimension_scores WHERE evaluation_score_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, evaluationScoreId);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * 将 ResultSet 映射到 EvaluationScore
     */
    private EvaluationScore mapResultSetToScore(ResultSet rs) throws SQLException {
        EvaluationScore score = new EvaluationScore();
        score.setId(rs.getInt("id"));
        score.setInterviewRecordId(rs.getInt("interview_record_id"));
        score.setCandidateUsername(rs.getString("candidate_username"));
        score.setEvaluatorUsername(rs.getString("evaluator_username"));
        score.setScoreType(ScoreType.valueOf(rs.getString("score_type")));
        score.setComments(rs.getString("comments"));
        score.setReasoning(rs.getString("reasoning"));
        score.setSuggestions(rs.getString("suggestions"));
        score.setSubmitted(rs.getBoolean("submitted"));
        
        Timestamp scoredAt = rs.getTimestamp("scored_at");
        if (scoredAt != null) {
            score.setScoredAt(scoredAt.toLocalDateTime());
        }
        
        // 加载维度分数
        loadDimensionScores(score);
        
        return score;
    }
    
    /**
     * 加载维度分数
     */
    private void loadDimensionScores(EvaluationScore score) throws SQLException {
        String sql = "SELECT * FROM evaluation_dimension_scores WHERE evaluation_score_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, score.getId());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String dimName = rs.getString("dimension_name");
                    int dimScore = rs.getInt("score");
                    try {
                        EvaluationDimension dim = EvaluationDimension.valueOf(dimName);
                        score.setDimensionScore(dim, dimScore);
                    } catch (IllegalArgumentException e) {
                        // 忽略无效的维度名称
                    }
                }
            }
        }
    }
}
