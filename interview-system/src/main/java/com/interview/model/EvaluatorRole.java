package com.interview.model;

/**
 * 评委角色枚举
 * 区分主评委和普通评委
 */
public enum EvaluatorRole {
    
    LEAD("主评委", "负责协调评分工作，拥有调整权"),
    NORMAL("普通评委", "独立评分，参与汇总计算");
    
    private final String displayName;
    private final String description;
    
    EvaluatorRole(String displayName, String description) {
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
     * 判断是否为主评委
     */
    public boolean isLead() {
        return this == LEAD;
    }
}
