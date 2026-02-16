package com.interview.model;

/**
 * 用户角色枚举
 * 定义系统中四种不同的用户角色及其权限
 */
public enum Role {
    
    /**
     * 管理员：拥有所有权限
     * - 用户管理（创建、删除、修改用户）
     * - 题目管理（增删改查）
     * - 系统配置
     * - 题目抽取
     * - 查看面试记录
     */
    ADMIN("管理员", new Permission[]{
        Permission.USER_MANAGE,
        Permission.QUESTION_CREATE,
        Permission.QUESTION_READ,
        Permission.QUESTION_UPDATE,
        Permission.QUESTION_DELETE,
        Permission.QUESTION_EXTRACT,
        Permission.VOICE_RECORD,
        Permission.VIEW_RECORDS,
        Permission.SYSTEM_CONFIG
    }),
    
    /**
     * 考官：读取权限，可以抽取题目，查看面试记录
     * - 查看题目
     * - 抽取题目
     * - 查看面试记录
     */
    EXAMINER("考官", new Permission[]{
        Permission.QUESTION_READ,
        Permission.QUESTION_EXTRACT,
        Permission.VOICE_RECORD,
        Permission.VIEW_RECORDS
    }),
    
    /**
     * 试题编制者：题目管理权限
     * - 创建题目
     * - 修改题目
     * - 删除题目
     * - 查看题目
     */
    QUESTION_CREATOR("试题编制者", new Permission[]{
        Permission.QUESTION_CREATE,
        Permission.QUESTION_READ,
        Permission.QUESTION_UPDATE,
        Permission.QUESTION_DELETE
    }),
    
    /**
     * 考生：面试参与者，仅可查看自己的面试记录和录入语音
     * - 录入语音文件（面试过程中）
     * - 查看自己的面试记录
     * 无抽取题目权限，无修改权限
     */
    CANDIDATE("考生", new Permission[]{
        Permission.VOICE_RECORD,
        Permission.VIEW_OWN_RECORDS
    });
    
    private final String displayName;
    private final Permission[] permissions;
    
    Role(String displayName, Permission[] permissions) {
        this.displayName = displayName;
        this.permissions = permissions;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public Permission[] getPermissions() {
        return permissions;
    }
    
    /**
     * 检查角色是否拥有指定权限
     */
    public boolean hasPermission(Permission permission) {
        for (Permission p : permissions) {
            if (p == permission) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 根据角色名称获取角色
     */
    public static Role fromString(String roleName) {
        for (Role role : values()) {
            if (role.name().equalsIgnoreCase(roleName) || 
                role.getDisplayName().equals(roleName)) {
                return role;
            }
        }
        throw new IllegalArgumentException("未知的角色: " + roleName);
    }
}
