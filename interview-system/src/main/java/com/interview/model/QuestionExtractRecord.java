package com.interview.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目抽取记录实体类
 * 记录每次题目抽取的历史
 */
public class QuestionExtractRecord {
    
    private Integer id;              // 记录ID
    private String extractedBy;      // 抽取者用户名
    private LocalDateTime extractTime; // 抽取时间
    private Integer questionCount;   // 抽取题目数量
    private String filterCriteria;   // 筛选条件（JSON格式）
    private List<Integer> questionIds; // 抽取的题目ID列表
    private String notes;            // 备注
    
    // 构造方法
    public QuestionExtractRecord() {
        this.extractTime = LocalDateTime.now();
    }
    
    public QuestionExtractRecord(String extractedBy, Integer questionCount, 
                                  String filterCriteria, List<Integer> questionIds) {
        this();
        this.extractedBy = extractedBy;
        this.questionCount = questionCount;
        this.filterCriteria = filterCriteria;
        this.questionIds = questionIds;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getExtractedBy() {
        return extractedBy;
    }
    
    public void setExtractedBy(String extractedBy) {
        this.extractedBy = extractedBy;
    }
    
    public LocalDateTime getExtractTime() {
        return extractTime;
    }
    
    public void setExtractTime(LocalDateTime extractTime) {
        this.extractTime = extractTime;
    }
    
    public Integer getQuestionCount() {
        return questionCount;
    }
    
    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }
    
    public String getFilterCriteria() {
        return filterCriteria;
    }
    
    public void setFilterCriteria(String filterCriteria) {
        this.filterCriteria = filterCriteria;
    }
    
    public List<Integer> getQuestionIds() {
        return questionIds;
    }
    
    public void setQuestionIds(List<Integer> questionIds) {
        this.questionIds = questionIds;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    @Override
    public String toString() {
        return String.format("QuestionExtractRecord{id=%d, extractedBy='%s', extractTime=%s, questionCount=%d}", 
                id, extractedBy, extractTime, questionCount);
    }
}
