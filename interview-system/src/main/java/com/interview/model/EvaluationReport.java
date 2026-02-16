package com.interview.model;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 综合评测报告实体类
 * 整合评委评分和大模型评分，生成最终评测报告
 */
public class EvaluationReport {
    
    private Integer id;
    private Integer interviewRecordId;
    private String candidateUsername;
    private String candidateName;
    
    // 评分来源
    private EvaluationScore humanScore;      // 评委评分
    private EvaluationScore aiScore;         // 大模型评分
    
    // 综合分数（加权融合评委和大模型评分）
    private double humanWeight;              // 评委评分权重（默认0.6）
    private double aiWeight;                 // 大模型评分权重（默认0.4）
    
    // 综合各维度分数
    private Map<EvaluationDimension, Double> compositeDimensionScores;
    
    // 各分类综合分数
    private Map<EvaluationDimension.Category, Double> categoryScores;
    
    // 总分
    private double totalScore;
    
    // 评级
    private Grade grade;
    
    // 排名百分比（相对于所有考生）
    private double percentileRank;
    
    // 优势维度
    private List<EvaluationDimension> strengths;
    
    // 待提升维度
    private List<EvaluationDimension> weaknesses;
    
    // 综合评价
    private String overallComment;
    
    // 发展建议
    private List<String> developmentSuggestions;
    
    // 适合的岗位方向
    private List<String> suitablePositions;
    
    // 报告生成时间
    private LocalDateTime generatedAt;
    
    /**
     * 评级枚举
     */
    public enum Grade {
        EXCELLENT("优秀", "S", 90, 100, "具有出色的计算机科学能力和潜力，强烈推荐"),
        GOOD("良好", "A", 80, 89, "具有较强的能力，推荐录取"),
        AVERAGE("中等", "B", 70, 79, "具备基础能力，有发展潜力"),
        BELOW_AVERAGE("待提升", "C", 60, 69, "能力有待提高，需要针对性培养"),
        POOR("需加强", "D", 0, 59, "基础较薄弱，建议加强学习");
        
        private final String displayName;
        private final String level;
        private final int minScore;
        private final int maxScore;
        private final String description;
        
        Grade(String displayName, String level, int minScore, int maxScore, String description) {
            this.displayName = displayName;
            this.level = level;
            this.minScore = minScore;
            this.maxScore = maxScore;
            this.description = description;
        }
        
        public static Grade fromScore(double score) {
            for (Grade grade : values()) {
                if (score >= grade.minScore && score <= grade.maxScore) {
                    return grade;
                }
            }
            return POOR;
        }
        
        public String getDisplayName() { return displayName; }
        public String getLevel() { return level; }
        public int getMinScore() { return minScore; }
        public int getMaxScore() { return maxScore; }
        public String getDescription() { return description; }
    }
    
    public EvaluationReport() {
        this.compositeDimensionScores = new HashMap<>();
        this.categoryScores = new HashMap<>();
        this.strengths = new ArrayList<>();
        this.weaknesses = new ArrayList<>();
        this.developmentSuggestions = new ArrayList<>();
        this.suitablePositions = new ArrayList<>();
        this.generatedAt = LocalDateTime.now();
        this.humanWeight = 0.6;
        this.aiWeight = 0.4;
    }
    
    /**
     * 计算综合评分
     */
    public void calculateCompositeScores() {
        if (humanScore == null && aiScore == null) {
            return;
        }
        
        // 计算各维度综合分数
        for (EvaluationDimension dim : EvaluationDimension.values()) {
            double humanDimScore = humanScore != null ? 
                    humanScore.getDimensionScore(dim) * humanWeight : 0;
            double aiDimScore = aiScore != null ? 
                    aiScore.getDimensionScore(dim) * aiWeight : 0;
            
            // 如果只有一方评分，使用那一方
            if (humanScore == null) {
                compositeDimensionScores.put(dim, (double) aiScore.getDimensionScore(dim));
            } else if (aiScore == null) {
                compositeDimensionScores.put(dim, (double) humanScore.getDimensionScore(dim));
            } else {
                compositeDimensionScores.put(dim, humanDimScore + aiDimScore);
            }
        }
        
        // 计算各分类分数
        for (EvaluationDimension.Category category : EvaluationDimension.Category.values()) {
            double totalScore = 0;
            int totalWeight = 0;
            
            for (EvaluationDimension dim : EvaluationDimension.getByCategory(category)) {
                Double score = compositeDimensionScores.get(dim);
                if (score != null) {
                    int weight = dim.getDefaultWeight();
                    totalScore += score * weight;
                    totalWeight += weight;
                }
            }
            
            if (totalWeight > 0) {
                categoryScores.put(category, totalScore / totalWeight);
            }
        }
        
        // 计算总分
        this.totalScore = categoryScores.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        
        // 确定评级
        this.grade = Grade.fromScore(this.totalScore);
        
        // 分析优势和待提升维度
        analyzeStrengthsAndWeaknesses();
    }
    
