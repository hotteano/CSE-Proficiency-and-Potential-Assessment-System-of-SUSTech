package com.interview.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 多评委评分汇总实体类
 * 存储多个评委评分的汇总计算结果
 */
public class EvaluationSummary {
    
    private Integer id;
    private Integer interviewRecordId;           // 关联的面试记录ID
    private String candidateUsername;            // 考生用户名
    
    // 各维度均分（所有评委该维度的平均分）
    private Map<EvaluationDimension, Double> dimensionAverages;
    
    // 各维度标准差（评估评委间一致性）
    private Map<EvaluationDimension, Double> dimensionStdDevs;
    
    // 大模型评分（作为参考）
    private Map<EvaluationDimension, Integer> aiDimensionScores;
    
    // 主评委调整后的最终分数
    private Map<EvaluationDimension, Integer> finalDimensionScores;
    
    // 各项平均加起来的原始总分（归一化前）
    private double rawTotalScore;
    
    // 归一化后的总分（0-100百分比）
    private double normalizedTotalScore;
    
    // 评分等级
    private String gradeLevel;
    
    // 参与评分的评委数量
    private int evaluatorCount;
    
    // 评委用户名列表（逗号分隔）
    private String evaluatorUsernames;
    
    // 主评委用户名
    private String leadEvaluatorUsername;
    
    // 汇总评语
    private String summaryComments;
    
    // 创建时间
    private LocalDateTime createdAt;
    
    // 最后更新时间
    private LocalDateTime updatedAt;
    
    public EvaluationSummary() {
        this.dimensionAverages = new HashMap<>();
        this.dimensionStdDevs = new HashMap<>();
        this.aiDimensionScores = new HashMap<>();
        this.finalDimensionScores = new HashMap<>();
        this.createdAt = LocalDateTime.now();
        this.evaluatorCount = 0;
    }
    
    /**
     * 计算加权综合分数
     */
    public double calculateWeightedScore(Map<EvaluationDimension, Integer> scores) {
        if (scores == null || scores.isEmpty()) {
            return 0.0;
        }
        
        double totalScore = 0;
        double totalWeight = 0;
        
        for (Map.Entry<EvaluationDimension, Integer> entry : scores.entrySet()) {
            double weight = entry.getKey().getDefaultWeight() / 100.0;
            totalScore += entry.getValue() * weight;
            totalWeight += weight;
        }
        
        return totalWeight > 0 ? totalScore : 0.0;
    }
    
    /**
     * 计算归一化总分（映射到0-100）
     */
    public double calculateNormalizedScore(double rawScore) {
        // 理论上最高分是100分，但多评委可能产生波动
        // 使用 sigmoid 或线性映射确保在 0-100 范围内
        return Math.max(0, Math.min(100, rawScore));
    }
    
    /**
     * 根据总分确定等级
     */
    public String determineGradeLevel(double score) {
        if (score >= 85) {
            return "A+";
        } else if (score >= 80) {
            return "A";
        } else if (score >= 75) {
            return "A-";
        } else if (score >= 70) {
            return "B+";
        } else if (score >= 65) {
            return "B";
        } else if (score >= 60) {
            return "B-";
        } else if (score >= 55) {
            return "C+";
        } else if (score >= 50) {
            return "C";
        } else {
            return "C-";
        }
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
    
    public Map<EvaluationDimension, Double> getDimensionAverages() {
        return dimensionAverages;
    }
    
    public void setDimensionAverages(Map<EvaluationDimension, Double> dimensionAverages) {
        this.dimensionAverages = dimensionAverages;
    }
    
    public void setDimensionAverage(EvaluationDimension dim, Double average) {
        this.dimensionAverages.put(dim, average);
    }
    
    public Double getDimensionAverage(EvaluationDimension dim) {
        return this.dimensionAverages.getOrDefault(dim, 0.0);
    }
    
    public Map<EvaluationDimension, Double> getDimensionStdDevs() {
        return dimensionStdDevs;
    }
    
    public void setDimensionStdDevs(Map<EvaluationDimension, Double> dimensionStdDevs) {
        this.dimensionStdDevs = dimensionStdDevs;
    }
    
    public void setDimensionStdDev(EvaluationDimension dim, Double stdDev) {
        this.dimensionStdDevs.put(dim, stdDev);
    }
    
    public Map<EvaluationDimension, Integer> getAiDimensionScores() {
        return aiDimensionScores;
    }
    
    public void setAiDimensionScores(Map<EvaluationDimension, Integer> aiDimensionScores) {
        this.aiDimensionScores = aiDimensionScores;
    }
    
    public Map<EvaluationDimension, Integer> getFinalDimensionScores() {
        return finalDimensionScores;
    }
    
    public void setFinalDimensionScores(Map<EvaluationDimension, Integer> finalDimensionScores) {
        this.finalDimensionScores = finalDimensionScores;
    }
    
    public void setFinalDimensionScore(EvaluationDimension dim, Integer score) {
        this.finalDimensionScores.put(dim, score);
    }
    
    public double getRawTotalScore() {
        return rawTotalScore;
    }
    
    public void setRawTotalScore(double rawTotalScore) {
        this.rawTotalScore = rawTotalScore;
    }
    
    public double getNormalizedTotalScore() {
        return normalizedTotalScore;
    }
    
    public void setNormalizedTotalScore(double normalizedTotalScore) {
        this.normalizedTotalScore = normalizedTotalScore;
        this.gradeLevel = determineGradeLevel(normalizedTotalScore);
    }
    
    public String getGradeLevel() {
        return gradeLevel;
    }
    
    public void setGradeLevel(String gradeLevel) {
        this.gradeLevel = gradeLevel;
    }
    
    public int getEvaluatorCount() {
        return evaluatorCount;
    }
    
    public void setEvaluatorCount(int evaluatorCount) {
        this.evaluatorCount = evaluatorCount;
    }
    
    public String getEvaluatorUsernames() {
        return evaluatorUsernames;
    }
    
    public void setEvaluatorUsernames(String evaluatorUsernames) {
        this.evaluatorUsernames = evaluatorUsernames;
    }
    
    public String getLeadEvaluatorUsername() {
        return leadEvaluatorUsername;
    }
    
    public void setLeadEvaluatorUsername(String leadEvaluatorUsername) {
        this.leadEvaluatorUsername = leadEvaluatorUsername;
    }
    
    public String getSummaryComments() {
        return summaryComments;
    }
    
    public void setSummaryComments(String summaryComments) {
        this.summaryComments = summaryComments;
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
     * 获取某维度的评委一致性评级
     */
    public String getConsistencyLevel(EvaluationDimension dim) {
        Double stdDev = dimensionStdDevs.get(dim);
        if (stdDev == null) {
            return "未知";
        }
        if (stdDev < 5) {
            return "高度一致";
        } else if (stdDev < 10) {
            return "较为一致";
        } else if (stdDev < 15) {
            return "存在分歧";
        } else {
            return "分歧较大";
        }
    }
    
    @Override
    public String toString() {
        return String.format("EvaluationSummary{id=%d, candidate='%s', evaluators=%d, score=%.2f, grade='%s'}", 
                id, candidateUsername, evaluatorCount, normalizedTotalScore, gradeLevel);
    }
}
