package com.example.ResumeParser.controller;

import com.example.ResumeParser.entity.User;
import com.example.ResumeParser.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.ResumeParser.dto.ForgotPasswordRequest;

@CrossOrigin(origins = "*") // Or restrict to frontend URL
@RestController
@RequestMapping("/api/auth")
//@CrossOrigin(origins = "*")  // Enable CORS for frontend connection
public class AuthController {

    @Autowired
    private UserService userService;

    // Register Endpoint
    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        return userService.registerUser(user);
    }

    // Login Endpoint
    @PostMapping("/login")
    public User loginUser(@RequestBody LoginRequest loginRequest) {
        return userService.loginUser(loginRequest.getUsernameOrEmail(), loginRequest.getPassword());
    }
@CrossOrigin(origins = "*")  // Allow all origins (for testing)
@PostMapping("/forgot-password")
public String forgotPassword(@RequestBody ForgotPasswordRequest request) {
    userService.resetPassword(request.getEmail(), request.getNewPassword());
    return "Password updated successfully";
}




    // DTO for Login Request
    public static class LoginRequest {
        private String usernameOrEmail;
        private String password;

        public String getUsernameOrEmail() {
            return usernameOrEmail;
        }

        public void setUsernameOrEmail(String usernameOrEmail) {
            this.usernameOrEmail = usernameOrEmail;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
