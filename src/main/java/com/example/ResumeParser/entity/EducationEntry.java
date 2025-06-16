package com.example.ResumeParser.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EducationEntry {
    private String degree;
    private String institute;
    private String startYear;
    private String endYear;
}
