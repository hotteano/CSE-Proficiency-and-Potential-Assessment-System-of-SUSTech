package com.interview.util;

import com.interview.config.AppConfig;
import com.interview.config.DatabaseConfig;
import com.interview.config.DatabaseConfig.DbUserRole;
import com.interview.dao.EvaluationScoreDao;
import com.interview.dao.InterviewRecordDao;
import com.interview.dao.LLMConfigDao;
import com.interview.dao.QuestionDao;
import com.interview.dao.UserDao;
import com.interview.model.LLMConfig;
import com.interview.model.Role;
import com.interview.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

/**
 * 数据库初始化类
 * 负责创建数据库表结构和初始数据（PostgreSQL 版本）
 * 支持 SSL/HTTPS 连接和角色权限控制
 */
public class DatabaseInitializer {
    
    private final UserDao userDao;
    private final QuestionDao questionDao;
    private final InterviewRecordDao recordDao;
    private final EvaluationScoreDao scoreDao;
    private final LLMConfigDao llmConfigDao;
    
    public DatabaseInitializer() {
        this.userDao = new UserDao();
        this.questionDao = new QuestionDao();
        this.recordDao = new InterviewRecordDao();
        this.scoreDao = new EvaluationScoreDao();
        this.llmConfigDao = new LLMConfigDao();
    }
    
    /**
     * 初始化数据库
     * 创建表结构和初始管理员账号
     * 使用管理员角色执行初始化
     */
    public void initialize() throws SQLException {
        System.out.println("正在初始化 PostgreSQL 数据库...");
        System.out.println("SSL/HTTPS 连接: " + (DatabaseConfig.isSslEnabled() ? "已启用" : "未启用"));
        
        // 使用管理员角色进行初始化
        DatabaseConnection.setCurrentDbRole(DbUserRole.ADMIN);
        
        // 创建用户表
        userDao.createTable();
        System.out.println("用户表创建完成");
        
        // 创建题目表
        questionDao.createTable();
        System.out.println("题目表创建完成");
        
        // 创建面试记录表
        recordDao.createTable();
        System.out.println("面试记录表创建完成");
        
        // 创建评分记录表
        scoreDao.createTable();
        System.out.println("评分记录表创建完成");
        
        // 创建LLM配置表
        llmConfigDao.createTable();
        System.out.println("LLM配置表创建完成");
        
        // 创建默认LLM配置
        createDefaultLLMConfig();
        
        // 创建初始管理员账号（如果不存在）
        createDefaultAdmin();
        
        // 创建示例考生账号
        createSampleCandidate();
        
        // 创建示例出题人账号
        createSampleSetter();
        
        // 创建示例考官账号
        createSampleJudge();
        
        System.out.println("数据库初始化完成");
        
        // 测试所有角色连接
        testAllRoleConnections();
    }
    
    /**
     * 创建默认管理员账号
     * 默认账号: admin / admin123
     */
    private void createDefaultAdmin() {
        try {
            // 使用管理员角色
            DatabaseConnection.setCurrentDbRole(DbUserRole.ADMIN);
            
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
                System.out.println("  用户名: admin");
                System.out.println("  密码: admin123");
                System.out.println("  请及时修改默认密码！");
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
            DatabaseConnection.setCurrentDbRole(DbUserRole.ADMIN);
            
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
                System.out.println("  用户名: candidate");
                System.out.println("  密码: candidate123");
            }
            
        } catch (SQLException e) {
            System.err.println("创建示例考生账号失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建示例出题人账号
     * 默认账号: setter / setter123
     */
    private void createSampleSetter() {
        try {
            DatabaseConnection.setCurrentDbRole(DbUserRole.ADMIN);
            
            // 检查是否已存在
            if (userDao.existsByUsername("setter")) {
                return;
            }
            
            // 创建出题人账号
            String passwordHash = BCrypt.hashpw("setter123", BCrypt.gensalt(12));
            User setter = new User("setter", passwordHash, "示例出题人", 
                                   "setter@interview.com", Role.QUESTION_CREATOR);
            
            if (userDao.insert(setter)) {
                System.out.println("示例出题人账号创建成功");
                System.out.println("  用户名: setter");
                System.out.println("  密码: setter123");
            }
            
        } catch (SQLException e) {
            System.err.println("创建示例出题人账号失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建示例考官账号
     * 默认账号: judge / judge123
     */
    private void createSampleJudge() {
        try {
            DatabaseConnection.setCurrentDbRole(DbUserRole.ADMIN);
            
            // 检查是否已存在
            if (userDao.existsByUsername("judge")) {
                return;
            }
            
            // 创建考官账号
            String passwordHash = BCrypt.hashpw("judge123", BCrypt.gensalt(12));
            User judge = new User("judge", passwordHash, "示例考官", 
                                  "judge@interview.com", Role.EXAMINER);
            
            if (userDao.insert(judge)) {
                System.out.println("示例考官账号创建成功");
                System.out.println("  用户名: judge");
                System.out.println("  密码: judge123");
            }
            
        } catch (SQLException e) {
            System.err.println("创建示例考官账号失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建默认LLM配置
     */
    private void createDefaultLLMConfig() {
        try {
            DatabaseConnection.setCurrentDbRole(DbUserRole.ADMIN);
            
            // 检查是否已有配置
            if (llmConfigDao.findDefault() != null) {
                System.out.println("LLM配置已存在");
                return;
            }
            
            LLMConfig config = new LLMConfig();
            config.setName("默认-DeepSeek-Thinking");
            config.setProvider(LLMConfig.LLMProvider.DEEPSEEK_THINKING);
            config.setModelName("deepseek-reasoner");
            config.setApiEndpoint("https://api.deepseek.com/v1/chat/completions");
            config.setApiKey(""); // 用户需要自行配置
            config.setDefault(true);
            config.setEnabled(true);
            config.setTimeout(60);
            
            if (llmConfigDao.insert(config)) {
                System.out.println("默认LLM配置创建成功（请配置API Key）");
            }
            
        } catch (Exception e) {
            System.err.println("创建默认LLM配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试所有角色数据库连接
     */
    private void testAllRoleConnections() {
        System.out.println("\n正在测试各角色数据库连接...");
        DatabaseConnection.testAllConnections();
    }
    
    /**
     * 测试数据库连接（使用管理员角色）
     * @deprecated 使用 DatabaseConnection.testConnection(DbUserRole) 替代
     */
    @Deprecated
    public static boolean testConnection(String host, int port, 
                                          String database, String username, String password) {
        try {
            // 设置测试配置
            DatabaseConfig.setPgHost(host);
            DatabaseConfig.setPgPort(port);
            DatabaseConfig.setPgDatabase(database);
            DatabaseConfig.setPgCredentials(username, password);
            
            // 测试连接
            return DatabaseConnection.testConnection(DbUserRole.ADMIN);
            
        } catch (Exception e) {
            System.err.println("数据库连接测试失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试指定角色的数据库连接
     */
    public static boolean testConnection(DbUserRole role) {
        return DatabaseConnection.testConnection(role);
    }
}
