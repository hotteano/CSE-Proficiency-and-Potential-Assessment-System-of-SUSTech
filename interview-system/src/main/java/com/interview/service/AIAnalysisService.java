package com.interview.service;

import com.interview.model.*;

import java.util.*;

/**
 * 大模型分析服务接口
 * 用于分析面试者表现并生成评分
 */
public interface AIAnalysisService {
    
    /**
     * 分析面试记录并生成评分
     * 
     * @param record 面试记录（包含语音文件路径等）
     * @param questions 面试题目列表
     * @return 大模型评分结果
     */
    EvaluationScore analyzeInterview(InterviewRecord record, List<Question> questions);
    
    /**
     * 分析单个问题的回答
     * 
     * @param question 题目
     * @param answerText 回答文本（从语音转录或文本输入）
     * @return 该问题的分析结果和评分
     */
    QuestionAnalysisResult analyzeAnswer(Question question, String answerText);
    
    /**
     * 生成综合评价报告
     * 
     * @param humanScore 评委评分（可为null）
     * @param aiScore 大模型评分
     * @param record 面试记录
     * @return 综合评测报告
     */
    EvaluationReport generateReport(EvaluationScore humanScore, EvaluationScore aiScore, InterviewRecord record);
    
    /**
     * 生成发展建议
     * 
     * @param dimensions 需要提升的维度
     * @return 针对性的发展建议列表
     */
    List<String> generateSuggestions(List<EvaluationDimension> dimensions);
    
    /**
     * 分析面试者适合的岗位方向
     * 
     * @param categoryScores 各分类得分
     * @return 适合的岗位列表
     */
    List<String> analyzeSuitablePositions(Map<EvaluationDimension.Category, Double> categoryScores);
    
    /**
     * 语音转文本（模拟）
     * 
     * @param voiceFilePath 语音文件路径
     * @return 转录文本
     */
    String transcribeVoice(String voiceFilePath);
}

/**
 * 问题分析结果
 */
class QuestionAnalysisResult {
    private Map<EvaluationDimension, Integer> dimensionScores;
    private String analysis;
    private String feedback;
    private List<String> keyPoints;
    
    public QuestionAnalysisResult() {
        this.dimensionScores = new HashMap<>();
        this.keyPoints = new ArrayList<>();
    }
    
    // Getters and Setters
    public Map<EvaluationDimension, Integer> getDimensionScores() { return dimensionScores; }
    public void setDimensionScores(Map<EvaluationDimension, Integer> dimensionScores) { this.dimensionScores = dimensionScores; }
    
    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }
    
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    
    public List<String> getKeyPoints() { return keyPoints; }
    public void setKeyPoints(List<String> keyPoints) { this.keyPoints = keyPoints; }
}
