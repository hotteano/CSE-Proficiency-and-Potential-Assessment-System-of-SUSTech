package com.interview.service;

import com.interview.model.*;

import java.util.*;

/**
 * 大模型分析服务模拟实现
 * 用于演示和测试，实际使用时应替换为真实的AI API调用
 */
public class MockAIAnalysisService implements AIAnalysisService {
    
    private final Random random = new Random();
    
    @Override
    public EvaluationScore analyzeInterview(InterviewRecord record, List<Question> questions) {
        EvaluationScore aiScore = new EvaluationScore();
        aiScore.setInterviewRecordId(record.getId());
        aiScore.setCandidateUsername(record.getCandidateUsername());
        aiScore.setScoreType(EvaluationScore.ScoreType.AI);
        
        // 模拟评分：随机生成60-95之间的分数
        for (EvaluationDimension dim : EvaluationDimension.values()) {
            int score = 60 + random.nextInt(36); // 60-95
            aiScore.setDimensionScore(dim, score);
        }
        
        // 生成评语
        double totalScore = aiScore.calculateWeightedScore();
        aiScore.setComments(generateOverallComment(totalScore));
        aiScore.setReasoning(generateReasoning(aiScore.getDimensionScores()));
        aiScore.setSuggestions(generateAISuggestions(aiScore));
        
        return aiScore;
    }
    
    @Override
    public QuestionAnalysisResult analyzeAnswer(Question question, String answerText) {
        QuestionAnalysisResult result = new QuestionAnalysisResult();
        
        // 根据题目关联的维度进行评分
        List<EvaluationDimension> dimensions = question.getEvaluationDimensions();
        if (dimensions == null || dimensions.isEmpty()) {
            dimensions = Arrays.asList(EvaluationDimension.PROGRAMMING_SKILL);
        }
        
        for (EvaluationDimension dim : dimensions) {
            int score = 65 + random.nextInt(31);
            result.getDimensionScores().put(dim, score);
        }
        
        result.setAnalysis("回答展现了一定的理解和思考，逻辑较为清晰，但在深度和广度上还有提升空间。");
        result.setFeedback("建议进一步深入理解核心概念，并尝试从多角度思考问题。");
        result.getKeyPoints().add("理解基本概念");
        result.getKeyPoints().add("逻辑表达清晰");
        
        return result;
    }
    
    @Override
    public EvaluationReport generateReport(EvaluationScore humanScore, EvaluationScore aiScore, InterviewRecord record) {
        EvaluationReport report = new EvaluationReport();
        report.setInterviewRecordId(record.getId());
        report.setCandidateUsername(record.getCandidateUsername());
        report.setHumanScore(humanScore);
        report.setAiScore(aiScore);
        
        // 计算综合分数
        report.calculateCompositeScores();
        
        // 生成发展建议
        report.generateSuggestions();
        
        // 分析适合岗位
        report.getSuitablePositions().addAll(
            analyzeSuitablePositions(report.getCategoryScores())
        );
        
        // 生成总体评价
        report.setOverallComment(generateOverallReportComment(report));
        
        return report;
    }
    
    @Override
    public List<String> generateSuggestions(List<EvaluationDimension> dimensions) {
        List<String> suggestions = new ArrayList<>();
        
        Map<String, List<String>> suggestionMap = new HashMap<>();
        suggestionMap.put("编程", Arrays.asList(
            "建议多参与实际项目开发，积累工程经验",
            "深入学习数据结构与算法，提升代码效率",
            "阅读优秀开源项目代码，学习最佳实践"
        ));
        suggestionMap.put("研究", Arrays.asList(
            "培养学术阅读习惯，关注领域前沿动态",
            "尝试撰写技术博客或论文，锻炼表达能力",
            "参与科研项目，培养研究思维"
        ));
        suggestionMap.put("沟通", Arrays.asList(
            "多参与技术分享和演讲，提升表达能力",
            "练习结构化思维，使论述更有条理",
            "学习倾听和反馈技巧，增强沟通效果"
        ));
        suggestionMap.put("数学", Arrays.asList(
            "系统学习离散数学和线性代数等基础课程",
            "多做算法题，锻炼数学建模思维",
            "阅读数学证明，学习严谨的推理方法"
        ));
        suggestionMap.put("商业", Arrays.asList(
            "关注行业动态，培养商业敏感度",
            "学习产品设计方法论，如用户研究、原型设计",
            "了解开源社区运营模式，参与开源贡献"
        ));
        
        for (EvaluationDimension dim : dimensions) {
            String category = dim.getCategory().name();
            List<String> catSuggestions = suggestionMap.getOrDefault(category, 
                Collections.singletonList("建议持续学习和练习，提升" + dim.getDisplayName()));
            
            if (!catSuggestions.isEmpty()) {
                suggestions.add(catSuggestions.get(random.nextInt(catSuggestions.size())));
            }
        }
        
        return suggestions;
    }
    
