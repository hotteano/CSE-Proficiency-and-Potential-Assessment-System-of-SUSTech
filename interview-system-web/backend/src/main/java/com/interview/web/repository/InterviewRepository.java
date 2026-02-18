package com.interview.web.repository;

import com.interview.web.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Integer> {
    
    List<Interview> findByCandidateIdOrderByCreatedAtDesc(Integer candidateId);
    
    List<Interview> findByExaminerIdOrderByCreatedAtDesc(Integer examinerId);
    
    List<Interview> findByStatusOrderByCreatedAtDesc(Interview.InterviewStatus status);
}
