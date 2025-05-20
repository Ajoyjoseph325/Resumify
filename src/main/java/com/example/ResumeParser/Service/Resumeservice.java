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
import com.example.ResumeParser.entity.User;
import com.example.ResumeParser.repository.Knownskillrepository;
import com.example.ResumeParser.repository.Resumerepository;
import com.example.ResumeParser.repository.Skillrepository;
import com.example.ResumeParser.repository.UserRepository;

import jakarta.transaction.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class Resumeservice {

    @Autowired
    private Resumerepository resumeRepository;

    @Autowired
    private Skillrepository skillRepository;

    @Autowired
    private Knownskillrepository knownskillrepo;

    @Autowired
    private UserRepository userRepository;

    public List<ResumeWithSkillsDTO> filterResumes(List<String> skills, double minExp, Long userId) {
       System.out.println("hi from filter resumes");
        
        int skillCount = skills.size();
        List<Object[]> results = resumeRepository.filterBySkillsAndExperience(skills, minExp,skillCount, userId);

        List<ResumeWithSkillsDTO> finalList = new ArrayList<>();
        for (Object[] row : results) {
            ResumeWithSkillsDTO dto = new ResumeWithSkillsDTO(
                ((Number) row[0]).longValue(),
                (String) row[1],
                (String) row[2],
                (String) row[3],
                ((Number) row[4]).doubleValue(),
                Arrays.asList(((String) row[5]).split(",")) // split skills string into list
            );
            finalList.add(dto);
        }

        return finalList;
    }

    // public void parseResume(MultipartFile file) {
    //     try {
    //         String content = extractTextFromFile(file);

    //         String name = extractName(content);
    //         String email = extractEmail(content);
    //         String phone = extractPhone(content);
    //         String experience = extractExperience(content);
    //         List<String> skills = extractSkills(content);

    //         System.out.println("Name: " + name);
    //         System.out.println("Email: " + email);
    //         System.out.println("Phone: " + phone);
    //         System.out.println("Experience: " + experience);
    //         System.out.println("Skills: " + skills);

    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }

    // private String extractTextFromFile(MultipartFile file) throws IOException {
    //     if (file.getOriginalFilename().endsWith(".pdf")) {
    //         try (PDDocument document = PDDocument.load(file.getInputStream())) {
    //             PDFTextStripper stripper = new PDFTextStripper();
    //             return stripper.getText(document);
    //         }
    //     } else {
    //         return new String(file.getBytes());
    //     }
    // }

    // private String extractEmail(String text) {
    //     Matcher matcher = Pattern.compile("\\b[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}\\b").matcher(text);
    //     return matcher.find() ? matcher.group() : "Not Found";
    // }

    // private String extractPhone(String text) {
    //     Matcher matcher = Pattern.compile("\\b\\d{10}\\b").matcher(text);
    //     return matcher.find() ? matcher.group() : "Not Found";
    // }
    // old funtion that extracts name
    // private String extractName(String text) {
    //     String[] lines = text.split("\n");
    //     return lines.length > 0 ? lines[0].trim() : "Not Found";
    // }


    // new function that extracts name






    private String extractExperience(String text) {
        Matcher matcher = Pattern.compile("(\\d+)\\+?\\s+years?\\s+of\\s+experience", Pattern.CASE_INSENSITIVE).matcher(text);
        return matcher.find() ? matcher.group(1) + " years" : "Not Found";
    }

    private List<String> extractSkills(String text) {
        List<String> knownSkills = knownskillrepo.findAll()
                                .stream()
                                .map(Knownskill::getName)
                                .collect(Collectors.toList());

        return knownSkills.stream()
                .filter(skill -> text.toLowerCase().contains(skill.toLowerCase()))
                .collect(Collectors.toList());
   }



    // old save function that saves resume without id
    // @Transactional
    // public void saveresume(MultipartFile file) {
    //     try {
    //         String content = extractTextFromFile(file);

    //         String name = extractName(content);
    //         String email = extractEmail(content);
    //         String phone = extractPhone(content);
    //         String experienceStr = extractExperience(content);
    //         List<String> skillsList = extractSkills(content);

    //         double experience = extractExperienceAsDouble(experienceStr);

    //         Resume resume = new Resume();
    //         resume.setName(name);
    //         resume.setEmail(email);
    //         resume.setPhoneNumber(phone);
    //         resume.setYearsOfExperience(experience);

    //         for (String skillName : skillsList) {
    //             Skill skill = skillRepository.findByName(skillName)
    //                     .orElseGet(() -> new Skill(skillName));
    //             resume.addSkill(skill);
    //         }

    //         resumeRepository.save(resume);
    //         System.out.println("Saved resume for: " + name);

    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }

    // private double extractExperienceAsDouble(String experienceStr) {
    //     try {
    //         return Double.parseDouble(experienceStr.replaceAll("[^\\d.]", ""));
    //     } catch (Exception e) {
    //         return 0.0;
    //     }
    // }



    // get all resumes funtion
    // public List<Resume> getAllResumes() {
    //     return resumeRepository.findAll();
    // }

    // New method to get resumes by userId
    // public List<ResumeWithSkillsDTO> getResumesByUserId(Long userId) {
    //     User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
    //     List<Resume> resumes = resumeRepository.findByUser(user);

    //     List<ResumeWithSkillsDTO> resumeDTOs = new ArrayList<>();
    //     for (Resume resume : resumes) {
    //         // Pass yearsOfExperience as double
    //         ResumeWithSkillsDTO dto = new ResumeWithSkillsDTO(
    //             resume.getId(),
    //             resume.getName(),
    //             resume.getPhoneNumber(),
    //             resume.getEmail(),
    //             resume.getYearsOfExperience(),  // No casting needed for double
    //             resume.getSkills().toString()
    //         );
    //         resumeDTOs.add(dto);
    //     }
    //     return resumeDTOs;
    // }


    // test method
    public List<ResumeWithSkillsDTO> getResumesByUserId(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Resume> resumes = resumeRepository.findByUser(user);
        List<ResumeWithSkillsDTO> resumeDTOs = new ArrayList<>();
    
        for (Resume resume : resumes) {
            List<String> skillNames = resume.getSkills().stream()
                .map(Skill::getName)  // Extract skill names
                .collect(Collectors.toList());
    
            ResumeWithSkillsDTO dto = new ResumeWithSkillsDTO(
                resume.getId(),
                resume.getName(),
                resume.getPhoneNumber(),
                resume.getEmail(),
                resume.getYearsOfExperience(),
                skillNames
            );
    
            resumeDTOs.add(dto);
        }
    
        return resumeDTOs;
    }


    // function that saves resume with userid
