package com.example.ResumeParser.service;

import com.example.ResumeParser.entity.User;

public interface UserService {
    User registerUser(User user);
    User loginUser(String usernameOrEmail, String password);
    void resetPassword(String email, String newPassword);
}
