package com.example.ResumeParser.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ResumeParser.Service.Forgotpassword;
import com.example.ResumeParser.entity.User;
@RestController
@RequestMapping("api/auth/newpassword")
@CrossOrigin(origins = "http://localhost:4200")
public class ForgotpasswordController { 
    @Autowired
    private Forgotpassword forgotpassword;

     @PostMapping("")
    public ResponseEntity<String> updatePassword(@RequestBody User userCode) {
        return forgotpassword.updatePassword(userCode);
    }
}