    @Override
    public List<String> analyzeSuitablePositions(Map<EvaluationDimension.Category, Double> categoryScores) {
        List<String> positions = new ArrayList<>();
        
        Double skillScore = categoryScores.get(EvaluationDimension.Category.SKILL);
        Double researchScore = categoryScores.get(EvaluationDimension.Category.RESEARCH);
        Double businessScore = categoryScores.get(EvaluationDimension.Category.BUSINESS);
        Double mathScore = categoryScores.get(EvaluationDimension.Category.MATHEMATICS);
        
        if (skillScore != null && skillScore >= 80) {
            positions.add("后端开发工程师");
            positions.add("系统架构师");
        }
        if (researchScore != null && researchScore >= 80) {
            positions.add("算法研究员");
            positions.add("研究科学家");
        }
        if (businessScore != null && businessScore >= 80) {
            positions.add("产品经理");
            positions.add("技术项目经理");
        }
        if (mathScore != null && mathScore >= 80) {
            positions.add("算法工程师");
            positions.add("数据科学家");
        }
        
        if (positions.isEmpty()) {
            positions.add("初级开发工程师");
            positions.add("技术支持工程师");
        }
        
        return positions;
    }
    
    @Override
    public String transcribeVoice(String voiceFilePath) {
        // 模拟语音转文本
        return "[语音转录模拟] 这是从语音文件转换的文本内容...";
    }
    
    // 辅助方法：生成总体评语
    private String generateOverallComment(double totalScore) {
        if (totalScore >= 90) {
            return "表现出色，具有优秀的计算机科学综合能力和潜力。在多个维度上都展现了扎实的基础和良好的思维习惯。";
        } else if (totalScore >= 80) {
            return "表现良好，具备较强的技术能力和发展潜力。在核心技能方面有较好的掌握，部分维度还有提升空间。";
        } else if (totalScore >= 70) {
            return "表现中等，具备基本的计算机科学素养。建议在专业深度和广度上持续学习和提升。";
        } else {
            return "表现有待提升，基础能力尚需加强。建议系统性地学习基础知识，并通过实践项目积累经验。";
        }
    }
    
    // 辅助方法：生成评分理由
    private String generateReasoning(Map<EvaluationDimension, Integer> scores) {
        StringBuilder sb = new StringBuilder();
        sb.append("基于对面试者回答的分析，评分理由如下：\n\n");
        
        // 找出最高和最低的三个维度
        List<Map.Entry<EvaluationDimension, Integer>> sorted = scores.entrySet().stream()
                .sorted(Map.Entry.<EvaluationDimension, Integer>comparingByValue().reversed())
                .toList();
        
        sb.append("【优势维度】\n");
        for (int i = 0; i < Math.min(3, sorted.size()); i++) {
            Map.Entry<EvaluationDimension, Integer> entry = sorted.get(i);
            sb.append("- ").append(entry.getKey().getDisplayName())
              .append(": ").append(entry.getValue()).append("分\n");
        }
        
        sb.append("\n【待提升维度】\n");
        for (int i = sorted.size() - 1; i >= Math.max(0, sorted.size() - 3); i--) {
            Map.Entry<EvaluationDimension, Integer> entry = sorted.get(i);
            sb.append("- ").append(entry.getKey().getDisplayName())
              .append(": ").append(entry.getValue()).append("分\n");
        }
        
        return sb.toString();
    }
    
    // 辅助方法：生成AI建议
    private String generateAISuggestions(EvaluationScore score) {
        double totalScore = score.calculateWeightedScore();
        
        if (totalScore >= 85) {
            return "建议挑战更高难度的项目和研究课题，考虑参与开源社区或学术会议。";
        } else if (totalScore >= 75) {
            return "建议在保持现有水平的基础上，针对薄弱环节进行针对性提升，多参与实际项目。";
        } else {
            return "建议系统性地复习基础知识，从简单的项目开始积累经验，逐步提升能力。";
        }
    }
    
    // 辅助方法：生成报告总体评价
    private String generateOverallReportComment(EvaluationReport report) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("本报告基于");
        if (report.getHumanScore() != null && report.getAiScore() != null) {
            sb.append("评委评分和大模型分析的交叉验证，");
        } else if (report.getHumanScore() != null) {
            sb.append("评委专业评分，");
        } else {
            sb.append("大模型智能分析，");
        }
        
        sb.append("对面试者在计算机科学领域的能力和潜力进行了全面评测。\n\n");
        
        sb.append("综合评价：").append(report.getGrade().getDescription()).append("\n\n");
        
        if (!report.getStrengths().isEmpty()) {
            sb.append("面试者的优势在于：");
            for (int i = 0; i < report.getStrengths().size(); i++) {
                if (i > 0) sb.append("、");
                sb.append(report.getStrengths().get(i).getDisplayName());
            }
            sb.append("等方面，展现了较好的专业素养。\n\n");
        }
        
        if (!report.getWeaknesses().isEmpty()) {
            sb.append("建议在以下方面进一步提升：");
            for (int i = 0; i < report.getWeaknesses().size(); i++) {
                if (i > 0) sb.append("、");
                sb.append(report.getWeaknesses().get(i).getDisplayName());
            }
            sb.append("，以形成更全面的能力体系。");
        }
        
        return sb.toString();
    }
}