//     @Transactional
// public void uploadResumeForUser(Long userId, MultipartFile file) {
//     try {
//         // Extract resume content and details
//         String content = extractTextFromFile(file);
//         String name = extractName(content);
//         String email = extractEmail(content);
//         String phone = extractPhone(content);
//         String experienceStr = extractExperience(content);
//         List<String> skillsList = extractSkills(content);

//         double experience = extractExperienceAsDouble(experienceStr);

//         // Find the user by ID
//         User user = userRepository.findById(userId)
//             .orElseThrow(() -> new RuntimeException("User not found"));

//         // Create a new Resume object
//         Resume resume = new Resume();
//         resume.setName(name);
//         resume.setEmail(email);
//         resume.setPhoneNumber(phone);
//         resume.setYearsOfExperience(experience);
//         resume.setUser(user);  // Associate with the user

//         // Save the skills
//         for (String skillName : skillsList) {
//             Skill skill = skillRepository.findByName(skillName)
//                     .orElseGet(() -> new Skill(skillName));
//             resume.addSkill(skill);
//         }

//         // Save the resume
//         resumeRepository.save(resume);
//         System.out.println("Resume uploaded and associated with user: " + name);

//     } catch (Exception e) {
//         e.printStackTrace();
//     }
// }



// new upload function from albin
public Resume uploadResumeForUser(Long userId,MultipartFile file) throws Exception {
    InputStream is = file.getInputStream();
    PDDocument document = PDDocument.load(is);
    String text = new PDFTextStripper().getText(document);
    String lowerText = text.toLowerCase();
    String[] lines = text.split("\n");

    String email = extractRegex(text, "[\\w\\.-]+@[\\w\\.-]+", "Email not found");
    String phone = extractRegex(text, "(\\+?\\d{1,3}[-.\\s]?)?(\\(?\\d{1,4}\\)?[-.\\s]?){1,5}\\d{1,4}", "Phone not found");
    double experience = extractExperienceYears(text);
    // String skills = extractSkills(lowerText);
    List<String> skillsList = extractSkills(text);
    //         // Find the user by ID
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    String name = "Name not found";
    FontAwarePDFStripper fontStripper = new FontAwarePDFStripper();
    fontStripper.setStartPage(1);
    fontStripper.setEndPage(1);
    fontStripper.getText(document);
    String fontDetectedName = fontStripper.extractLargestText();
    if (!"Name not found".equals(fontDetectedName)) {
        name = fontDetectedName;
    } else {
        String emailLower = email.toLowerCase();
        for (int i = 0; i < lines.length; i++) {
            String cleanedLine = lines[i].replaceAll("[^\\p{Print}]", "").toLowerCase();
            if (!emailLower.isEmpty() && cleanedLine.contains(emailLower)) {
                int start = Math.max(0, i - 5);
                for (int j = i - 1; j >= start; j--) {
                    String candidate = lines[j].trim();
                    if (candidate.matches("[A-Za-z ]{3,40}")) {
                        int wordCount = candidate.split("\\s+").length;
                        if (wordCount >= 2 && wordCount <= 3) {
                            name = candidate;
                            break;
                        }
                    }
                }
                if (!"Name not found".equals(name))
                    break;
            }
        }
    }
    //  Save the skills
 

    document.close();

    Resume resume = new Resume();
    resume.setName(name);
    resume.setEmail(email);
    resume.setUser(user); 
    resume.setPhoneNumber(phone);
    resume.setYearsOfExperience(experience);
    for (String skillName : skillsList) {
        Skill skill = skillRepository.findByName(skillName)
                .orElseGet(() -> new Skill(skillName));
        resume.addSkill(skill);
    }
  

    return resumeRepository.save(resume);
}
public Resume getResumeById(Long resumeId) {
    return resumeRepository.findById(resumeId)
            .orElse(null);
}





















































