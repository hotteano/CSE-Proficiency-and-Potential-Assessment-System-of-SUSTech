package com.interview.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 评测分数记录实体类
 * 存储一次评测的完整评分信息（评委评分 + 大模型评分）
 */
public class EvaluationScore {
    
    private Integer id;
    private Integer interviewRecordId;  // 关联的面试记录ID
    private String candidateUsername;   // 考生用户名
    private String evaluatorUsername;   // 评委用户名（如果是大模型评分则为null）
    private ScoreType scoreType;        // 评分类型（评委/大模型）
    
    // 各维度分数（1-100分）
    private Map<EvaluationDimension, Integer> dimensionScores;
    
    // 评委评语/大模型分析
    private String comments;
    
    // 评分理由（评委填写/大模型生成）
    private String reasoning;
    
    // 发展建议
    private String suggestions;
    
    // 评分时间
    private LocalDateTime scoredAt;
    
    // 是否已提交
    private boolean submitted;
    
    /**
     * 评分类型
     */
    public enum ScoreType {
        HUMAN("评委评分"),
        AI("大模型评分"),
        HYBRID("综合评分");
        
        private final String displayName;
        
        ScoreType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public EvaluationScore() {
        this.dimensionScores = new HashMap<>();
        this.scoredAt = LocalDateTime.now();
        this.submitted = false;
        // 初始化所有维度分数为0
        for (EvaluationDimension dim : EvaluationDimension.values()) {
            this.dimensionScores.put(dim, 0);
        }
    }
    
    // 计算综合分数（加权平均）
    public double calculateWeightedScore() {
        if (dimensionScores.isEmpty()) {
            return 0.0;
        }
        
        double totalScore = 0;
        int totalWeight = 0;
        
        for (Map.Entry<EvaluationDimension, Integer> entry : dimensionScores.entrySet()) {
            int weight = entry.getKey().getDefaultWeight();
            totalScore += entry.getValue() * weight;
            totalWeight += weight;
        }
        
        return totalWeight > 0 ? totalScore / totalWeight : 0.0;
    }
    
    // 计算某个大分类的分数
    public double calculateCategoryScore(EvaluationDimension.Category category) {
        double totalScore = 0;
        int totalWeight = 0;
        
        for (Map.Entry<EvaluationDimension, Integer> entry : dimensionScores.entrySet()) {
            if (entry.getKey().getCategory() == category) {
                int weight = entry.getKey().getDefaultWeight();
                totalScore += entry.getValue() * weight;
                totalWeight += weight;
            }
        }
        
        return totalWeight > 0 ? totalScore / totalWeight : 0.0;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getInterviewRecordId() {
        return interviewRecordId;
    }
    
    public void setInterviewRecordId(Integer interviewRecordId) {
        this.interviewRecordId = interviewRecordId;
    }
    
    public String getCandidateUsername() {
        return candidateUsername;
    }
    
    public void setCandidateUsername(String candidateUsername) {
        this.candidateUsername = candidateUsername;
    }
    
    public String getEvaluatorUsername() {
        return evaluatorUsername;
    }
    
    public void setEvaluatorUsername(String evaluatorUsername) {
        this.evaluatorUsername = evaluatorUsername;
    }
    
    public ScoreType getScoreType() {
        return scoreType;
    }
    
    public void setScoreType(ScoreType scoreType) {
        this.scoreType = scoreType;
    }
    
    public Map<EvaluationDimension, Integer> getDimensionScores() {
        return dimensionScores;
    }
    
    public void setDimensionScores(Map<EvaluationDimension, Integer> dimensionScores) {
        this.dimensionScores = dimensionScores;
    }
    
    public void setDimensionScore(EvaluationDimension dimension, Integer score) {
        this.dimensionScores.put(dimension, score);
    }
    
    public Integer getDimensionScore(EvaluationDimension dimension) {
        return this.dimensionScores.getOrDefault(dimension, 0);
    }
    
    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }
    
    public String getReasoning() {
        return reasoning;
    }
    
    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }
    
    public String getSuggestions() {
        return suggestions;
    }
    
    public void setSuggestions(String suggestions) {
        this.suggestions = suggestions;
    }
    
    public LocalDateTime getScoredAt() {
        return scoredAt;
    }
    
    public void setScoredAt(LocalDateTime scoredAt) {
        this.scoredAt = scoredAt;
    }
    
    public boolean isSubmitted() {
        return submitted;
    }
    
    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }
    
    public String getScoreTypeDisplayName() {
        return scoreType != null ? scoreType.getDisplayName() : "";
    }
    
    @Override
    public String toString() {
        return String.format("EvaluationScore{id=%d, candidate='%s', type=%s, score=%.2f}", 
                id, candidateUsername, scoreType, calculateWeightedScore());
    }
}
