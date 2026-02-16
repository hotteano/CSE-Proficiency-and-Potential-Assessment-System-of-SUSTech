package com.interview.service;

import com.interview.dao.QuestionDao;
import com.interview.model.Permission;
import com.interview.model.Question;
import com.interview.model.Question.Difficulty;
import com.interview.model.Question.QuestionType;

import java.sql.SQLException;
import java.util.List;

/**
 * 题目服务类
 * 处理题目的增删改查和抽取等业务逻辑
 */
public class QuestionService {
    
    private final QuestionDao questionDao;
    private final AuthService authService;
    
    public QuestionService(AuthService authService) {
        this.questionDao = new QuestionDao();
        this.authService = authService;
    }
    
    /**
     * 创建新题目
     * 需要 QUESTION_CREATE 权限
     */
    public String createQuestion(Question question) {
        if (!authService.hasPermission(Permission.QUESTION_CREATE)) {
            return "权限不足，无法创建题目";
        }
        
        // 验证输入
        if (question.getTitle() == null || question.getTitle().trim().isEmpty()) {
            return "题目标题不能为空";
        }
        if (question.getContent() == null || question.getContent().trim().isEmpty()) {
            return "题目内容不能为空";
        }
        if (question.getType() == null) {
            return "请选择题目类型";
        }
        if (question.getDifficulty() == null) {
            return "请选择难度等级";
        }
        
        // 设置创建者
        question.setCreatedBy(authService.getCurrentUser().getUsername());
        
        try {
            if (questionDao.insert(question)) {
                return "题目创建成功";
            } else {
                return "题目创建失败";
            }
        } catch (SQLException e) {
            return "题目创建失败: " + e.getMessage();
        }
    }
    
    /**
     * 更新题目
     * 需要 QUESTION_UPDATE 权限
     */
    public String updateQuestion(Question question) {
        if (!authService.hasPermission(Permission.QUESTION_UPDATE)) {
            return "权限不足，无法修改题目";
        }
        
        if (question.getId() == null) {
            return "题目ID不能为空";
        }
        
        try {
            // 检查题目是否存在
            Question existing = questionDao.findById(question.getId());
            if (existing == null) {
                return "题目不存在";
            }
            
            if (questionDao.update(question)) {
                return "题目更新成功";
            } else {
                return "题目更新失败";
            }
        } catch (SQLException e) {
            return "题目更新失败: " + e.getMessage();
        }
    }
    
    /**
     * 删除题目（逻辑删除）
     * 需要 QUESTION_DELETE 权限
     */
    public String deleteQuestion(int questionId) {
        if (!authService.hasPermission(Permission.QUESTION_DELETE)) {
            return "权限不足，无法删除题目";
        }
        
        try {
            // 检查题目是否存在
            Question existing = questionDao.findById(questionId);
            if (existing == null) {
                return "题目不存在";
            }
            
            if (questionDao.delete(questionId)) {
                return "题目删除成功";
            } else {
                return "题目删除失败";
            }
        } catch (SQLException e) {
            return "题目删除失败: " + e.getMessage();
        }
    }
    
    /**
     * 获取所有题目
     * 需要 QUESTION_READ 权限
     */
    public List<Question> getAllQuestions() {
        if (!authService.hasPermission(Permission.QUESTION_READ)) {
            return List.of(); // 返回空列表
        }
        
        try {
            return questionDao.findAll();
        } catch (SQLException e) {
            System.err.println("获取题目列表失败: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 根据ID获取题目
     * 需要 QUESTION_READ 权限
     */
    public Question getQuestionById(int id) {
        if (!authService.hasPermission(Permission.QUESTION_READ)) {
            return null;
        }
        
        try {
            return questionDao.findById(id);
        } catch (SQLException e) {
            System.err.println("获取题目失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 搜索题目
     * 需要 QUESTION_READ 权限
     */
    public List<Question> searchQuestions(String keyword, QuestionType type, 
                                          Difficulty difficulty, String category) {
        if (!authService.hasPermission(Permission.QUESTION_READ)) {
            return List.of();
        }
        
        try {
            return questionDao.search(keyword, type, difficulty, category);
        } catch (SQLException e) {
            System.err.println("搜索题目失败: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 随机抽取题目
     * 需要 QUESTION_EXTRACT 权限
     * 
     * @param count 抽取数量
     * @param type 题目类型（可选）
     * @param difficulty 难度等级（可选）
     * @param category 分类（可选）
     * @return 抽取的题目列表
     */
    public List<Question> extractQuestions(int count, QuestionType type, 
                                           Difficulty difficulty, String category) {
        if (!authService.hasPermission(Permission.QUESTION_EXTRACT)) {
            System.err.println("权限不足，无法抽取题目");
            return List.of();
        }
        
        if (count <= 0 || count > 100) {
            System.err.println("抽取数量无效，必须在1-100之间");
            return List.of();
        }
        
        try {
            return questionDao.extractRandom(count, type, difficulty, category);
        } catch (SQLException e) {
            System.err.println("抽取题目失败: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 获取所有分类
     * 需要 QUESTION_READ 权限
     */
    public List<String> getAllCategories() {
        if (!authService.hasPermission(Permission.QUESTION_READ)) {
            return List.of();
        }
        
        try {
            return questionDao.findAllCategories();
        } catch (SQLException e) {
            System.err.println("获取分类列表失败: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 统计题目总数
     * 需要 QUESTION_READ 权限
     */
    public int countQuestions() {
        if (!authService.hasPermission(Permission.QUESTION_READ)) {
            return 0;
        }
        
        try {
            return questionDao.count();
        } catch (SQLException e) {
            System.err.println("统计题目数量失败: " + e.getMessage());
            return 0;
        }
    }
}
