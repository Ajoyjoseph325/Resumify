package com.example.ResumeParser.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumeSkillsDTO {
    private long id;
    private String name;
    private String email;
    private String phone;
    private Double totalExperienceYears;
    private String skills;
}
