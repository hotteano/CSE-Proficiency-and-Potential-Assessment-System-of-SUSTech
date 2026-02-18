package com.interview.web.service;

import com.interview.web.entity.InterviewRecord;
import com.interview.web.repository.InterviewRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InterviewRecordService {
    
    private final InterviewRecordRepository recordRepository;
    
    @Autowired
    public InterviewRecordService(InterviewRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }
    
    @Transactional(readOnly = true)
    public List<InterviewRecord> getAllRecords() {
        return recordRepository.findAllByOrderByCreatedAtDesc();
    }
    
    @Transactional(readOnly = true)
    public List<InterviewRecord> getMyRecords(String username) {
        return recordRepository.findByCandidateUsernameOrderByCreatedAtDesc(username);
    }
    
    @Transactional(readOnly = true)
    public List<InterviewRecord> getRecordsByExaminer(String examinerUsername) {
        return recordRepository.findByExaminerUsernameOrderByCreatedAtDesc(examinerUsername);
    }
    
    @Transactional
    public InterviewRecord createRecord(InterviewRecord record) {
        return recordRepository.save(record);
    }
    
    @Transactional
    public InterviewRecord updateRecord(InterviewRecord record) {
        return recordRepository.save(record);
    }
    
    @Transactional
    public void deleteRecord(Integer id) {
        recordRepository.deleteById(id);
    }
}
