package com.interview.service;

import com.interview.dao.EvaluationScoreDao;
import com.interview.model.*;

import java.sql.SQLException;
import java.util.List;

/**
 * 评测服务层
 * 处理评分相关的业务逻辑
 */
public class EvaluationService {
    
    private final EvaluationScoreDao scoreDao;
    private final AIAnalysisService aiService;
    private final AuthService authService;
    
    public EvaluationService(AuthService authService) {
        this.scoreDao = new EvaluationScoreDao();
        this.aiService = new MockAIAnalysisService(); // 使用模拟实现
        this.authService = authService;
    }
    
    /**
     * 创建评分记录（评委评分）
     */
    public String createHumanScore(EvaluationScore score) {
        if (!authService.hasPermission(com.interview.model.Permission.VIEW_RECORDS)) {
            return "权限不足";
        }
        
        score.setScoreType(EvaluationScore.ScoreType.HUMAN);
        score.setEvaluatorUsername(authService.getCurrentUser().getUsername());
        
        try {
            if (scoreDao.insert(score)) {
                return "评分记录创建成功";
            } else {
                return "评分记录创建失败";
            }
        } catch (SQLException e) {
            return "创建失败: " + e.getMessage();
        }
    }
    
    /**
     * 提交或更新评委评分
     */
    public String submitHumanScore(EvaluationScore score) {
        if (!authService.hasPermission(com.interview.model.Permission.VIEW_RECORDS)) {
            return "权限不足";
        }
        
        score.setSubmitted(true);
        
        try {
            if (score.getId() == null) {
                // 新评分
                score.setScoreType(EvaluationScore.ScoreType.HUMAN);
                score.setEvaluatorUsername(authService.getCurrentUser().getUsername());
                if (scoreDao.insert(score)) {
                    return "评分提交成功";
                }
            } else {
                // 更新评分
                if (scoreDao.update(score)) {
                    return "评分更新成功";
                }
            }
            return "评分提交失败";
        } catch (SQLException e) {
            return "提交失败: " + e.getMessage();
        }
    }
    
    /**
     * 触发大模型分析
     */
    public String triggerAIAnalysis(int interviewRecordId, InterviewRecord record, List<Question> questions) {
        if (!authService.isAdmin()) {
            return "只有管理员可以触发大模型分析";
        }
        
        try {
            // 检查是否已有AI评分
            EvaluationScore existingAI = scoreDao.findAIScoreByInterview(interviewRecordId);
            if (existingAI != null) {
                return "该面试记录已有大模型评分";
            }
            
            // 调用AI服务分析
            EvaluationScore aiScore = aiService.analyzeInterview(record, questions);
            aiScore.setInterviewRecordId(interviewRecordId);
            aiScore.setSubmitted(true);
            
            if (scoreDao.insert(aiScore)) {
                return "大模型分析完成";
            } else {
                return "大模型分析保存失败";
            }
        } catch (SQLException e) {
            return "分析失败: " + e.getMessage();
        }
    }
    
    /**
     * 获取面试记录的所有评分
     */
    public List<EvaluationScore> getScoresByInterview(int interviewRecordId) {
        try {
            return scoreDao.findByInterviewRecordId(interviewRecordId);
        } catch (SQLException e) {
            System.err.println("获取评分失败: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 获取面试记录的评委评分
     */
    public EvaluationScore getHumanScore(int interviewRecordId) {
        try {
            return scoreDao.findHumanScoreByInterview(interviewRecordId);
        } catch (SQLException e) {
            System.err.println("获取评委评分失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取面试记录的大模型评分
     */
    public EvaluationScore getAIScore(int interviewRecordId) {
        try {
            return scoreDao.findAIScoreByInterview(interviewRecordId);
        } catch (SQLException e) {
            System.err.println("获取AI评分失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 生成综合评测报告
     */
    public EvaluationReport generateReport(int interviewRecordId, InterviewRecord record) {
        EvaluationScore humanScore = getHumanScore(interviewRecordId);
        EvaluationScore aiScore = getAIScore(interviewRecordId);
        
        // 至少需要一种评分
        if (humanScore == null && aiScore == null) {
            return null;
        }
        
        return aiService.generateReport(humanScore, aiScore, record);
    }
    
    /**
     * 获取所有维度
     */
    public EvaluationDimension[] getAllDimensions() {
        return EvaluationDimension.values();
    }
    
    /**
     * 获取维度分类
     */
    public EvaluationDimension.Category[] getAllCategories() {
        return EvaluationDimension.Category.values();
    }
    
    /**
     * 根据分类获取维度
     */
    public List<EvaluationDimension> getDimensionsByCategory(EvaluationDimension.Category category) {
        return EvaluationDimension.getByCategory(category);
    }
}
