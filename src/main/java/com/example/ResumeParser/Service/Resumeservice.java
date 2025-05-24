// package com.example.ResumeParser.Service;

// import org.apache.pdfbox.pdmodel.PDDocument;
// import org.apache.pdfbox.text.PDFTextStripper;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import org.springframework.web.multipart.MultipartFile;
// import com.example.ResumeParser.dto.ResumeWithSkillsDTO;
// import com.example.ResumeParser.entity.Knownskill;
// import com.example.ResumeParser.entity.Resume;
// import com.example.ResumeParser.entity.Skill;
// import com.example.ResumeParser.entity.User;
// import com.example.ResumeParser.repository.Knownskillrepository;
// import com.example.ResumeParser.repository.Resumerepository;
// import com.example.ResumeParser.repository.Skillrepository;
// import com.example.ResumeParser.repository.UserRepository;

// import jakarta.transaction.Transactional;

// import java.io.IOException;
// import java.io.InputStream;
// import java.util.regex.Matcher;
// import java.util.regex.Pattern;
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.Arrays;
// import java.util.HashSet;
// import java.util.List;
// import java.util.Set;
// import java.util.stream.Collectors;

// @Service
// public class Resumeservice {

//     @Autowired
//     private Resumerepository resumeRepository;

//     @Autowired
//     private Skillrepository skillRepository;

//     @Autowired
//     private Knownskillrepository knownskillrepo;

//     @Autowired
//     private UserRepository userRepository;

//     public List<ResumeWithSkillsDTO> filterResumes(List<String> skills, double minExp, Long userId) {
//        System.out.println("hi from filter resumes");
        
//         int skillCount = skills.size();
//         List<Object[]> results = resumeRepository.filterBySkillsAndExperience(skills, minExp,skillCount, userId);

//         List<ResumeWithSkillsDTO> finalList = new ArrayList<>();
//         for (Object[] row : results) {
//             ResumeWithSkillsDTO dto = new ResumeWithSkillsDTO(
//                 ((Number) row[0]).longValue(),
//                 (String) row[1],
//                 (String) row[2],
//                 (String) row[3],
//                 ((Number) row[4]).doubleValue(),
//                 Arrays.asList(((String) row[5]).split(",")) // split skills string into list
//             );
//             finalList.add(dto);
//         }

//         return finalList;
//     }

    



//     private String extractExperience(String text) {
//         Matcher matcher = Pattern.compile("(\\d+)\\+?\\s+years?\\s+of\\s+experience", Pattern.CASE_INSENSITIVE).matcher(text);
//         return matcher.find() ? matcher.group(1) + " years" : "Not Found";
//     }

//     private List<String> extractSkills(String text) {
//         List<String> knownSkills = knownskillrepo.findAll()
//                                 .stream()
//                                 .map(Knownskill::getName)
//                                 .collect(Collectors.toList());

//         return knownSkills.stream()
//                 .filter(skill -> text.toLowerCase().contains(skill.toLowerCase()))
//                 .collect(Collectors.toList());
//    }



    
//     // test method
//     public List<ResumeWithSkillsDTO> getResumesByUserId(Long userId) {
//         User user = userRepository.findById(userId)
//             .orElseThrow(() -> new RuntimeException("User not found"));
        
//         List<Resume> resumes = resumeRepository.findByUser(user);
//         List<ResumeWithSkillsDTO> resumeDTOs = new ArrayList<>();
    
//         for (Resume resume : resumes) {
//             List<String> skillNames = resume.getSkills().stream()
//                 .map(Skill::getName)  // Extract skill names
//                 .collect(Collectors.toList());
    
//             ResumeWithSkillsDTO dto = new ResumeWithSkillsDTO(
//                 resume.getId(),
//                 resume.getName(),
//                 resume.getPhoneNumber(),
//                 resume.getEmail(),
//                 resume.getYearsOfExperience(),
//                 skillNames
//             );
    
//             resumeDTOs.add(dto);
//         }
    
//         return resumeDTOs;
//     }


    

// // new upload function from albin
// public Resume uploadResumeForUser(Long userId,MultipartFile file) throws Exception {
//     InputStream is = file.getInputStream();
//     PDDocument document = PDDocument.load(is);
//     String text = new PDFTextStripper().getText(document);
//     String lowerText = text.toLowerCase();
//     String[] lines = text.split("\n");

//     String email = extractRegex(text, "[\\w\\.-]+@[\\w\\.-]+", "Email not found");
//     String phone = extractRegex(text, "(\\+?\\d{1,3}[-.\\s]?)?(\\(?\\d{1,4}\\)?[-.\\s]?){1,5}\\d{1,4}", "Phone not found");
//     double experience = extractExperienceYears(text);
//     // String skills = extractSkills(lowerText);
//     List<String> skillsList = extractSkills(text);
//     //         // Find the user by ID
//         User user = userRepository.findById(userId)
//             .orElseThrow(() -> new RuntimeException("User not found"));

