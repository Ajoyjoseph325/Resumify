package com.example.ResumeParser.dto;

import com.example.ResumeParser.entity.EducationEntry;
import com.example.ResumeParser.entity.ExperienceEntry;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PreviewDTO {
    private Long previewId;
    private Long userId;
    private Long resumeId;
    private String name;
    private String email;
    private String phone;
   @Column(columnDefinition = "TEXT")
    private String profileSummary;
   @Column(columnDefinition = "TEXT")
    private String description;
    private String linkedin;
    private String github;
    private Double overallScore;
    private String jobFit;
    private String specializedJob;
    private String jobPosition;
    private String imageUrl;
    private List<String> skills;
    private List<EducationEntry> education;
    private List<ExperienceEntry> experience;
}
