package com.example.ResumeParser.Service;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.ResumeParser.dto.ResumeWithSkillsDTO;
import com.example.ResumeParser.entity.Knownskill;
import com.example.ResumeParser.entity.Resume;
import com.example.ResumeParser.entity.Skill;
import com.example.ResumeParser.repository.Knownskillrepository;
import com.example.ResumeParser.repository.Resumerepository;
import com.example.ResumeParser.repository.Skillrepository;

import jakarta.transaction.Transactional;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;


    @Service

    public class Resumeservice {

    @Autowired
    private Resumerepository resumeRepository;

    @Autowired
    private Skillrepository skillRepository;
    

    @Autowired
    private Knownskillrepository knownskillrepo;

    // added portion for filtering

    @Autowired
    private Resumerepository resumerepository;

    public List<ResumeWithSkillsDTO> filterResumes(List<String> skills, int minExp) {
        int skillCount = skills.size();
        List<Object[]> results = resumerepository.filterBySkillsAndExperience(skills, minExp, skillCount);

        List<ResumeWithSkillsDTO> finalList = new ArrayList<>();
        for (Object[] row : results) {
            ResumeWithSkillsDTO dto = new ResumeWithSkillsDTO(
                ((Number) row[0]).longValue(),
                (String) row[1],
                (String) row[2],
                (String) row[3],
                ((Number) row[4]).intValue(),
                (String) row[5]
            );
            finalList.add(dto);
        }

        return finalList;
    }
    // till here


    public void parseResume(MultipartFile file) {
        try {
            String content = extractTextFromFile(file);

            String name = extractName(content);
            String email = extractEmail(content);
            String phone = extractPhone(content);
            String experience = extractExperience(content);
            List<String> skills = extractSkills(content);

            System.out.println("Name: " + name);
            System.out.println("Email: " + email);
            System.out.println("Phone: " + phone);
            System.out.println("Experience: " + experience);
            System.out.println("Skills: " + skills);






        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String extractTextFromFile(MultipartFile file) throws IOException {
        if (file.getOriginalFilename().endsWith(".pdf")) {
            try (PDDocument document = PDDocument.load(file.getInputStream())) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }
        } else {
            return new String(file.getBytes());
        }
    }

    private String extractEmail(String text) {
        Matcher matcher = Pattern.compile("\\b[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}\\b").matcher(text);
        return matcher.find() ? matcher.group() : "Not Found";
    }

    private String extractPhone(String text) {
        Matcher matcher = Pattern.compile("\\b\\d{10}\\b").matcher(text);
        return matcher.find() ? matcher.group() : "Not Found";
    }

    private String extractName(String text) {
        String[] lines = text.split("\n");
        return lines.length > 0 ? lines[0].trim() : "Not Found";
    }

    private String extractExperience(String text) {
        Matcher matcher = Pattern.compile("(\\d+)\\+?\\s+years?\\s+of\\s+experience", Pattern.CASE_INSENSITIVE).matcher(text);
        return matcher.find() ? matcher.group(1) + " years" : "Not Found";
    }

    // private List<String> extractSkills(String text) {
    //     List<String> knownSkills = List.of("Java", "Spring", "Python", "SQL", "JavaScript", "AWS", "Docker", "Angular", "React");
    //     return knownSkills.stream()
    //             .filter(skill -> text.toLowerCase().contains(skill.toLowerCase()))
    //             .collect(Collectors.toList());
    // }



private List<String> extractSkills(String text) {
    List<String> knownSkills = knownskillrepo.findAll()
                                .stream()
                                .map(Knownskill::getName)
                                .collect(Collectors.toList());

    return knownSkills.stream()
            .filter(skill -> text.toLowerCase().contains(skill.toLowerCase()))
            .collect(Collectors.toList());
}



    @Transactional
    public void saveresume(MultipartFile file) {
        try {
            String content = extractTextFromFile(file);

            String name = extractName(content);
            String email = extractEmail(content);
            String phone = extractPhone(content);
            String experienceStr = extractExperience(content);
            List<String> skillsList = extractSkills(content);

            double experience = extractExperienceAsDouble(experienceStr);

            Resume resume = new Resume();
            resume.setName(name);
            resume.setEmail(email);
            resume.setPhoneNumber(phone);
            resume.setYearsOfExperience(experience);

            for (String skillName : skillsList) {
                Skill skill = skillRepository.findByName(skillName)
                        .orElseGet(() -> new Skill(skillName));
                resume.addSkill(skill); // Bi-directional link
            }

            resumeRepository.save(resume);
            System.out.println("Saved resume for: " + name);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double extractExperienceAsDouble(String experienceStr) {
        try {
            return Double.parseDouble(experienceStr.replaceAll("[^\\d.]", ""));
        } catch (Exception e) {
            return 0.0;
        }
    }



    public List<Resume> getAllResumes() {
        return resumeRepository.findAll();
    }


    
}
