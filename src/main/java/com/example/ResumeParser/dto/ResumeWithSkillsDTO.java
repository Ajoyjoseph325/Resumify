package com.example.ResumeParser.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumeWithSkillsDTO {
    private Long id;
    private String name;
    private String phoneNumber;
    private String email;
    private int yearsOfExperience;
    private String skills;
}
