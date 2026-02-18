package com.interview.web.entity;

/**
 * 权限枚举 - 复用原 JavaFX 版本
 */
public enum Permission {
    
    USER_MANAGE("用户管理", "创建、删除、修改用户信息"),
    
    QUESTION_CREATE("创建题目", "添加新的面试题目"),
    QUESTION_READ("查看题目", "查看题目内容和详情"),
    QUESTION_UPDATE("修改题目", "修改已有题目内容"),
    QUESTION_DELETE("删除题目", "删除题目"),
    QUESTION_EXTRACT("抽取题目", "随机抽取面试题目"),
    
    VOICE_RECORD("语音录入", "录入面试语音文件"),
    VIEW_RECORDS("查看所有面试记录", "查看所有考生的面试记录"),
    VIEW_OWN_RECORDS("查看自己的面试记录", "考生只能查看自己的面试记录"),
    
    SYSTEM_CONFIG("系统配置", "配置数据库连接等系统设置");
    
    private final String displayName;
    private final String description;
    
    Permission(String displayName, String description) {
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
