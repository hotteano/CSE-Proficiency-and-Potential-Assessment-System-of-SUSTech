package com.interview.model;

import java.time.LocalDateTime;

/**
 * 题目实体类
 * 存储面试题目的详细信息
 */
public class Question {
    
    private Integer id;              // 题目ID
    private String title;            // 题目标题/简述
    private String content;          // 题目内容（详细描述）
    private String answer;           // 参考答案
    private QuestionType type;       // 题目类型
    private Difficulty difficulty;   // 难度等级
    private String category;         // 分类/标签
    private String createdBy;        // 创建者用户名
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 最后更新时间
    private boolean active;          // 是否启用
    
    // 题目类型枚举
    public enum QuestionType {
        TECHNICAL("技术题", "编程语言、算法、数据结构等技术相关问题"),
        BEHAVIORAL("行为题", "团队协作、沟通能力、问题解决等行为相关问题"),
        SCENARIO("场景题", "给定场景下的分析和解决方案"),
        ALGORITHM("算法题", "算法设计和编码实现"),
        SYSTEM_DESIGN("系统设计题", "系统架构和设计问题"),
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
    
    // 难度等级枚举
    public enum Difficulty {
        EASY("简单", 1),
        MEDIUM("中等", 2),
        HARD("困难", 3),
        EXPERT("专家", 4);
        
        private final String displayName;
        private final int level;
        
        Difficulty(String displayName, int level) {
            this.displayName = displayName;
            this.level = level;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getLevel() {
            return level;
        }
    }
    
    // 构造方法
    public Question() {
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }
    
    public Question(String title, String content, String answer, 
                    QuestionType type, Difficulty difficulty, String category) {
        this();
        this.title = title;
        this.content = content;
        this.answer = answer;
        this.type = type;
        this.difficulty = difficulty;
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
    
    public Difficulty getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
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
    
    /**
     * 获取类型显示名称
     */
    public String getTypeDisplayName() {
        return type != null ? type.getDisplayName() : "";
    }
    
    /**
     * 获取难度显示名称
     */
    public String getDifficultyDisplayName() {
        return difficulty != null ? difficulty.getDisplayName() : "";
    }
    
    @Override
    public String toString() {
        return String.format("Question{id=%d, title='%s', type=%s, difficulty=%s}", 
                id, title, type, difficulty);
    }
}
