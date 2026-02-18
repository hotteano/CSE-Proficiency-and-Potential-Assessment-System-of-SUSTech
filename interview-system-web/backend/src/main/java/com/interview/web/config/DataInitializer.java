package com.interview.web.config;

import com.interview.web.entity.Role;
import com.interview.web.entity.User;
import com.interview.web.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    
    @Autowired
    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public void run(String... args) {
        // 创建默认管理员账号
        if (!userRepository.existsByUsername("admin")) {
            String passwordHash = BCrypt.hashpw("admin123", BCrypt.gensalt(12));
            User admin = new User("admin", passwordHash, "系统管理员", 
                                  "admin@interview.com", Role.ADMIN);
            userRepository.save(admin);
            System.out.println("默认管理员账号创建成功: admin / admin123");
        }
        
        // 创建示例考生账号
        if (!userRepository.existsByUsername("candidate")) {
            String passwordHash = BCrypt.hashpw("candidate123", BCrypt.gensalt(12));
            User candidate = new User("candidate", passwordHash, "示例考生", 
                                      "candidate@interview.com", Role.CANDIDATE);
            userRepository.save(candidate);
            System.out.println("示例考生账号创建成功: candidate / candidate123");
        }
        
        // 创建示例考官账号
        if (!userRepository.existsByUsername("judge")) {
            String passwordHash = BCrypt.hashpw("judge123", BCrypt.gensalt(12));
            User judge = new User("judge", passwordHash, "示例考官", 
                                  "judge@interview.com", Role.EXAMINER);
            userRepository.save(judge);
            System.out.println("示例考官账号创建成功: judge / judge123");
        }
    }
}
