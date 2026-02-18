package com.interview.web.repository;

import com.interview.web.entity.InterviewRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewRecordRepository extends JpaRepository<InterviewRecord, Integer> {
    
    List<InterviewRecord> findByCandidateUsernameOrderByCreatedAtDesc(String candidateUsername);
    
    List<InterviewRecord> findByExaminerUsernameOrderByCreatedAtDesc(String examinerUsername);
    
    List<InterviewRecord> findAllByOrderByCreatedAtDesc();
}