//     String name = "Name not found";
//     FontAwarePDFStripper fontStripper = new FontAwarePDFStripper();
//     fontStripper.setStartPage(1);
//     fontStripper.setEndPage(1);
//     fontStripper.getText(document);
//     String fontDetectedName = fontStripper.extractLargestText();
//     if (!"Name not found".equals(fontDetectedName)) {
//         name = fontDetectedName;
//     } else {
//         String emailLower = email.toLowerCase();
//         for (int i = 0; i < lines.length; i++) {
//             String cleanedLine = lines[i].replaceAll("[^\\p{Print}]", "").toLowerCase();
//             if (!emailLower.isEmpty() && cleanedLine.contains(emailLower)) {
//                 int start = Math.max(0, i - 5);
//                 for (int j = i - 1; j >= start; j--) {
//                     String candidate = lines[j].trim();
//                     if (candidate.matches("[A-Za-z ]{3,40}")) {
//                         int wordCount = candidate.split("\\s+").length;
//                         if (wordCount >= 2 && wordCount <= 3) {
//                             name = candidate;
//                             break;
//                         }
//                     }
//                 }
//                 if (!"Name not found".equals(name))
//                     break;
//             }
//         }
//     }
//     //  Save the skills
 

//     document.close();

//     Resume resume = new Resume();
//     resume.setName(name);
//     resume.setEmail(email);
//     resume.setUser(user); 
//     resume.setPhoneNumber(phone);
//     resume.setYearsOfExperience(experience);
//     for (String skillName : skillsList) {
//         Skill skill = skillRepository.findByName(skillName)
//                 .orElseGet(() -> new Skill(skillName));
//         resume.addSkill(skill);
//     }
  

//     return resumeRepository.save(resume);
// }
// public Resume getResumeById(Long resumeId) {
//     return resumeRepository.findById(resumeId)
//             .orElse(null);
// }




// public void deleteResumesByUserId(Long userId) {
//     User user = userRepository.findById(userId)
//                   .orElseThrow(() -> new RuntimeException("User not found"));
//     resumeRepository.deleteAllByUserId(userId);



// }



// private String extractRegex(String text, String regex, String defaultValue) {
//     Pattern pattern = Pattern.compile(regex);
//     Matcher matcher = pattern.matcher(text);
//     return matcher.find() ? matcher.group() : defaultValue;
// }

// private double extractExperienceYears(String text) {
//     String[] lines = text.split("\n");
//     List<String> experienceKeywords = Arrays.asList("experience", "work experience", "professional experience");

//     Pattern pattern = Pattern.compile("(\\d+)\\s*(years|year|yrs|yr)?\\s*(and)?\\s*(\\d+)?\\s*(months|month|mos|mo)?", Pattern.CASE_INSENSITIVE);

//     for (int i = 0; i < lines.length; i++) {
//         String lineLower = lines[i].toLowerCase();
//         boolean containsKeyword = experienceKeywords.stream().anyMatch(lineLower::contains);

//         if (containsKeyword) {
//             Matcher matcher = pattern.matcher(lineLower);
//             if (matcher.find()) {
//                 int years = matcher.group(1) != null ? Integer.parseInt(matcher.group(1)) : 0;
//                 int months = matcher.group(4) != null ? Integer.parseInt(matcher.group(4)) : 0;
//                 return years + (months / 12.0);
//             }

//             if (i + 1 < lines.length) {
//                 String nextLine = lines[i + 1].toLowerCase();
//                 Matcher matcherNext = pattern.matcher(nextLine);
//                 if (matcherNext.find()) {
//                     int years = matcherNext.group(1) != null ? Integer.parseInt(matcherNext.group(1)) : 0;
//                     int months = matcherNext.group(4) != null ? Integer.parseInt(matcherNext.group(4)) : 0;
//                     return years + (months / 12.0);
//                 }
//             }
//         }
//     }

//     return 0;
// }



// }




package com.example.ResumeParser.Service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.rendering.PDFRenderer;

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

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        int skillCount = skills.size();
        List<Object[]> results = resumeRepository.filterBySkillsAndExperience(skills, minExp, skillCount, userId);

        List<ResumeWithSkillsDTO> finalList = new ArrayList<>();
        for (Object[] row : results) {
            ResumeWithSkillsDTO dto = new ResumeWithSkillsDTO(
                ((Number) row[0]).longValue(),
                (String) row[1],
                (String) row[2],
                (String) row[3],
                ((Number) row[4]).doubleValue(),
                Arrays.asList(((String) row[5]).split(","))
            );
            finalList.add(dto);
        }
        return finalList;
    }

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

    public List<ResumeWithSkillsDTO> getResumesByUserId(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        List<Resume> resumes = resumeRepository.findByUser(user);
        List<ResumeWithSkillsDTO> resumeDTOs = new ArrayList<>();

        for (Resume resume : resumes) {
            List<String> skillNames = resume.getSkills().stream()
                .map(Skill::getName)
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

    public Resume uploadResumeForUser(Long userId, MultipartFile file) throws Exception {
        InputStream is = file.getInputStream();
        PDDocument document = PDDocument.load(is);
        String text = new PDFTextStripper().getText(document);
        String[] lines = text.split("\n");

        String email = extractRegex(text, "[\\w\\.-]+@[\\w\\.-]+", "Email not found");
        String phone = extractRegex(text, "(\\+?\\d{1,3}[-.\\s]?)?(\\(?\\d{1,4}\\)?[-.\\s]?){1,5}\\d{1,4}", "Phone not found");
        double experience = extractExperienceYears(text);
        List<String> skillsList = extractSkills(text);

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
                    if (!"Name not found".equals(name)) break;
                }
            }
        }

        // Convert 1st page to image
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        BufferedImage image = pdfRenderer.renderImageWithDPI(0, 300);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();

        document.close();

        Resume resume = new Resume();
        resume.setName(name);
        resume.setEmail(email);
        resume.setUser(user);
        resume.setPhoneNumber(phone);
        resume.setYearsOfExperience(experience);
        resume.setResumeImage(imageBytes); // ✅ Set resume image here

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
}
