package com.example.ResumeParser.Controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.ResumeParser.Service.Resumeservice;
import com.example.ResumeParser.entity.Resume;

import com.example.ResumeParser.dto.ResumeWithSkillsDTO;
import com.example.ResumeParser.dto.SkillFilterRequest;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
public class Resumecontroller {

    
  @Autowired
  private Resumeservice resumeservice;
    
    @GetMapping("/hii")
    public String hello(){
        return "hello";
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadResume(@RequestParam("file") MultipartFile file) {
        
        resumeservice.parseResume(file);
        return ResponseEntity.ok("Resume parsed successfully. Check console for details.");
    }

    @PostMapping("/saveresume")
    public ResponseEntity<String> uploadAndSaveResume(@RequestParam("file") MultipartFile file) {
        resumeservice.saveresume(file);
        return ResponseEntity.ok("Resume parsed and saved successfully.");
    }


    @GetMapping("/resumes")
    public ResponseEntity<List<Resume>> getAllResumes() {
        List<Resume> resumes = resumeservice.getAllResumes();
        return ResponseEntity.ok(resumes);
    }
    // controller for filtering
    @PostMapping("/filter")
    public List<ResumeWithSkillsDTO> filterResumes(@RequestBody SkillFilterRequest request) {
        return resumeservice.filterResumes(request.getSkills(), request.getMinExp());
    }
}
