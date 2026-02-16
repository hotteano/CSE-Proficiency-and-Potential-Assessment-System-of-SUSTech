package com.interview.model;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 存储用户的基本信息
 */
public class User {
    
    private Integer id;              // 用户ID
    private String username;         // 用户名
    private String passwordHash;     // 密码哈希（不存储明文密码）
    private String realName;         // 真实姓名
    private String email;            // 邮箱
    private Role role;               // 用户角色
    private boolean active;          // 账号是否激活
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime lastLogin; // 最后登录时间
    
    // 构造方法
    public User() {}
    
    public User(String username, String passwordHash, String realName, 
                String email, Role role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.realName = realName;
        this.email = email;
        this.role = role;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public String getRealName() {
        return realName;
    }
    
    public void setRealName(String realName) {
        this.realName = realName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    /**
     * 检查用户是否有指定权限
     */
    public boolean hasPermission(Permission permission) {
        return role != null && role.hasPermission(permission);
    }
    
    /**
     * 获取角色显示名称
     */
    public String getRoleDisplayName() {
        return role != null ? role.getDisplayName() : "";
    }
    
    @Override
    public String toString() {
        return String.format("User{id=%d, username='%s', realName='%s', role=%s}", 
                id, username, realName, role);
    }
}
