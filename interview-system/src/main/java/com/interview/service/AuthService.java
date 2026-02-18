package com.interview.service;

import com.interview.config.DatabaseConfig.DbUserRole;
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
 * 支持按角色使用不同的数据库连接（SSL/HTTPS）
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
            // 使用管理员连接进行用户创建操作
            DatabaseConnection.setCurrentDbRole(DbUserRole.ADMIN);
            
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
     * 登录成功后，会根据用户角色设置对应的数据库连接
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
            // 登录时先使用管理员连接验证用户
            DatabaseConnection.setCurrentDbRole(DbUserRole.ADMIN);
            
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
            
            // 根据用户角色设置数据库连接角色
            // 这将使用对应角色的数据库用户通过 SSL/HTTPS 连接
            DbUserRole dbRole = DbUserRole.fromAppRole(user.getRole());
            DatabaseConnection.setCurrentDbRole(dbRole);
            
            System.out.println("用户 " + username + " 登录成功，使用数据库角色: " + dbRole.getDisplayName());
            
            return "登录成功";
            
        } catch (SQLException e) {
            return "登录失败: " + e.getMessage();
        }
    }
    
    /**
     * 用户登出
     * 清除当前用户和数据库角色设置
     */
    public void logout() {
        if (currentUser != null) {
            System.out.println("用户 " + currentUser.getUsername() + " 登出");
        }
        this.currentUser = null;
        // 关闭当前连接并清除角色设置
        DatabaseConnection.closeAllConnections();
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
     * 获取当前用户对应的数据库角色
     */
    public DbUserRole getCurrentDbRole() {
        if (currentUser == null) {
            return DbUserRole.CANDIDATE;
        }
        return DbUserRole.fromAppRole(currentUser.getRole());
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
            // 使用管理员连接进行密码修改
            DatabaseConnection.setCurrentDbRole(DbUserRole.ADMIN);
            
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
            // 使用管理员连接
            DatabaseConnection.setCurrentDbRole(DbUserRole.ADMIN);
            
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
    
    /**
     * 临时切换到管理员角色执行操作（仅当前用户是管理员时）
     * 操作完成后需要手动恢复原来的角色
     */
    public boolean switchToAdminRole() {
        if (!isAdmin()) {
            return false;
        }
        DatabaseConnection.setCurrentDbRole(DbUserRole.ADMIN);
        return true;
    }
    
    /**
     * 恢复当前用户的数据库角色
     */
    public void restoreUserRole() {
        if (currentUser != null) {
            DatabaseConnection.setRoleByAppRole(currentUser.getRole());
        }
    }
}
