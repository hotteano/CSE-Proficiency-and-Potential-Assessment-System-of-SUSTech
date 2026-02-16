package com.interview.service;

import com.interview.dao.UserDao;
import com.interview.model.Permission;
import com.interview.model.Role;
import com.interview.model.User;
import com.interview.util.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

/**
 * 认证服务类
 * 处理用户注册、登录、权限验证等业务逻辑
 */
public class AuthService {
    
    private final UserDao userDao;
    private User currentUser;  // 当前登录用户
    
    public AuthService() {
        this.userDao = new UserDao();
    }
    
    /**
     * 用户注册
     * 
     * @param username 用户名
     * @param password 明文密码
     * @param realName 真实姓名
     * @param email 邮箱
     * @param role 角色
     * @return 注册结果消息
     */
    public String register(String username, String password, String realName, 
                          String email, Role role) {
        // 验证输入
        if (username == null || username.trim().isEmpty()) {
            return "用户名不能为空";
        }
        if (password == null || password.length() < 6) {
            return "密码长度至少为6位";
        }
        if (realName == null || realName.trim().isEmpty()) {
            return "真实姓名不能为空";
        }
        if (role == null) {
            return "请选择用户角色";
        }
        
        // 检查用户名是否已存在
        try {
            if (userDao.existsByUsername(username)) {
                return "用户名已存在";
            }
            
            // 对密码进行哈希处理
            String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(12));
            
            // 创建用户
            User user = new User(username, passwordHash, realName, email, role);
            
            if (userDao.insert(user)) {
                return "注册成功";
            } else {
                return "注册失败，请稍后重试";
            }
            
        } catch (SQLException e) {
            return "注册失败: " + e.getMessage();
        }
    }
    
    /**
     * 用户登录
     * 
     * @param username 用户名
     * @param password 明文密码
     * @return 登录结果消息
     */
    public String login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return "用户名不能为空";
        }
        if (password == null || password.isEmpty()) {
            return "密码不能为空";
        }
        
        try {
            User user = userDao.findByUsername(username);
            
            if (user == null) {
                return "用户名或密码错误";
            }
            
            if (!user.isActive()) {
                return "账号已被禁用，请联系管理员";
            }
            
            // 验证密码
            if (!BCrypt.checkpw(password, user.getPasswordHash())) {
                return "用户名或密码错误";
            }
            
            // 更新最后登录时间
            userDao.updateLastLogin(user.getId());
            
            // 设置当前用户
            this.currentUser = user;
            
            return "登录成功";
            
        } catch (SQLException e) {
            return "登录失败: " + e.getMessage();
        }
    }
    
    /**
     * 用户登出
     */
    public void logout() {
        this.currentUser = null;
    }
    
    /**
     * 获取当前登录用户
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * 检查是否有用户登录
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * 检查当前用户是否有指定权限
     */
    public boolean hasPermission(Permission permission) {
        return currentUser != null && currentUser.hasPermission(permission);
    }
    
    /**
     * 检查当前用户是否为管理员
     */
    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }
    
    /**
     * 修改密码
     * 
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改结果消息
     */
    public String changePassword(String oldPassword, String newPassword) {
        if (currentUser == null) {
            return "请先登录";
        }
        
        if (newPassword == null || newPassword.length() < 6) {
            return "新密码长度至少为6位";
        }
        
        try {
            // 验证旧密码
            User user = userDao.findById(currentUser.getId());
            if (!BCrypt.checkpw(oldPassword, user.getPasswordHash())) {
                return "旧密码错误";
            }
            
            // 更新密码
            String newPasswordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
            if (userDao.updatePassword(currentUser.getId(), newPasswordHash)) {
                // 更新当前用户的密码哈希
                currentUser.setPasswordHash(newPasswordHash);
                return "密码修改成功";
            } else {
                return "密码修改失败";
            }
            
        } catch (SQLException e) {
            return "密码修改失败: " + e.getMessage();
        }
    }
    
    /**
     * 重置用户密码（仅管理员可用）
     * 
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 重置结果消息
     */
    public String resetPassword(int userId, String newPassword) {
        if (!isAdmin()) {
            return "权限不足，只有管理员可以重置密码";
        }
        
        if (newPassword == null || newPassword.length() < 6) {
            return "新密码长度至少为6位";
        }
        
        try {
            String newPasswordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
            if (userDao.updatePassword(userId, newPasswordHash)) {
                return "密码重置成功";
            } else {
                return "密码重置失败，用户不存在";
            }
            
        } catch (SQLException e) {
            return "密码重置失败: " + e.getMessage();
        }
    }
}
