package com.interview.web.service;

import com.interview.web.entity.Role;
import com.interview.web.entity.User;
import com.interview.web.repository.UserRepository;
import com.interview.web.security.JwtUtil;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    
    @Autowired
    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }
    
    @Transactional(readOnly = true)
    public Optional<User> authenticate(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.isActive() && BCrypt.checkpw(password, user.getPasswordHash())) {
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }
    
    public String generateToken(User user) {
        return jwtUtil.generateToken(user.getUsername(), user.getRole().name());
    }
    
    @Transactional
    public User register(String username, String password, String realName, 
                        String email, Role role) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(12));
        User user = new User(username, passwordHash, realName, email, role);
        return userRepository.save(user);
    }
    
    @Transactional(readOnly = true)
    public Optional<User> getCurrentUser(String username) {
        return userRepository.findByUsername(username);
    }
    
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (!BCrypt.checkpw(oldPassword, user.getPasswordHash())) {
            throw new RuntimeException("旧密码错误");
        }
        
        user.setPasswordHash(BCrypt.hashpw(newPassword, BCrypt.gensalt(12)));
        userRepository.save(user);
    }
}
