package com.example.ResumeParser.controller;

import com.example.ResumeParser.model.ResumeData;
import com.example.ResumeParser.service.ResumeParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController // Marks this class as a REST controller
@RequestMapping("/api/resume") // Base URL for all endpoints here
public class ResumeController {

    @Autowired // Injects the service class into the controller
    private ResumeParserService resumeParserService;

    // Example: GET /api/resume/parse?path=C:/resumes/john.pdf
    @GetMapping("/parse")
    public ResumeData parseResume(@RequestParam String path) throws IOException {
        return resumeParserService.parsePdf(path);
    }
}
