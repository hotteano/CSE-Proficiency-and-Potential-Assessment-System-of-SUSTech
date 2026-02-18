package com.interview.web.repository;

import com.interview.web.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    
    List<Question> findByActiveTrue();
    
    @Query("SELECT DISTINCT q.category FROM Question q WHERE q.category IS NOT NULL")
    List<String> findAllCategories();
    
    @Query(value = "SELECT * FROM questions WHERE active = true ORDER BY RANDOM() LIMIT ?1", nativeQuery = true)
    List<Question> findRandomQuestions(int count);
}
