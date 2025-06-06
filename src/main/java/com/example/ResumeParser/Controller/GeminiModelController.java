package com.example.ResumeParser.Controller;

import com.example.ResumeParser.entity.DomainExperience;
import com.example.ResumeParser.entity.Resume;
import com.example.ResumeParser.entity.Skill;
import com.example.ResumeParser.entity.User;
import com.example.ResumeParser.repository.Resumerepository;
import com.example.ResumeParser.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class GeminiModelController {

    private static final Logger log = LoggerFactory.getLogger(GeminiModelController.class);

    @Value("${spring.ai.openai.api-key}")
    private String GEMINI_API_KEY;

    private final WebClient webClient;
    private final UserRepository userRepository;
    private final Resumerepository resumeRepository;

    public GeminiModelController(Resumerepository resumeRepository, UserRepository userRepository) {
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
        log.info("GeminiModelController initialized");
    }

    @PostMapping("/resumes/upload/{userId}")
    public ResponseEntity<?> promptWithPdf(@RequestParam("file") MultipartFile file,
                                           @PathVariable Long userId) {

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            log.warn("User not found with id: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found with id: " + userId));
        }
        User user = userOpt.get();

        String extractedText;
        try {
            extractedText = extractTextFromPdfWithTika(file);
        } catch (IOException | TikaException e) {
            log.error("Error extracting text from PDF", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to extract text from PDF: " + e.getMessage()));
        }

        String prompt = buildPrompt() + "\n\n--- PDF Content ---\n" + extractedText;

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)))));

        Mono<String> responseMono = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/models/gemini-2.0-flash:generateContent")
                        .queryParam("key", GEMINI_API_KEY)
                        .build())
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class);

        try {
            String responseBody = responseMono.block(); // blocking here for demo - switch to reactive in prod

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);

            JsonNode candidateNode = root.path("candidates");
            if (!candidateNode.isArray() || candidateNode.size() == 0) {
                log.error("No candidates found in Gemini response");
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body(Map.of("error", "Gemini returned empty candidates"));
            }

            JsonNode textNode = candidateNode.get(0).path("content").path("parts");
            if (!textNode.isArray() || textNode.size() == 0) {
                log.error("No parts found in Gemini response");
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body(Map.of("error", "Gemini returned empty content parts"));
            }

            String jsonText = textNode.get(0).path("text").asText();

            // Clean up any markdown JSON blocks Gemini might add
            jsonText = jsonText.replaceAll("(?s)```json\\s*", "").replaceAll("```", "").trim();

            if (!isValidJson(jsonText)) {
                log.error("Gemini returned non-JSON output: {}", jsonText);
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body(Map.of("error", "Gemini returned non-JSON output", "raw", jsonText));
            }

            Map<String, Object> cleanJson = mapper.readValue(jsonText, Map.class);
            Resume resume = mapToResume(cleanJson, user);
            Resume saved = resumeRepository.save(resume);

            return ResponseEntity.ok(Map.of("message", "Resume saved", "id", saved.getId()));

        } catch (Exception e) {
            log.error("Failed to process Gemini response", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process Gemini response", "details", e.getMessage()));
        }
    }

    private boolean isValidJson(String json) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(json);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private Resume mapToResume(Map<String, Object> json, User user) {
        Resume resume = new Resume();
        resume.setUser(user);

        // Defensive casts with null checks
        resume.setName((String) json.getOrDefault("name", null));
        resume.setEmail((String) json.getOrDefault("email", null));
        resume.setPhone((String) json.getOrDefault("phone number", null));

        Object expYearsObj = json.get("experience_years");
        if (expYearsObj != null) {
            resume.setTotalExperienceYears(Double.valueOf(expYearsObj.toString()));
        }

        // Skills parsing
        List<String> skillsList = (List<String>) json.get("skills");
        if (skillsList != null) {
            List<Skill> skills = skillsList.stream().map(s -> {
                Skill skill = new Skill();
                skill.setSkillName(s);
                skill.setResume(resume);
                return skill;
            }).collect(Collectors.toList());
            resume.setSkills(skills);
        }

        // Experience parsing
        List<Map<String, Object>> expList = (List<Map<String, Object>>) json.get("experience");
        if (expList != null) {
            List<DomainExperience> experience = expList.stream().map(exp -> {
                DomainExperience d = new DomainExperience();
                d.setJobTitle((String) exp.get("title"));
                d.setSkill((String) exp.get("specializationskills"));
                Object durObj = exp.get("duration_years");
                if (durObj != null) {
                    d.setYears(Double.valueOf(durObj.toString()));
                }
                d.setResume(resume);
                return d;
            }).collect(Collectors.toList());
            resume.setExperiences(experience);
        }

        return resume;
    }

    private String extractTextFromPdfWithTika(MultipartFile file) throws IOException, TikaException {
        Tika tika = new Tika();
        return tika.parseToString(file.getInputStream());
    }

    private String buildPrompt() {
        return "From the following resume text, extract the following structured JSON data:\n\n" +
                "1. Basic Information:\n" +
                "- \"name\": Full name of the person.\n" +
                "- \"email\": Email of the person.\n" +
                "- \"phone number\": Phone number of the person.\n" +
                "- \"experience_years\": This must be the *exact sum* of all \"duration_years\" values from the detailed work experience section below. "
                +
                "Only include full-time job roles (skip internships, OJT, or training). Round final total to the nearest 0.5 (e.g., 1.0, 2.5).\n"
                +
                "- \"skills\": List of technical skills only (languages, frameworks, tools). Skip soft skills or vague terms.\n\n"
                +

                "2. Detailed Work Experience:\n" +
                "Only include full-time roles (exclude internships, training, OJT). For each valid job, extract:\n" +
                "- \"title\": Job title\n" +
                "- \"company\": Company name\n" +
                "- \"specializationskills\": Primary technical skill used (single value only). Choose using this logic:\n"
                +
                "   → If job mentions 'Spring Boot', set to 'Spring Boot'\n" +
                "   → Else if job mentions 'Java' (and not Spring Boot), set to 'Java'\n" +
                "   → Else if job mentions 'Flutter', set to 'Flutter'\n" +
                "   → Else if job mentions React, Angular, or Vue, set to most used JS framework\n" +
                "   → Else if job involves Pandas, NumPy, Machine Learning, etc., set to 'Python'\n" +
                "   → Else use best guess based on tools and tech\n" +
                "- \"location\": City and country if available, otherwise null\n" +
                "- \"start_date\": Format as 'Month Year'\n" +
                "- \"end_date\": Format as 'Month Year' or 'Present'\n" +
                "- \"duration_years\": Float value (in years), rounded to 1 decimal place. Use June 2025 if end date is 'Present'\n\n"
                +

                "*Rules:*\n" +
                "- DO NOT include internships, apprenticeships, training programs, or OJT.\n" +
                "- Make sure the sum of all \"duration_years\" equals the \"experience_years\" field.\n" +
                "- Round each individual \"duration_years\" to 1 decimal place\n" +
                "- Round final \"experience_years\" total to the nearest 0.5 only\n" +
                "    → e.g., 0.4 → 0.5, 1.3 → 1.5, 2.6 → 2.5, 3.75 → 4.0\n\n" +

                "Return strictly in this JSON format, nothing else:\n" +
                "{\n" +
                "  \"name\": \"Example Name\",\n" +
                "  \"email\": \"example@email.com\",\n" +
                "  \"phone number\": \"+91-1234567890\",\n" +
                "  \"experience_years\": 2.5,\n" +
                "  \"skills\": [\"Java\", \"Spring Boot\", \"SQL\", \"Docker\"],\n" +
                "  \"experience\": [\n" +
                "    {\n" +
                "      \"title\": \"Backend Developer\",\n" +
                "      \"company\": \"XYZ Ltd\",\n" +
                "      \"specializationskills\": \"Spring Boot\",\n" +
                "      \"location\": \"Bangalore, India\",\n" +
                "      \"start_date\": \"Jan 2022\",\n" +
                "      \"end_date\": \"June 2025\",\n" +
                "      \"duration_years\": 3.5\n" +
                "    },\n" +
                "    {\n" +
                "      \"title\": \"Junior Developer\",\n" +
                "      \"company\": \"ABC Solutions\",\n" +
                "      \"specializationskills\": \"Java\",\n" +
                "      \"location\": null,\n" +
                "      \"start_date\": \"Jan 2020\",\n" +
                "      \"end_date\": \"Dec 2021\",\n" +
                "      \"duration_years\": 2.0\n" +
                "    }\n" +
                "  ]\n" +
                "}\n\n" +
                "--- Resume Text ---\n";
    }
}