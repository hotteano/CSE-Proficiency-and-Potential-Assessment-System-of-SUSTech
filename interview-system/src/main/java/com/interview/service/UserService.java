package com.interview.service;

import com.interview.dao.UserDao;
import com.interview.model.Permission;
import com.interview.model.Role;
import com.interview.model.User;

import java.sql.SQLException;
import java.util.List;

/**
 * 用户服务类
 * 处理用户管理的业务逻辑
 */
public class UserService {
    
    private final UserDao userDao;
    private final AuthService authService;
    
    public UserService(AuthService authService) {
        this.userDao = new UserDao();
        this.authService = authService;
    }
    
    /**
     * 获取所有用户
     * 需要 USER_MANAGE 权限
     */
    public List<User> getAllUsers() {
        if (!authService.hasPermission(Permission.USER_MANAGE)) {
            return List.of();
        }
        
        try {
            return userDao.findAll();
        } catch (SQLException e) {
            System.err.println("获取用户列表失败: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 根据ID获取用户
     * 需要 USER_MANAGE 权限
     */
    public User getUserById(int id) {
        if (!authService.hasPermission(Permission.USER_MANAGE)) {
            return null;
        }
        
        try {
            return userDao.findById(id);
        } catch (SQLException e) {
            System.err.println("获取用户失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 更新用户信息
     * 需要 USER_MANAGE 权限
     */
    public String updateUser(User user) {
        if (!authService.hasPermission(Permission.USER_MANAGE)) {
            return "权限不足";
        }
        
        if (user.getId() == null) {
            return "用户ID不能为空";
        }
        
        // 不能修改自己的角色（防止管理员意外删除自己的权限）
        User currentUser = authService.getCurrentUser();
        if (user.getId().equals(currentUser.getId()) && user.getRole() != currentUser.getRole()) {
            return "不能修改自己的角色";
        }
        
        try {
            User existing = userDao.findById(user.getId());
            if (existing == null) {
                return "用户不存在";
            }
            
            if (userDao.update(user)) {
                return "用户更新成功";
            } else {
                return "用户更新失败";
            }
        } catch (SQLException e) {
            return "用户更新失败: " + e.getMessage();
        }
    }
    
    /**
     * 删除用户
     * 需要 USER_MANAGE 权限
     */
    public String deleteUser(int userId) {
        if (!authService.hasPermission(Permission.USER_MANAGE)) {
            return "权限不足";
        }
        
        // 不能删除自己
        User currentUser = authService.getCurrentUser();
        if (userId == currentUser.getId()) {
            return "不能删除当前登录的账号";
        }
        
        try {
            User existing = userDao.findById(userId);
            if (existing == null) {
                return "用户不存在";
            }
            
            if (userDao.delete(userId)) {
                return "用户删除成功";
            } else {
                return "用户删除失败";
            }
        } catch (SQLException e) {
            return "用户删除失败: " + e.getMessage();
        }
    }
    
    /**
     * 启用/禁用用户账号
     * 需要 USER_MANAGE 权限
     */
    public String toggleUserStatus(int userId) {
        if (!authService.hasPermission(Permission.USER_MANAGE)) {
            return "权限不足";
        }
        
        // 不能禁用自己
        User currentUser = authService.getCurrentUser();
        if (userId == currentUser.getId()) {
            return "不能禁用当前登录的账号";
        }
        
        try {
            User user = userDao.findById(userId);
            if (user == null) {
                return "用户不存在";
            }
            
            user.setActive(!user.isActive());
            
            if (userDao.update(user)) {
                return user.isActive() ? "账号已启用" : "账号已禁用";
            } else {
                return "操作失败";
            }
        } catch (SQLException e) {
            return "操作失败: " + e.getMessage();
        }
    }
    
    /**
     * 获取所有角色
     */
    public Role[] getAllRoles() {
        return Role.values();
    }
}
