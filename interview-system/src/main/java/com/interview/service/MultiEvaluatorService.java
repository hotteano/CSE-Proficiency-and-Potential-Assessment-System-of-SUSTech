package com.interview.service;

import com.interview.dao.EvaluationScoreDao;
import com.interview.dao.EvaluationSummaryDao;
import com.interview.model.*;

import java.sql.SQLException;
import java.util.*;

/**
 * 多评委评分服务
 * 处理多评委评分的汇总计算和主评委调整
 */
public class MultiEvaluatorService {
    
    private final EvaluationScoreDao scoreDao;
    private final EvaluationSummaryDao summaryDao;
    private final AuthService authService;
    
    public MultiEvaluatorService(AuthService authService) {
        this.scoreDao = new EvaluationScoreDao();
        this.summaryDao = new EvaluationSummaryDao();
        this.authService = authService;
    }
    
    /**
     * 提交评委评分（带角色标识）
     * 
     * @param record 面试记录
     * @param scores 各维度分数
     * @param comments 评语
     * @param reasoning 评分理由
     * @param role 评委角色（主评委/普通评委）
     * @return 提交结果消息
     */
    public String submitEvaluatorScore(InterviewRecord record, 
                                        Map<EvaluationDimension, Double> scores,
                                        String comments, 
                                        String reasoning,
                                        EvaluatorRole role) {
        if (!authService.hasPermission(Permission.VIEW_RECORDS)) {
            return "权限不足";
        }
        
        if (record == null) {
            return "面试记录不能为空";
        }
        
        // 检查是否已有该评委的评分
        try {
            List<EvaluationScore> existingScores = scoreDao.findByInterviewRecordId(record.getId());
            String currentUser = authService.getCurrentUser().getUsername();
            
            for (EvaluationScore existing : existingScores) {
                if (existing.getScoreType() == EvaluationScore.ScoreType.HUMAN &&
                    currentUser.equals(existing.getEvaluatorUsername())) {
                    return "您已经提交过评分，如需修改请联系管理员";
                }
            }
        } catch (SQLException e) {
            System.err.println("[MultiEvaluatorService] 检查现有评分失败: " + e.getMessage());
        }
        
        // 创建评分记录
        EvaluationScore score = new EvaluationScore();
        score.setInterviewRecordId(record.getId());
        score.setCandidateUsername(record.getCandidateUsername());
        score.setEvaluatorUsername(authService.getCurrentUser().getUsername());
        score.setScoreType(EvaluationScore.ScoreType.HUMAN);
        score.setComments(comments);
        score.setReasoning(reasoning);
        score.setSubmitted(true);
        
        // 将 Double 分数转换为 Integer
        for (Map.Entry<EvaluationDimension, Double> entry : scores.entrySet()) {
            score.setDimensionScore(entry.getKey(), entry.getValue().intValue());
        }
        
        try {
            if (scoreDao.insert(score)) {
                // 重新计算汇总
                recalculateSummary(record.getId());
                
                String roleLabel = role.isLead() ? "【主评委】" : "【普通评委】";
                return roleLabel + " 评分提交成功";
            } else {
                return "评分提交失败";
            }
        } catch (SQLException e) {
            return "提交失败: " + e.getMessage();
        }
    }
    
    /**
     * 主评委调整最终分数
     * 
     * @param interviewRecordId 面试记录ID
     * @param finalScores 主评委调整后的最终分数
     * @param adjustmentReason 调整理由
     * @return 调整结果消息
     */
    public String adjustFinalScores(int interviewRecordId, 
                                     Map<EvaluationDimension, Integer> finalScores,
                                     String adjustmentReason) {
        if (!authService.hasPermission(Permission.VIEW_RECORDS)) {
            return "权限不足";
        }
        
        // 检查是否为主评委（简化处理：任何有权限的用户都可以调整，实际应该检查角色）
        String currentUser = authService.getCurrentUser().getUsername();
        
        try {
            EvaluationSummary summary = summaryDao.findByInterviewRecordId(interviewRecordId);
            if (summary == null) {
                return "未找到汇总记录，请先提交评分";
            }
            
            // 设置最终分数
            for (Map.Entry<EvaluationDimension, Integer> entry : finalScores.entrySet()) {
                summary.setFinalDimensionScore(entry.getKey(), entry.getValue());
            }
            
            // 重新计算总分
            double weightedScore = summary.calculateWeightedScore(finalScores);
            summary.setRawTotalScore(weightedScore);
            summary.setNormalizedTotalScore(summary.calculateNormalizedScore(weightedScore));
            summary.setGradeLevel(summary.determineGradeLevel(weightedScore));
            
            // 记录调整信息
            String existingComments = summary.getSummaryComments();
            String adjustmentNote = String.format("\n【主评委 %s 调整】%s", currentUser, adjustmentReason);
            summary.setSummaryComments((existingComments != null ? existingComments : "") + adjustmentNote);
            
            summary.setLeadEvaluatorUsername(currentUser);
            summary.setUpdatedAt(java.time.LocalDateTime.now());
            
            if (summaryDao.save(summary)) {
                return "最终分数调整成功";
            } else {
                return "调整失败";
            }
            
        } catch (SQLException e) {
            return "调整失败: " + e.getMessage();
        }
    }
    
