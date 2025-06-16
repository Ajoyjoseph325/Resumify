package com.example.ResumeParser.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceEntry {
    private String title;
    private String company;
    private String specializationskills;
    private Double years;
}
