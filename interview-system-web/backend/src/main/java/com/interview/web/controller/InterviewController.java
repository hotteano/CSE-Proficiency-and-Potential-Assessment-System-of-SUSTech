package com.interview.web.controller;

import com.interview.web.entity.Interview;
import com.interview.web.entity.Role;
import com.interview.web.entity.User;
import com.interview.web.repository.InterviewRepository;
import com.interview.web.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/interviews")
@CrossOrigin(origins = "*")
public class InterviewController {
    
    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public InterviewController(InterviewRepository interviewRepository, UserRepository userRepository) {
        this.interviewRepository = interviewRepository;
        this.userRepository = userRepository;
    }
    
    @GetMapping
    public ResponseEntity<List<Interview>> getAllInterviews() {
        return ResponseEntity.ok(interviewRepository.findAll());
    }
    
    @GetMapping("/my")
    public ResponseEntity<?> getMyInterviews(@AuthenticationPrincipal String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = userOpt.get();
        List<Interview> interviews;
        
        if (user.getRole() == Role.CANDIDATE) {
            interviews = interviewRepository.findByCandidateIdOrderByCreatedAtDesc(user.getId());
        } else if (user.getRole() == Role.EXAMINER) {
            interviews = interviewRepository.findByExaminerIdOrderByCreatedAtDesc(user.getId());
        } else {
            interviews = interviewRepository.findAll();
        }
        
        return ResponseEntity.ok(interviews);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getInterviewById(@PathVariable Integer id) {
        Optional<Interview> interview = interviewRepository.findById(id);
        return interview.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<?> createInterview(
            @RequestBody Interview interview,
            @AuthenticationPrincipal String username) {
        
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("用户不存在");
        }
        
        User user = userOpt.get();
        interview.setExaminerId(user.getId());
        interview.setExaminerUsername(user.getUsername());
        interview.setStatus(Interview.InterviewStatus.SCHEDULED);
        interview.setCreatedAt(LocalDateTime.now());
        
        Interview saved = interviewRepository.save(interview);
        return ResponseEntity.ok(saved);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateInterview(
            @PathVariable Integer id,
            @RequestBody Interview interview) {
        
        if (!interviewRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        interview.setId(id);
        interview.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(interviewRepository.save(interview));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInterview(@PathVariable Integer id) {
        if (!interviewRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        interviewRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/start")
    public ResponseEntity<?> startInterview(@PathVariable Integer id) {
        Optional<Interview> opt = interviewRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Interview interview = opt.get();
        interview.setStatus(Interview.InterviewStatus.IN_PROGRESS);
        interview.setStartedAt(LocalDateTime.now());
        interviewRepository.save(interview);
        return ResponseEntity.ok(interview);
    }
    
    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeInterview(
            @PathVariable Integer id,
            @RequestBody(required = false) Interview data) {
        
        Optional<Interview> opt = interviewRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Interview interview = opt.get();
        interview.setStatus(Interview.InterviewStatus.COMPLETED);
        interview.setCompletedAt(LocalDateTime.now());
        
        if (data != null) {
            if (data.getTotalScore() != null) {
                interview.setTotalScore(data.getTotalScore());
            }
            if (data.getEvaluation() != null) {
                interview.setEvaluation(data.getEvaluation());
            }
        }
        
        interviewRepository.save(interview);
        return ResponseEntity.ok(interview);
    }
}
