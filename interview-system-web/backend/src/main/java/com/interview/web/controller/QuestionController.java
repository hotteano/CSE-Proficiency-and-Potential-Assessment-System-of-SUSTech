package com.interview.web.controller;

import com.interview.web.entity.Question;
import com.interview.web.entity.User;
import com.interview.web.service.AuthService;
import com.interview.web.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "*")
public class QuestionController {
    
    private final QuestionService questionService;
    private final AuthService authService;
    
    @Autowired
    public QuestionController(QuestionService questionService, AuthService authService) {
        this.questionService = questionService;
        this.authService = authService;
    }
    
    @GetMapping
    public ResponseEntity<List<Question>> getAllQuestions() {
        return ResponseEntity.ok(questionService.getAllQuestions());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getQuestionById(@PathVariable Integer id) {
        Optional<Question> question = questionService.getQuestionById(id);
        return question.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<?> createQuestion(
            @RequestBody Question question,
            @AuthenticationPrincipal String username) {
        try {
            User user = authService.getCurrentUser(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            Question saved = questionService.createQuestion(question, user);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuestion(
            @PathVariable Integer id,
            @RequestBody Question question) {
        try {
            question.setId(id);
            Question updated = questionService.updateQuestion(question);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Integer id) {
        try {
            questionService.deleteQuestion(id);
            return ResponseEntity.ok("删除成功");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(questionService.getAllCategories());
    }
    
    @GetMapping("/extract")
    public ResponseEntity<List<Question>> extractQuestions(
            @RequestParam(defaultValue = "5") int count) {
        return ResponseEntity.ok(questionService.extractRandomQuestions(count));
    }
}
