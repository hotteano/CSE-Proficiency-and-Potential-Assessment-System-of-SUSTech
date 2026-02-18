package com.interview.web.service;

import com.interview.web.entity.Question;
import com.interview.web.entity.User;
import com.interview.web.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class QuestionService {
    
    private final QuestionRepository questionRepository;
    
    @Autowired
    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }
    
    @Transactional(readOnly = true)
    public List<Question> getAllQuestions() {
        return questionRepository.findByActiveTrue();
    }
    
    @Transactional(readOnly = true)
    public Optional<Question> getQuestionById(Integer id) {
        return questionRepository.findById(id);
    }
    
    @Transactional
    public Question createQuestion(Question question, User creator) {
        question.setCreatedBy(creator.getUsername());
        question.setActive(true);
        return questionRepository.save(question);
    }
    
    @Transactional
    public Question updateQuestion(Question question) {
        return questionRepository.save(question);
    }
    
    @Transactional
    public void deleteQuestion(Integer id) {
        Question question = questionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("题目不存在"));
        question.setActive(false);
        questionRepository.save(question);
    }
    
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return questionRepository.findAllCategories();
    }
    
    @Transactional(readOnly = true)
    public List<Question> extractRandomQuestions(int count) {
        return questionRepository.findRandomQuestions(count);
    }
}
