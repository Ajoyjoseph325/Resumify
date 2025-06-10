package com.example.ResumeParser.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ResumeParser.Service.VerificationCode;
import com.example.ResumeParser.entity.User;

@RestController
@RequestMapping("api/auth/setCode")
@CrossOrigin(origins = "http://localhost:4200")
public class VerificationCodeController {
     @Autowired
    public VerificationCode verificationCode;

     @PostMapping("")
    public ResponseEntity<String> verifyCode(@RequestBody User userCode) {
        return verificationCode.getCode(userCode);
    }
    
}