    /**
     * 重新计算多评委汇总
     * 
     * @param interviewRecordId 面试记录ID
     */
    public void recalculateSummary(int interviewRecordId) {
        try {
            // 获取该面试记录的所有评委评分
            List<EvaluationScore> humanScores = scoreDao.findHumanScoresByInterview(interviewRecordId);
            EvaluationScore aiScore = scoreDao.findAIScoreByInterview(interviewRecordId);
            
            if (humanScores.isEmpty()) {
                System.out.println("[MultiEvaluatorService] 暂无评委评分，跳过汇总计算");
                return;
            }
            
            // 获取面试记录信息
            InterviewRecord record = getInterviewRecord(interviewRecordId);
            if (record == null) {
                System.err.println("[MultiEvaluatorService] 未找到面试记录: " + interviewRecordId);
                return;
            }
            
            // 创建或更新汇总记录
            EvaluationSummary summary = summaryDao.findByInterviewRecordId(interviewRecordId);
            if (summary == null) {
                summary = new EvaluationSummary();
                summary.setInterviewRecordId(interviewRecordId);
                summary.setCandidateUsername(record.getCandidateUsername());
            }
            
            // 计算各维度统计
            Map<EvaluationDimension, List<Integer>> dimensionScores = new HashMap<>();
            for (EvaluationDimension dim : EvaluationDimension.values()) {
                dimensionScores.put(dim, new ArrayList<>());
            }
            
            // 收集所有评委的分数
            for (EvaluationScore score : humanScores) {
                for (EvaluationDimension dim : EvaluationDimension.values()) {
                    Integer dimScore = score.getDimensionScore(dim);
                    if (dimScore != null && dimScore > 0) {
                        dimensionScores.get(dim).add(dimScore);
                    }
                }
            }
            
            // 计算均分和标准差
            for (EvaluationDimension dim : EvaluationDimension.values()) {
                List<Integer> scores = dimensionScores.get(dim);
                if (!scores.isEmpty()) {
                    double average = scores.stream().mapToInt(Integer::intValue).average().orElse(0);
                    double stdDev = calculateStdDev(scores, average);
                    
                    summary.setDimensionAverage(dim, average);
                    summary.setDimensionStdDev(dim, stdDev);
                }
            }
            
            // 保存大模型评分（作为参考）
            if (aiScore != null) {
                for (EvaluationDimension dim : EvaluationDimension.values()) {
                    Integer aiDimScore = aiScore.getDimensionScore(dim);
                    if (aiDimScore != null && aiDimScore > 0) {
                        summary.getAiDimensionScores().put(dim, aiDimScore);
                    }
                }
            }
            
            // 计算原始总分（使用均分计算）
            Map<EvaluationDimension, Integer> averageScores = new HashMap<>();
            for (EvaluationDimension dim : EvaluationDimension.values()) {
                averageScores.put(dim, (int) Math.round(summary.getDimensionAverage(dim)));
            }
            double rawTotal = summary.calculateWeightedScore(averageScores);
            summary.setRawTotalScore(rawTotal);
            summary.setNormalizedTotalScore(summary.calculateNormalizedScore(rawTotal));
            summary.setGradeLevel(summary.determineGradeLevel(rawTotal));
            
            // 设置评委信息
            summary.setEvaluatorCount(humanScores.size());
            StringBuilder evaluatorNames = new StringBuilder();
            for (int i = 0; i < humanScores.size(); i++) {
                if (i > 0) evaluatorNames.append(", ");
                evaluatorNames.append(humanScores.get(i).getEvaluatorUsername());
            }
            summary.setEvaluatorUsernames(evaluatorNames.toString());
            
            // 生成汇总评语
            String summaryComment = generateSummaryComment(summary, humanScores.size());
            summary.setSummaryComments(summaryComment);
            
            // 保存汇总
            summaryDao.save(summary);
            
            System.out.println("[MultiEvaluatorService] 汇总计算完成: " + summary);
            
        } catch (SQLException e) {
            System.err.println("[MultiEvaluatorService] 汇总计算失败: " + e.getMessage());
        }
    }
    
