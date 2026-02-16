package com.interview.util;

import com.interview.config.DatabaseConfig;
import com.interview.dao.InterviewRecordDao;
import com.interview.dao.QuestionDao;
import com.interview.dao.UserDao;
import com.interview.model.Role;
import com.interview.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库初始化类
 * 负责创建数据库表结构和初始数据（PostgreSQL 版本）
 */
public class DatabaseInitializer {
    
    private final UserDao userDao;
    private final QuestionDao questionDao;
    private final InterviewRecordDao recordDao;
    
    public DatabaseInitializer() {
        this.userDao = new UserDao();
        this.questionDao = new QuestionDao();
        this.recordDao = new InterviewRecordDao();
    }
    
    /**
     * 初始化数据库
     * 创建表结构和初始管理员账号
     */
    public void initialize() throws SQLException {
        System.out.println("正在初始化 PostgreSQL 数据库...");
        
        // 创建用户表
        userDao.createTable();
        System.out.println("用户表创建完成");
        
        // 创建题目表
        questionDao.createTable();
        System.out.println("题目表创建完成");
        
        // 创建面试记录表
        recordDao.createTable();
        System.out.println("面试记录表创建完成");
        
        // 创建初始管理员账号（如果不存在）
        createDefaultAdmin();
        
        // 创建示例考生账号
        createSampleCandidate();
        
        System.out.println("数据库初始化完成");
    }
    
    /**
     * 创建默认管理员账号
     * 默认账号: admin / admin123
     */
    private void createDefaultAdmin() {
        try {
            // 检查是否已有管理员账号
            if (userDao.existsByUsername("admin")) {
                System.out.println("管理员账号已存在");
                return;
            }
            
            // 创建管理员账号
            String passwordHash = BCrypt.hashpw("admin123", BCrypt.gensalt(12));
            User admin = new User("admin", passwordHash, "系统管理员", 
                                   "admin@interview.com", Role.ADMIN);
            
            if (userDao.insert(admin)) {
                System.out.println("默认管理员账号创建成功");
                System.out.println("用户名: admin");
                System.out.println("密码: admin123");
                System.out.println("请及时修改默认密码！");
            }
            
        } catch (SQLException e) {
            System.err.println("创建默认管理员账号失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建示例考生账号
     * 默认账号: candidate / candidate123
     */
    private void createSampleCandidate() {
        try {
            // 检查是否已存在
            if (userDao.existsByUsername("candidate")) {
                return;
            }
            
            // 创建考生账号
            String passwordHash = BCrypt.hashpw("candidate123", BCrypt.gensalt(12));
            User candidate = new User("candidate", passwordHash, "示例考生", 
                                      "candidate@interview.com", Role.CANDIDATE);
            
            if (userDao.insert(candidate)) {
                System.out.println("示例考生账号创建成功");
                System.out.println("用户名: candidate");
                System.out.println("密码: candidate123");
            }
            
        } catch (SQLException e) {
            System.err.println("创建示例考生账号失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试数据库连接
     */
    public static boolean testConnection(String host, int port, 
                                          String database, String username, String password) {
        try {
            // 设置测试配置
            DatabaseConfig.setPgHost(host);
            DatabaseConfig.setPgPort(port);
            DatabaseConfig.setPgDatabase(database);
            DatabaseConfig.setPgCredentials(username, password);
            
            // 测试连接
            return DatabaseConnection.testConnection();
            
        } catch (Exception e) {
            System.err.println("数据库连接测试失败: " + e.getMessage());
            return false;
        }
    }
}
