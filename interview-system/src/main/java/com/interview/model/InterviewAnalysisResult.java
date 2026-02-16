package com.interview.model;

import java.util.*;

/**
 * 面试分析结果
 * 存储LLM分析后的各维度评分和评语
 */
public class InterviewAnalysisResult {
    
    // 各维度分数
    private final Map<EvaluationDimension, Double> dimensionScores;
    // 各维度评语
    private final Map<EvaluationDimension, String> dimensionComments;
    // 整体评价
    private String overallComment;
    // 优势维度
    private List<String> strengths;
    // 待提升维度
    private List<String> weaknesses;
    // 发展建议
    private List<String> suggestions;
    // AI分析时间
    private Date analysisTime;
    
    public InterviewAnalysisResult() {
        this.dimensionScores = new HashMap<>();
        this.dimensionComments = new HashMap<>();
        this.strengths = new ArrayList<>();
        this.weaknesses = new ArrayList<>();
        this.suggestions = new ArrayList<>();
        this.analysisTime = new Date();
    }
    
    /**
     * 设置维度分数
     */
    public void setDimensionScore(EvaluationDimension dimension, Double score) {
        dimensionScores.put(dimension, score);
    }
    
    /**
     * 获取维度分数
     */
    public Double getDimensionScore(EvaluationDimension dimension) {
        return dimensionScores.getOrDefault(dimension, 0.0);
    }
    
    /**
     * 获取所有维度分数
     */
    public Map<EvaluationDimension, Double> getDimensionScores() {
        return dimensionScores;
    }
    
    /**
     * 设置维度评语
     */
    public void setDimensionComment(EvaluationDimension dimension, String comment) {
        dimensionComments.put(dimension, comment);
    }
    
    /**
     * 获取维度评语
     */
    public String getDimensionComment(EvaluationDimension dimension) {
        return dimensionComments.getOrDefault(dimension, "");
    }
    
    /**
     * 获取所有维度评语
     */
    public Map<EvaluationDimension, String> getDimensionComments() {
        return dimensionComments;
    }
    
    /**
     * 设置整体评价
     */
    public void setOverallComment(String comment) {
        this.overallComment = comment;
    }
    
    /**
     * 获取整体评价
     */
    public String getOverallComment() {
        return overallComment;
    }
    
    /**
     * 设置优势维度
     */
    public void setStrengths(List<String> strengths) {
        this.strengths = strengths != null ? strengths : new ArrayList<>();
    }
    
    /**
     * 获取优势维度
     */
    public List<String> getStrengths() {
        return strengths;
    }
    
    /**
     * 设置待提升维度
     */
    public void setWeaknesses(List<String> weaknesses) {
        this.weaknesses = weaknesses != null ? weaknesses : new ArrayList<>();
    }
    
    /**
     * 获取待提升维度
     */
    public List<String> getWeaknesses() {
        return weaknesses;
    }
    
    /**
     * 设置发展建议
     */
    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions != null ? suggestions : new ArrayList<>();
    }
    
    /**
     * 获取发展建议
     */
    public List<String> getSuggestions() {
        return suggestions;
    }
    
    /**
     * 设置分析时间
     */
    public void setAnalysisTime(Date analysisTime) {
        this.analysisTime = analysisTime;
    }
    
    /**
     * 获取分析时间
     */
    public Date getAnalysisTime() {
        return analysisTime;
    }
    
    /**
     * 计算总分
     */
    public double calculateTotalScore() {
        if (dimensionScores.isEmpty()) {
            return 0.0;
        }
        
        double total = 0.0;
        for (EvaluationDimension dim : EvaluationDimension.values()) {
            Double score = dimensionScores.get(dim);
            if (score != null) {
                total += score * dim.getDefaultWeight() / 100.0;
            }
        }
        return total;
    }
    
    /**
     * 计算分类得分
     */
    public Map<EvaluationDimension.Category, Double> calculateCategoryScores() {
        Map<EvaluationDimension.Category, Double> categoryScores = new HashMap<>();
        Map<EvaluationDimension.Category, Double> categoryWeights = new HashMap<>();
        
        for (EvaluationDimension dim : EvaluationDimension.values()) {
            Double score = dimensionScores.get(dim);
            if (score != null) {
                var category = dim.getCategory();
                categoryScores.merge(category, score * dim.getDefaultWeight() / 100.0, Double::sum);
                categoryWeights.merge(category, dim.getDefaultWeight() / 100.0, Double::sum);
            }
        }
        
        // 归一化
        for (var category : categoryScores.keySet()) {
            double weight = categoryWeights.getOrDefault(category, 1.0);
            if (weight > 0) {
                categoryScores.put(category, categoryScores.get(category) / weight);
            }
        }
        
        return categoryScores;
    }
    
    @Override
    public String toString() {
        return "InterviewAnalysisResult{" +
            "dimensionScores=" + dimensionScores +
            ", overallComment='" + overallComment + '\'' +
            ", strengths=" + strengths +
            ", weaknesses=" + weaknesses +
            ", suggestions=" + suggestions +
            ", analysisTime=" + analysisTime +
            '}';
    }
}