    /**
     * 计算标准差
     */
    private double calculateStdDev(List<Integer> scores, double mean) {
        if (scores.size() < 2) {
            return 0;
        }
        double sumSquaredDiff = scores.stream()
            .mapToDouble(s -> Math.pow(s - mean, 2))
            .sum();
        return Math.sqrt(sumSquaredDiff / scores.size());
    }
    
    /**
     * 生成汇总评语
     */
    private String generateSummaryComment(EvaluationSummary summary, int evaluatorCount) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("共有 %d 位评委参与评分。\n", evaluatorCount));
        sb.append(String.format("综合得分: %.2f 分，等级: %s\n\n", 
            summary.getNormalizedTotalScore(), summary.getGradeLevel()));
        
        sb.append("各维度评分情况:\n");
        for (EvaluationDimension dim : EvaluationDimension.values()) {
            double avg = summary.getDimensionAverage(dim);
            String consistency = summary.getConsistencyLevel(dim);
            sb.append(String.format("- %s: %.1f 分 (%s)\n", 
                dim.getDisplayName(), avg, consistency));
        }
        
        // 添加大模型参考
        if (!summary.getAiDimensionScores().isEmpty()) {
            sb.append("\n大模型参考评分:\n");
            double aiTotal = summary.calculateWeightedScore(summary.getAiDimensionScores());
            sb.append(String.format("大模型综合得分: %.2f 分\n", aiTotal));
        }
        
        return sb.toString();
    }
    
    /**
     * 获取面试记录（简化实现，实际应该调用 InterviewRecordService）
     */
    private InterviewRecord getInterviewRecord(int interviewRecordId) {
        // 这里简化处理，实际应该通过 InterviewRecordDao 查询
        InterviewRecord record = new InterviewRecord();
        record.setId(interviewRecordId);
        return record;
    }
    
    /**
     * 获取某面试记录的汇总结果
     */
    public EvaluationSummary getSummary(int interviewRecordId) {
        try {
            return summaryDao.findByInterviewRecordId(interviewRecordId);
        } catch (SQLException e) {
            System.err.println("[MultiEvaluatorService] 获取汇总失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取某面试记录的所有评委评分
     */
    public List<EvaluationScore> getEvaluatorScores(int interviewRecordId) {
        try {
            return scoreDao.findHumanScoresByInterview(interviewRecordId);
        } catch (SQLException e) {
            System.err.println("[MultiEvaluatorService] 获取评委评分失败: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 获取评分统计信息
     */
    public Map<String, Object> getScoreStatistics(int interviewRecordId) {
        Map<String, Object> stats = new HashMap<>();
        
        List<EvaluationScore> scores = getEvaluatorScores(interviewRecordId);
        EvaluationSummary summary = getSummary(interviewRecordId);
        
        stats.put("evaluatorCount", scores.size());
        stats.put("hasSummary", summary != null);
        
        if (summary != null) {
            stats.put("averageScore", summary.getNormalizedTotalScore());
            stats.put("gradeLevel", summary.getGradeLevel());
            
            // 计算一致性评级
            int highConsistency = 0, mediumConsistency = 0, lowConsistency = 0;
            for (EvaluationDimension dim : EvaluationDimension.values()) {
                String level = summary.getConsistencyLevel(dim);
                switch (level) {
                    case "高度一致" -> highConsistency++;
                    case "较为一致" -> mediumConsistency++;
                    default -> lowConsistency++;
                }
            }
            stats.put("highConsistencyCount", highConsistency);
            stats.put("mediumConsistencyCount", mediumConsistency);
            stats.put("lowConsistencyCount", lowConsistency);
        }
        
        return stats;
    }
}
