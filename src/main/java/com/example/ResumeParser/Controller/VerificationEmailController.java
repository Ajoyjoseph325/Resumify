package com.example.ResumeParser.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ResumeParser.Service.VeridicationEmail;
import com.example.ResumeParser.entity.User;

@RestController
@RequestMapping("api/auth/setEmail")
@CrossOrigin(origins = "http://localhost:4200")
public class VerificationEmailController {
    @Autowired
    public VeridicationEmail verificationEmail;
    

    @PostMapping("")
    public ResponseEntity<String> getEmail(@RequestBody User userEmail) {
        return verificationEmail.getEmail(userEmail);  
    }
    
}
