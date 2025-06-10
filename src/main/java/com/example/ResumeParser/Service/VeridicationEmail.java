package com.example.ResumeParser.Service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.ResumeParser.Service.Email.EmailService;
import com.example.ResumeParser.Service.Email.GenCode;
import com.example.ResumeParser.entity.User;
import com.example.ResumeParser.repository.UserRepository;

@Service
public class VeridicationEmail {
    @Autowired
    private UserRepository forgotRepo;
    @Autowired
    private EmailService emailService;
    @Autowired
    private GenCode genCode;
 public ResponseEntity<String> getEmail(User userEmail) {
        Optional<User> existEmail = forgotRepo.findByEmail(userEmail.getEmail());

        if (existEmail.isPresent()) {
            User existingUser = existEmail.get();

            // Always generate a new code (or control this with config/expiry if needed)
            String code = genCode.AutogenerateCode();
            existingUser.setValidationCode(code);
            forgotRepo.save(existingUser); // 📝 Save the updated user with new code

            // 📧 Send email
            emailService.sendVerificationEmail(userEmail.getEmail(), code);
            System.out.println("📨 Verification code: " + code);

            return ResponseEntity.ok("Verification code sent to " + userEmail.getEmail());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not found.");
        }
    }
}