// get filtered resumes for specific user
// public List<ResumeWithSkillsDTO> filterResumesByUser(Long userId, List<String> skillNames, double minExperience) {
//     User user = userRepository.findById(userId)
//         .orElseThrow(() -> new RuntimeException("User not found"));

//     List<Resume> userResumes = resumeRepository.findByUser(user);

//     return userResumes.stream()
//         .filter(resume -> resume.getYearsOfExperience() >= minExperience)
//         .filter(resume -> {
//             List<String> resumeSkillNames = resume.getSkills().stream()
//                 .map(Skill::getName)
//                 .map(String::toLowerCase)
//                 .collect(Collectors.toList());
//             return skillNames.stream()
//                 .allMatch(s -> resumeSkillNames.contains(s.toLowerCase()));
//         })
//         .map(resume -> {
//             List<String> skillList = resume.getSkills().stream()
//                 .map(Skill::getName)
//                 .collect(Collectors.toList());

//             return new ResumeWithSkillsDTO(
//                 resume.getId(),
//                 resume.getName(),
//                 resume.getPhoneNumber(),
//                 resume.getEmail(),
//                 resume.getYearsOfExperience(),
//                 skillList
//             );
//         })
//         .collect(Collectors.toList());
// }


// deklete users resume with id


public void deleteResumesByUserId(Long userId) {
    User user = userRepository.findById(userId)
                  .orElseThrow(() -> new RuntimeException("User not found"));
    resumeRepository.deleteAllByUserId(userId);




}



private String extractRegex(String text, String regex, String defaultValue) {
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(text);
    return matcher.find() ? matcher.group() : defaultValue;
}

private double extractExperienceYears(String text) {
    String[] lines = text.split("\n");
    List<String> experienceKeywords = Arrays.asList("experience", "work experience", "professional experience");

    Pattern pattern = Pattern.compile("(\\d+)\\s*(years|year|yrs|yr)?\\s*(and)?\\s*(\\d+)?\\s*(months|month|mos|mo)?", Pattern.CASE_INSENSITIVE);

    for (int i = 0; i < lines.length; i++) {
        String lineLower = lines[i].toLowerCase();
        boolean containsKeyword = experienceKeywords.stream().anyMatch(lineLower::contains);

        if (containsKeyword) {
            Matcher matcher = pattern.matcher(lineLower);
            if (matcher.find()) {
                int years = matcher.group(1) != null ? Integer.parseInt(matcher.group(1)) : 0;
                int months = matcher.group(4) != null ? Integer.parseInt(matcher.group(4)) : 0;
                return years + (months / 12.0);
            }

            if (i + 1 < lines.length) {
                String nextLine = lines[i + 1].toLowerCase();
                Matcher matcherNext = pattern.matcher(nextLine);
                if (matcherNext.find()) {
                    int years = matcherNext.group(1) != null ? Integer.parseInt(matcherNext.group(1)) : 0;
                    int months = matcherNext.group(4) != null ? Integer.parseInt(matcherNext.group(4)) : 0;
                    return years + (months / 12.0);
                }
            }
        }
    }

    return 0;
}

// private String extractSkills(String lowerText) {
//     Set<String> foundSkills = new HashSet<>();
//     for (String skill : KNOWN_SKILLS) {
//         if (lowerText.contains(skill)) {
//             foundSkills.add(skill);
//         }
//     }
//     return String.join(", ", foundSkills);
// }




}


































































