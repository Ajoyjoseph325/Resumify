package com.example.ResumeParser.dto;

public class ResumeFilteredDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Double totalExperienceYears;
    private String skills;  // Comma-separated

    // Constructor
    public ResumeFilteredDTO(Long id, String name, String email, String phone, Double totalExperienceYears, String skills) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.totalExperienceYears = totalExperienceYears;
        this.skills = skills;
    }

    // Getters & Setters (or use Lombok @Data for brevity)
}
