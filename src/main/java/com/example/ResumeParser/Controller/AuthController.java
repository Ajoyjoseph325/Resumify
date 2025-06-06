package com.example.ResumeParser.Controller;

import com.example.ResumeParser.Service.Email.GenCode;
import com.example.ResumeParser.entity.User;
import com.example.ResumeParser.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GenCode genCode;

    // Register endpoint
    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        // Optional: Check if user already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("User with this email already exists");
        }
        genCode.gencode(user);
        return userRepository.save(user);
        
    }

    // Login endpoint
    @PostMapping("/login")
    public User loginUser(@RequestBody User user) {
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent() && existingUser.get().getPassword().equals(user.getPassword())) {
            return existingUser.get(); // Successful login
        } else {
            throw new RuntimeException("Invalid email or password");
        }
    }
}
