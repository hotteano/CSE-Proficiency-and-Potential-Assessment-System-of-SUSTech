package com.interview.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题目实体类
 * 存储面试题目的详细信息
 * 
 * 等级体系：
 * 1. 基础等级（面试环节）：初级、中级、高级
 * 2. 专精等级（仅三等，面试环节）：算法与程序设计、系统设计、商业设计、科研
 */
public class Question implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Integer id;              // 题目ID
    private String title;            // 题目标题/简述
    private String content;          // 题目内容（详细描述）
    private String answer;           // 参考答案
    private QuestionType type;       // 题目类型
    private QuestionLevel level;     // 题目等级（基础等级或专精三等）
    private SpecializationType specialization; // 专精类型（如果是专精题）
    private String category;         // 分类/标签
    private String createdBy;        // 创建者用户名
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 最后更新时间
    private boolean active;          // 是否启用
    
    // 评测维度（一道题可考察多个维度）
    private java.util.List<EvaluationDimension> evaluationDimensions;
    
    // 题目设计意图描述
    private String designIntent;
    
    // 评分参考标准（评委/AI评分的参考依据）
    private String scoringCriteria;
    
    /**
     * 题目类型枚举
     */
    public enum QuestionType {
        TECHNICAL("技术题", "编程语言、数据结构、系统设计等技术相关问题"),
        ALGORITHM("算法题", "算法设计和编码实现"),
        SYSTEM_DESIGN("系统设计题", "系统架构和设计问题"),
        RESEARCH("科研题", "研究思路、创新能力和学术潜力评估"),
        BUSINESS("商业设计题", "产品设计、商业模式和市场分析"),
        BEHAVIORAL("行为题", "团队协作、沟通能力、问题解决等行为相关问题"),
        SCENARIO("场景题", "给定场景下的分析和解决方案"),
        OTHER("其他", "其他类型题目");
        
        private final String displayName;
        private final String description;
        
        QuestionType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 题目等级枚举
     * 基础等级：初级、中级、高级
     * 专精等级：仅保留三等（面试环节）
     */
    public enum QuestionLevel {
        // 基础等级
        BASIC("初级", "基础等级", 1, false),
        INTERMEDIATE("中级", "基础等级", 2, false),
        ADVANCED("高级", "基础等级", 3, false),
        
        // 专精三等（仅面试环节）
        SPECIALIZATION_THREE("专精三等", "专精等级", 4, true);
        
        private final String displayName;
        private final String category;
        private final int level;
        private final boolean isSpecialization;
        
        QuestionLevel(String displayName, String category, int level, boolean isSpecialization) {
            this.displayName = displayName;
            this.category = category;
            this.level = level;
            this.isSpecialization = isSpecialization;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getCategory() {
            return category;
        }
        
        public int getLevel() {
            return level;
        }
        
        public boolean isSpecialization() {
            return isSpecialization;
        }
        
        /**
         * 获取基础等级列表
         */
        public static QuestionLevel[] getBasicLevels() {
            return new QuestionLevel[] { BASIC, INTERMEDIATE, ADVANCED };
        }
        
        /**
         * 获取专精等级（仅三等）
         */
        public static QuestionLevel getSpecializationLevel() {
            return SPECIALIZATION_THREE;
        }
    }
    
    /**
     * 专精类型枚举
     * 仅用于专精三等题目分类
     */
    public enum SpecializationType {
        NONE("无", "基础等级题目"),
        ALGORITHM("算法与程序设计", "算法设计、数据结构、编程能力"),
        SYSTEM_DESIGN("系统设计", "系统架构、模块设计、性能优化"),
        BUSINESS("商业设计", "产品设计、商业模式、市场分析"),
        RESEARCH("科研", "研究思路、创新能力、学术写作");
        
        private final String displayName;
        private final String description;
        
        SpecializationType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
        
        /**
         * 获取有效的专精类型（排除 NONE）
         */
        public static SpecializationType[] getValidTypes() {
            return new SpecializationType[] { ALGORITHM, SYSTEM_DESIGN, BUSINESS, RESEARCH };
        }
    }
    
    // 构造方法
    public Question() {
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.evaluationDimensions = new java.util.ArrayList<>();
        this.specialization = SpecializationType.NONE;
    }
    
    public Question(String title, String content, String answer, 
                    QuestionType type, QuestionLevel level, String category) {
        this();
        this.title = title;
        this.content = content;
        this.answer = answer;
        this.type = type;
        this.level = level;
        this.category = category;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getAnswer() {
        return answer;
    }
    
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    
    public QuestionType getType() {
        return type;
    }
    
    public void setType(QuestionType type) {
        this.type = type;
    }
    
    public QuestionLevel getLevel() {
        return level;
    }
    
    public void setLevel(QuestionLevel level) {
        this.level = level;
    }
    
    public SpecializationType getSpecialization() {
        return specialization;
    }
    
    public void setSpecialization(SpecializationType specialization) {
        this.specialization = specialization != null ? specialization : SpecializationType.NONE;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public java.util.List<EvaluationDimension> getEvaluationDimensions() {
        return evaluationDimensions;
    }
    
    public void setEvaluationDimensions(java.util.List<EvaluationDimension> evaluationDimensions) {
        this.evaluationDimensions = evaluationDimensions;
    }
    
    public String getDesignIntent() {
        return designIntent;
    }
    
    public void setDesignIntent(String designIntent) {
        this.designIntent = designIntent;
    }
    
    public String getScoringCriteria() {
        return scoringCriteria;
    }
    
    public void setScoringCriteria(String scoringCriteria) {
        this.scoringCriteria = scoringCriteria;
    }
    
    /**
     * 获取类型显示名称
     */
    public String getTypeDisplayName() {
        return type != null ? type.getDisplayName() : "";
    }
    
    /**
     * 获取等级显示名称
     */
    public String getLevelDisplayName() {
        if (level == null) return "";
        if (level.isSpecialization() && specialization != null && specialization != SpecializationType.NONE) {
            return level.getDisplayName() + " - " + specialization.getDisplayName();
        }
        return level.getDisplayName();
    }
    
    /**
     * 获取评测维度显示名称列表
     */
    public String getDimensionsDisplayName() {
        if (evaluationDimensions == null || evaluationDimensions.isEmpty()) {
            return "未设置";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < evaluationDimensions.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(evaluationDimensions.get(i).getDisplayName());
        }
        return sb.toString();
    }
    
    /**
     * 判断是否为基础等级题目
     */
    public boolean isBasicLevel() {
        return level != null && !level.isSpecialization();
    }
    
    /**
     * 判断是否为专精等级题目
     */
    public boolean isSpecializationLevel() {
        return level != null && level.isSpecialization();
    }
    
    @Override
    public String toString() {
        return String.format("Question{id=%d, title='%s', type=%s, level=%s}", 
                id, title, type, level);
    }
}
