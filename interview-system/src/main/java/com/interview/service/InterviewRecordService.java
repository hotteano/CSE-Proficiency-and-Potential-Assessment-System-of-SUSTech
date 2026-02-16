package com.interview.service;

import com.interview.dao.InterviewRecordDao;
import com.interview.model.InterviewRecord;
import com.interview.model.InterviewRecord.InterviewStatus;
import com.interview.model.Permission;
import com.interview.model.Role;
import com.interview.model.User;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

/**
 * 面试记录服务类
 * 处理面试记录和语音文件管理的业务逻辑
 */
public class InterviewRecordService {
    
    private final InterviewRecordDao recordDao;
    private final AuthService authService;
    
    // 语音文件存储目录
    private static final String VOICE_STORAGE_DIR = "voice_records";
    
    public InterviewRecordService(AuthService authService) {
        this.recordDao = new InterviewRecordDao();
        this.authService = authService;
        
        // 确保存储目录存在
        createStorageDirectory();
    }
    
    /**
     * 创建语音文件存储目录
     */
    private void createStorageDirectory() {
        File dir = new File(VOICE_STORAGE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    /**
     * 创建新的面试记录
     * 需要 VOICE_RECORD 权限
     */
    public String createRecord(String candidateUsername, String examinerUsername) {
        if (!authService.hasPermission(Permission.VOICE_RECORD)) {
            return "权限不足";
        }
        
        if (candidateUsername == null || candidateUsername.trim().isEmpty()) {
            return "考生用户名不能为空";
        }
        
        try {
            InterviewRecord record = new InterviewRecord(candidateUsername, examinerUsername);
            
            if (recordDao.insert(record)) {
                return "面试记录创建成功，ID: " + record.getId();
            } else {
                return "面试记录创建失败";
            }
        } catch (SQLException e) {
            return "创建失败: " + e.getMessage();
        }
    }
    
    /**
     * 为当前考生创建面试记录
     * 考生自己创建记录
     */
    public String createRecordForSelf() {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return "请先登录";
        }
        
        if (currentUser.getRole() != Role.CANDIDATE) {
            return "只有考生角色可以创建自己的面试记录";
        }
        
        return createRecord(currentUser.getUsername(), null);
    }
    
    /**
     * 获取所有面试记录
     * 需要 VIEW_RECORDS 权限
     */
    public List<InterviewRecord> getAllRecords() {
        if (!authService.hasPermission(Permission.VIEW_RECORDS)) {
            return List.of();
        }
        
        try {
            return recordDao.findAll();
        } catch (SQLException e) {
            System.err.println("获取面试记录失败: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 获取当前考生的面试记录
     * 需要 VIEW_OWN_RECORDS 权限
     */
    public List<InterviewRecord> getMyRecords() {
        if (!authService.hasPermission(Permission.VIEW_OWN_RECORDS)) {
            return List.of();
        }
        
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return List.of();
        }
        
        try {
            return recordDao.findByCandidate(currentUser.getUsername());
        } catch (SQLException e) {
            System.err.println("获取面试记录失败: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 获取指定考生的面试记录
     * 需要 VIEW_RECORDS 权限
     */
    public List<InterviewRecord> getRecordsByCandidate(String candidateUsername) {
        if (!authService.hasPermission(Permission.VIEW_RECORDS)) {
            return List.of();
        }
        
        try {
            return recordDao.findByCandidate(candidateUsername);
        } catch (SQLException e) {
            System.err.println("获取面试记录失败: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 更新面试状态
     */
    public String updateStatus(int recordId, InterviewStatus status) {
        if (!authService.hasPermission(Permission.VOICE_RECORD)) {
            return "权限不足";
        }
        
        try {
            InterviewRecord record = recordDao.findById(recordId);
            if (record == null) {
                return "面试记录不存在";
            }
            
            // 考生只能更新自己的记录
            User currentUser = authService.getCurrentUser();
            if (currentUser.getRole() == Role.CANDIDATE && 
                !currentUser.getUsername().equals(record.getCandidateUsername())) {
                return "只能更新自己的面试记录";
            }
            
            if (recordDao.updateStatus(recordId, status)) {
                return "状态更新成功";
            } else {
                return "状态更新失败";
            }
        } catch (SQLException e) {
            return "更新失败: " + e.getMessage();
        }
    }
    
    /**
     * 更新面试评价
     * 考官或管理员可以使用
     */
    public String updateNotes(int recordId, String notes) {
        // 考官和管理员可以添加评价
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return "请先登录";
        }
        
        // 考生不能添加评价
        if (currentUser.getRole() == Role.CANDIDATE) {
            return "考生不能添加面试评价";
        }
        
        try {
            InterviewRecord record = recordDao.findById(recordId);
            if (record == null) {
                return "面试记录不存在";
            }
            
            if (recordDao.updateNotes(recordId, notes)) {
                return "评价更新成功";
            } else {
                return "评价更新失败";
            }
        } catch (SQLException e) {
            return "更新失败: " + e.getMessage();
        }
    }
    
    /**
     * 保存语音文件
     * 需要 VOICE_RECORD 权限
     * 
     * @param recordId 面试记录ID
     * @param sourceFile 源语音文件
     * @return 保存结果消息
     */
    public String saveVoiceFile(int recordId, File sourceFile) {
        if (!authService.hasPermission(Permission.VOICE_RECORD)) {
            return "权限不足，无法上传语音文件";
        }
        
        if (sourceFile == null || !sourceFile.exists()) {
            return "语音文件不存在";
        }
        
        try {
            InterviewRecord record = recordDao.findById(recordId);
            if (record == null) {
                return "面试记录不存在";
            }
            
            // 考生只能上传到自己的记录
            User currentUser = authService.getCurrentUser();
            if (currentUser.getRole() == Role.CANDIDATE && 
                !currentUser.getUsername().equals(record.getCandidateUsername())) {
                return "只能上传语音到自己的面试记录";
            }
            
            // 生成存储文件名: candidate_recordId_timestamp.ext
            String originalName = sourceFile.getName();
            String extension = "";
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalName.substring(dotIndex);
            }
            
            String storageFileName = String.format("%s_%d_%d%s",
                    record.getCandidateUsername(),
                    recordId,
                    System.currentTimeMillis(),
                    extension);
            
            File destFile = new File(VOICE_STORAGE_DIR, storageFileName);
            
            // 复制文件到存储目录
            java.nio.file.Files.copy(
                sourceFile.toPath(),
                destFile.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
            
            // 更新数据库记录
            if (recordDao.updateVoiceFile(recordId, destFile.getAbsolutePath(), 
                                           originalName, destFile.length())) {
                return "语音文件上传成功";
            } else {
                // 删除已复制的文件
                destFile.delete();
                return "语音文件保存失败";
            }
            
        } catch (Exception e) {
            return "上传失败: " + e.getMessage();
        }
    }
    
    /**
     * 获取语音文件
     * 
     * @param recordId 面试记录ID
     * @return 语音文件对象，如果不存在返回 null
     */
    public File getVoiceFile(int recordId) {
        try {
            InterviewRecord record = recordDao.findById(recordId);
            if (record == null || record.getVoiceFilePath() == null) {
                return null;
            }
            
            // 检查权限
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return null;
            }
            
            // 考生只能访问自己的语音
            if (currentUser.getRole() == Role.CANDIDATE && 
                !currentUser.getUsername().equals(record.getCandidateUsername())) {
                return null;
            }
            
            // 其他角色可以查看所有记录
            if (!authService.hasPermission(Permission.VIEW_RECORDS) && 
                !authService.hasPermission(Permission.VIEW_OWN_RECORDS)) {
                return null;
            }
            
            File voiceFile = new File(record.getVoiceFilePath());
            if (voiceFile.exists()) {
                return voiceFile;
            }
            
        } catch (SQLException e) {
            System.err.println("获取语音文件失败: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 删除面试记录
     */
    public String deleteRecord(int recordId) {
        // 只有管理员可以删除
        if (!authService.isAdmin()) {
            return "只有管理员可以删除面试记录";
        }
        
        try {
            InterviewRecord record = recordDao.findById(recordId);
            if (record == null) {
                return "面试记录不存在";
            }
            
            // 删除关联的语音文件
            if (record.getVoiceFilePath() != null) {
                File voiceFile = new File(record.getVoiceFilePath());
                if (voiceFile.exists()) {
                    voiceFile.delete();
                }
            }
            
            if (recordDao.delete(recordId)) {
                return "面试记录删除成功";
            } else {
                return "面试记录删除失败";
            }
        } catch (SQLException e) {
            return "删除失败: " + e.getMessage();
        }
    }
    
    /**
     * 获取存储目录路径
     */
    public String getStorageDirectory() {
        return new File(VOICE_STORAGE_DIR).getAbsolutePath();
    }
}
