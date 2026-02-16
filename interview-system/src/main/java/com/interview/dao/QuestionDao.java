package com.interview.dao;

import com.interview.model.Question;
import com.interview.model.Question.Difficulty;
import com.interview.model.Question.QuestionType;
import com.interview.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 题目数据访问对象
 * 处理题目相关的数据库操作（PostgreSQL 版本）
 */
public class QuestionDao {
    
    /**
     * 创建题目表
     */
    public void createTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS questions (
                id SERIAL PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                content TEXT NOT NULL,
                answer TEXT,
                type VARCHAR(50) NOT NULL,
                difficulty VARCHAR(20) NOT NULL,
                category VARCHAR(100),
                created_by VARCHAR(50),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP,
                active BOOLEAN DEFAULT TRUE
            )
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * 插入新题目
     */
    public boolean insert(Question question) throws SQLException {
        String sql = """
            INSERT INTO questions (title, content, answer, type, difficulty, category, created_by, created_at, updated_at, active)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, question.getTitle());
            pstmt.setString(2, question.getContent());
            pstmt.setString(3, question.getAnswer());
            pstmt.setString(4, question.getType().name());
            pstmt.setString(5, question.getDifficulty().name());
            pstmt.setString(6, question.getCategory());
            pstmt.setString(7, question.getCreatedBy());
            pstmt.setTimestamp(8, Timestamp.valueOf(question.getCreatedAt()));
            pstmt.setTimestamp(9, question.getUpdatedAt() != null ? 
                    Timestamp.valueOf(question.getUpdatedAt()) : null);
            pstmt.setBoolean(10, question.isActive());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    question.setId(rs.getInt(1));
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 根据ID查找题目
     */
    public Question findById(int id) throws SQLException {
        String sql = "SELECT * FROM questions WHERE id = ? AND active = TRUE";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToQuestion(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * 获取所有题目
     */
    public List<Question> findAll() throws SQLException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE active = TRUE ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                questions.add(mapResultSetToQuestion(rs));
            }
        }
        return questions;
    }
    
    /**
     * 根据条件搜索题目
     */
    public List<Question> search(String keyword, QuestionType type, 
                                  Difficulty difficulty, String category) throws SQLException {
        List<Question> questions = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM questions WHERE active = TRUE");
        List<Object> params = new ArrayList<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (title ILIKE ? OR content ILIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        
        if (type != null) {
            sql.append(" AND type = ?");
            params.add(type.name());
        }
        
        if (difficulty != null) {
            sql.append(" AND difficulty = ?");
            params.add(difficulty.name());
        }
        
        if (category != null && !category.isEmpty()) {
            sql.append(" AND category = ?");
            params.add(category);
        }
        
        sql.append(" ORDER BY created_at DESC");
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    questions.add(mapResultSetToQuestion(rs));
                }
            }
        }
        return questions;
    }
    
    /**
     * 随机抽取题目（PostgreSQL 使用 RANDOM()）
     */
    public List<Question> extractRandom(int count, QuestionType type, 
                                         Difficulty difficulty, String category) throws SQLException {
        List<Question> questions = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM questions WHERE active = TRUE");
        List<Object> params = new ArrayList<>();
        
        if (type != null) {
            sql.append(" AND type = ?");
            params.add(type.name());
        }
        
        if (difficulty != null) {
            sql.append(" AND difficulty = ?");
            params.add(difficulty.name());
        }
        
        if (category != null && !category.isEmpty()) {
            sql.append(" AND category = ?");
            params.add(category);
        }
        
        // PostgreSQL 使用 RANDOM() 函数
        sql.append(" ORDER BY RANDOM() LIMIT ?");
        params.add(count);
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    questions.add(mapResultSetToQuestion(rs));
                }
            }
        }
        return questions;
    }
    
    /**
     * 更新题目
     */
    public boolean update(Question question) throws SQLException {
        String sql = """
            UPDATE questions 
            SET title = ?, content = ?, answer = ?, type = ?, difficulty = ?, 
                category = ?, updated_at = CURRENT_TIMESTAMP, active = ?
            WHERE id = ?
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, question.getTitle());
            pstmt.setString(2, question.getContent());
            pstmt.setString(3, question.getAnswer());
            pstmt.setString(4, question.getType().name());
            pstmt.setString(5, question.getDifficulty().name());
            pstmt.setString(6, question.getCategory());
            pstmt.setBoolean(7, question.isActive());
            pstmt.setInt(8, question.getId());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 删除题目（逻辑删除）
     */
    public boolean delete(int id) throws SQLException {
        String sql = "UPDATE questions SET active = FALSE WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 物理删除题目
     */
    public boolean hardDelete(int id) throws SQLException {
        String sql = "DELETE FROM questions WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 获取所有分类
     */
    public List<String> findAllCategories() throws SQLException {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM questions WHERE active = TRUE AND category IS NOT NULL";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        }
        return categories;
    }
    
    /**
     * 统计题目数量
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM questions WHERE active = TRUE";
        
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
     * 将 ResultSet 映射到 Question 对象
     */
    private Question mapResultSetToQuestion(ResultSet rs) throws SQLException {
        Question question = new Question();
        question.setId(rs.getInt("id"));
        question.setTitle(rs.getString("title"));
        question.setContent(rs.getString("content"));
        question.setAnswer(rs.getString("answer"));
        question.setType(QuestionType.valueOf(rs.getString("type")));
        question.setDifficulty(Difficulty.valueOf(rs.getString("difficulty")));
        question.setCategory(rs.getString("category"));
        question.setCreatedBy(rs.getString("created_by"));
        question.setActive(rs.getBoolean("active"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            question.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            question.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return question;
    }
}
