package com.example.ResumeParser.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.ResumeParser.dto.ResumeData;
import com.example.ResumeParser.dto.ResumeWithSkillsDTO;  // example package path, adjust accordingly
import com.example.ResumeParser.entity.Knownskill;
import com.example.ResumeParser.entity.Resume;
import com.example.ResumeParser.entity.Skill;
import com.example.ResumeParser.entity.User;
import com.example.ResumeParser.repository.Knownskillrepository;
import com.example.ResumeParser.repository.Resumerepository;
import com.example.ResumeParser.repository.Skillrepository;
import com.example.ResumeParser.repository.UserRepository;

import jakarta.transaction.Transactional;

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



    // ====== Resume parsing and extraction methods ======

    public String extractName(String text) {
        String[] lines = text.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.matches("^[A-Za-z ,.'-]+$") && line.length() < 40) {
                return line;
            }
        }
        return "Not Found";
    }

    public String extractEmail(String text) {
        Pattern pattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return "Not Found";
    }

    public String extractPhoneNumber(String text) {
        Pattern pattern = Pattern.compile("(\\+\\d{1,3}[- ]?)?\\d{10}");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return "Not Found";
    }

    public String extractExperience(String text) {
        Pattern pattern = Pattern.compile(
            "(\\b\\w{3,9}\\b\\s*\\d{4})\\s*(?:to|\\-|–|—)\\s*(\\b(?:present|current|\\w{3,9}\\s*\\d{4})\\b)",
            Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text.toLowerCase());

        double totalMonths = 0;

        while (matcher.find()) {
            String start = matcher.group(1);
            String end = matcher.group(2);

            try {
                LocalDate startDate = parseDate(start);
                LocalDate endDate;

                if (end.contains("present") || end.contains("current")) {
                    endDate = LocalDate.now();
                } else {
                    endDate = parseDate(end);
                }

                Period period = Period.between(startDate, endDate);
                int months = period.getYears() * 12 + period.getMonths();
                totalMonths += months;

            } catch (Exception e) {
                // ignore invalid dates
            }
        }

        if (totalMonths == 0) return "Not Found";

        double totalYears = totalMonths / 12.0;
        return String.format("%.1f years", totalYears);
    }

    private LocalDate parseDate(String dateStr) throws Exception {
        List<DateTimeFormatter> formatters = List.of(
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy")
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                return YearMonth.parse(dateStr.trim(), formatter).atDay(1);
            } catch (DateTimeParseException ignored) {}
        }

        throw new Exception("Date parsing failed for: " + dateStr);
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

    // ====== Parsing PDF and extracting resume data ======

    public ResumeData parseResumeFile(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            String text = new PDFTextStripper().getText(document);

        ResumeData data = new ResumeData();
data.setFileName(file.getOriginalFilename());
data.setName(extractName(text));
data.setEmail(extractEmail(text));
data.setPhoneNumber(extractPhoneNumber(text));
String expStr = extractExperience(text); // "3.5 years" or "Not Found"
double years = 0;
try {
    years = Double.parseDouble(expStr.split(" ")[0]);
} catch (Exception e) {
    // if parsing fails, default 0
}
data.setYearsOfExperience(years);
data.setSkills(String.join(", ", extractSkills(text)));

            return data;
        }
    }

    // ====== Database interaction methods ======

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

    // Upload resume, parse and save to DB for given user
    @Transactional
    public Resume uploadResumeForUser(Long userId, MultipartFile file) throws Exception {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        try (InputStream is = file.getInputStream();
             PDDocument document = PDDocument.load(is)) {
            String text = new PDFTextStripper().getText(document);

            Resume resume = new Resume();
            resume.setUser(user);
            resume.setName(extractName(text));
            resume.setEmail(extractEmail(text));
            resume.setPhoneNumber(extractPhoneNumber(text));
            
            String experienceStr = extractExperience(text);
            // parse years from string like "3.5 years"
            double years = 0;
            try {
                years = Double.parseDouble(experienceStr.split(" ")[0]);
            } catch (Exception e) {
                // default 0
            }
            resume.setYearsOfExperience(years);

            List<String> skillNames = extractSkills(text);
            Set<Skill> skills = skillNames.stream()
                .map(skillName -> skillRepository.findByName(skillName)
                    .orElseGet(() -> skillRepository.save(new Skill(skillName))))
                .collect(Collectors.toSet());
            resume.setSkills(skills);

            resume.setFileName(file.getOriginalFilename());

            return resumeRepository.save(resume);
        }
    }
  @Transactional
    public void deleteResumesByUserId(Long userId) {
        resumeRepository.deleteAllByUserId(userId);
    }

}