    /**
     * 分析优势和待提升维度
     */
    private void analyzeStrengthsAndWeaknesses() {
        List<Map.Entry<EvaluationDimension, Double>> sortedDims = 
                compositeDimensionScores.entrySet().stream()
                        .sorted(Map.Entry.<EvaluationDimension, Double>comparingByValue().reversed())
                        .toList();
        
        // 前3名为优势
        int topCount = Math.min(3, sortedDims.size());
        for (int i = 0; i < topCount; i++) {
            if (sortedDims.get(i).getValue() >= 80) {
                strengths.add(sortedDims.get(i).getKey());
            }
        }
        
        // 后3名为待提升
        int bottomCount = Math.min(3, sortedDims.size());
        for (int i = sortedDims.size() - 1; i >= sortedDims.size() - bottomCount; i--) {
            if (sortedDims.get(i).getValue() < 70) {
                weaknesses.add(sortedDims.get(i).getKey());
            }
        }
    }
    
    /**
     * 生成发展建议
     */
    public void generateSuggestions() {
        developmentSuggestions.clear();
        
        // 根据评级生成总体建议
        switch (grade) {
            case EXCELLENT -> {
                developmentSuggestions.add("您已经具备优秀的计算机科学能力，建议挑战更高难度的项目和研究课题");
                developmentSuggestions.add("考虑参与开源项目或学术会议，扩大影响力");
            }
            case GOOD -> {
                developmentSuggestions.add("继续保持学习的热情，深入钻研感兴趣的领域");
                developmentSuggestions.add("建议加强实际项目经验，提升工程能力");
            }
            case AVERAGE -> {
                developmentSuggestions.add("建议系统性地补充基础知识，打牢根基");
                developmentSuggestions.add("多参与实践项目，将理论知识应用到实际中");
            }
            case BELOW_AVERAGE, POOR -> {
                developmentSuggestions.add("建议从基础课程开始，循序渐进地提升能力");
                developmentSuggestions.add("寻找导师指导，制定针对性的学习计划");
            }
        }
        
        // 针对待提升维度给出具体建议
        for (EvaluationDimension dim : weaknesses) {
            developmentSuggestions.add("在" + dim.getDisplayName() + "方面需要加强练习，建议多阅读相关资料和案例");
        }
    }
    
    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Integer getInterviewRecordId() { return interviewRecordId; }
    public void setInterviewRecordId(Integer interviewRecordId) { this.interviewRecordId = interviewRecordId; }
    
    public String getCandidateUsername() { return candidateUsername; }
    public void setCandidateUsername(String candidateUsername) { this.candidateUsername = candidateUsername; }
    
    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }
    
    public EvaluationScore getHumanScore() { return humanScore; }
    public void setHumanScore(EvaluationScore humanScore) { this.humanScore = humanScore; }
    
    public EvaluationScore getAiScore() { return aiScore; }
    public void setAiScore(EvaluationScore aiScore) { this.aiScore = aiScore; }
    
    public double getTotalScore() { return totalScore; }
    public void setTotalScore(double totalScore) { this.totalScore = totalScore; }
    
    public Grade getGrade() { return grade; }
    public void setGrade(Grade grade) { this.grade = grade; }
    
    public double getPercentileRank() { return percentileRank; }
    public void setPercentileRank(double percentileRank) { this.percentileRank = percentileRank; }
    
    public List<EvaluationDimension> getStrengths() { return strengths; }
    public List<EvaluationDimension> getWeaknesses() { return weaknesses; }
    
    public String getOverallComment() { return overallComment; }
    public void setOverallComment(String overallComment) { this.overallComment = overallComment; }
    
    public List<String> getDevelopmentSuggestions() { return developmentSuggestions; }
    public List<String> getSuitablePositions() { return suitablePositions; }
    
    public Map<EvaluationDimension.Category, Double> getCategoryScores() { return categoryScores; }
    public Map<EvaluationDimension, Double> getCompositeDimensionScores() { return compositeDimensionScores; }
    
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    
    public double getHumanWeight() { return humanWeight; }
    public void setHumanWeight(double humanWeight) { this.humanWeight = humanWeight; }
    
    public double getAiWeight() { return aiWeight; }
    public void setAiWeight(double aiWeight) { this.aiWeight = aiWeight; }
    
    public String getGradeDisplayName() {
        return grade != null ? grade.getDisplayName() : "";
    }
    
    public String getGradeLevel() {
        return grade != null ? grade.getLevel() : "";
    }
}
