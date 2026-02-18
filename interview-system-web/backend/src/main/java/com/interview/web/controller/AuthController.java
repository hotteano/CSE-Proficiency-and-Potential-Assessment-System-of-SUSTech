package com.interview.web.controller;

import com.interview.web.dto.LoginRequest;
import com.interview.web.dto.LoginResponse;
import com.interview.web.dto.RegisterRequest;
import com.interview.web.entity.User;
import com.interview.web.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthService authService;
    
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Optional<User> userOpt = authService.authenticate(
            request.getUsername(), 
            request.getPassword()
        );
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String token = authService.generateToken(user);
            return ResponseEntity.ok(new LoginResponse(
                token,
                user.getUsername(),
                user.getRole().name(),
                "登录成功"
            ));
        }
        
        return ResponseEntity.status(401).body("用户名或密码错误");
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = authService.register(
                request.getUsername(),
                request.getPassword(),
                request.getRealName(),
                request.getEmail(),
                request.getRole()
            );
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal String username) {
        Optional<User> userOpt = authService.getCurrentUser(username);
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(userOpt.get());
        }
        return ResponseEntity.status(401).body("未登录");
    }
}
