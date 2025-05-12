package com.example.ResumeParser.dto;

import lombok.Data;

import java.util.List;

@Data
public class SkillFilterRequest {
    private List<String> skills;
    private int minExp;
}
