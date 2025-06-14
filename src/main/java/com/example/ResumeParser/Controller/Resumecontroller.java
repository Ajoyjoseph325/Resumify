package com.example.ResumeParser.Controller;
import com.example.ResumeParser.Service.Resumeservice;
import com.example.ResumeParser.dto.ResumeSkillsDTO;
import com.example.ResumeParser.entity.Resume;
import com.example.ResumeParser.repository.Resumerepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/resumes")
public class Resumecontroller {
    private final Resumeservice resumeService;

    public Resumecontroller(Resumeservice resumeService) {
        this.resumeService = resumeService;
    }
    @Autowired
    GeminiModelController controller;

    @Autowired
    private Resumerepository resumeRepository;

   @GetMapping("/resumesByUserId/{userId}")
public ResponseEntity<List<ResumeSkillsDTO>> getResumesByUserId(@PathVariable Long userId) {
    List<Object[]> results = resumeRepository.findResumesByUserIdNative(userId);
    List<ResumeSkillsDTO> dtos = results.stream().map(record -> {
        Long id = ((Number) record[0]).longValue();
        String name = (String) record[1];
        String email = (String) record[2];
        String phone = (String) record[3];
        double totalExp = record[4] != null ? ((Number) record[4]).doubleValue() : 0.0;
        String skills = (String) record[5];
        return new ResumeSkillsDTO(id, name, email, phone, totalExp, skills); // 👈 with id now
    }).toList();

    return dtos.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(dtos);
}

    @GetMapping("/by-skills-experience-range")
public ResponseEntity<List<ResumeSkillsDTO>> getResumesBySkillsAndExperienceRangeAndUserId(
        @RequestParam List<String> skills,
        @RequestParam double start,
        @RequestParam double end,
        @RequestParam Long userId) {

    List<Object[]> results = resumeRepository.findResumesBySkillsAndExperienceRangeAndUserId(skills, start, end, userId);

    List<ResumeSkillsDTO> dtos = results.stream().map(record -> {
        Long id = ((Number) record[0]).longValue();
        String name = (String) record[1];
        String email = (String) record[2];
        String phone = (String) record[3];
        double totalExp = record[4] != null ? ((Number) record[4]).doubleValue() : 0.0;
        String skillStr = (String) record[5];
        return new ResumeSkillsDTO(id, name, email, phone, totalExp, skillStr);
    }).toList();

    return dtos.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(dtos);
}


   @GetMapping("/by-skills")
public ResponseEntity<List<ResumeSkillsDTO>> getResumesBySkillsAndUserId(
        @RequestParam List<String> skills,
        @RequestParam Long userId) {
    
    List<Object[]> results = resumeRepository.findResumesBySkillsUserId(skills, userId);

    List<ResumeSkillsDTO> dtos = results.stream().map(record -> {
        Long id = ((Number) record[0]).longValue(); // <-- MUST MATCH THE QUERY
        String name = (String) record[1];
        String email = (String) record[2];
        String phone = (String) record[3];
        double totalExp = record[4] != null ? ((Number) record[4]).doubleValue() : 0.0;
        String skillz = (String) record[5];
        return new ResumeSkillsDTO(id, name, email, phone, totalExp, skillz);
    }).toList();

    return ResponseEntity.ok(dtos);
}


    @DeleteMapping("/delete/user/{userId}")
    public ResponseEntity<String> deleteResumes(@PathVariable Long userId) {
        resumeService.deleteResumesByUserId(userId);
        return ResponseEntity.ok("All resumes for user " + userId + " deleted.");
    }

    @DeleteMapping("/delete/user/{userId}/resume/{resumeId}")
    public ResponseEntity<String> deleteUserResume(
            @PathVariable Long userId,
            @PathVariable Long resumeId) {
        try {
            resumeService.deleteResumeByIdAndUserId(resumeId, userId);
            return ResponseEntity.ok("Resume with ID " + resumeId + " deleted for user " + userId);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(403).body(ex.getMessage());
        }
    }
    // Function to show the image of the resume
   @GetMapping("/image/{resumeId}")
public ResponseEntity<Resource> getResumeImage(@PathVariable Long resumeId) {
    Optional<Resume> optionalResume = resumeRepository.findById(resumeId);

    if (optionalResume.isEmpty() || optionalResume.get().getResumeImage() == null) {
        return ResponseEntity.notFound().build();
    }

    Resume resume = optionalResume.get();
    byte[] imageData = resume.getResumeImage();
    ByteArrayResource resource = new ByteArrayResource(imageData);

    return ResponseEntity.ok()
            .contentLength(imageData.length)
            .contentType(MediaType.IMAGE_PNG) // Adjust if necessary
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"resume_" + resumeId + ".png\"")
            .body(resource);
}
}