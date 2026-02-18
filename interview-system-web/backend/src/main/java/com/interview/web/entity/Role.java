package com.interview.web.entity;

/**
 * 用户角色枚举 - 复用原 JavaFX 版本
 */
public enum Role {
    
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
    
    EXAMINER("考官", new Permission[]{
        Permission.QUESTION_READ,
        Permission.QUESTION_EXTRACT,
        Permission.VOICE_RECORD,
        Permission.VIEW_RECORDS
    }),
    
    QUESTION_CREATOR("试题编制者", new Permission[]{
        Permission.QUESTION_CREATE,
        Permission.QUESTION_READ,
        Permission.QUESTION_UPDATE,
        Permission.QUESTION_DELETE
    }),
    
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
    
    public boolean hasPermission(Permission permission) {
        for (Permission p : permissions) {
            if (p == permission) {
                return true;
            }
        }
        return false;
    }
    
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
