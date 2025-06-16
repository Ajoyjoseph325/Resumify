package com.example.ResumeParser.Controller;

import com.example.ResumeParser.entity.*;
import com.example.ResumeParser.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
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
    private final PreviewRepository previewRepository;

    public GeminiModelController(Resumerepository resumeRepository, UserRepository userRepository, PreviewRepository previewRepository) {
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.previewRepository = previewRepository;
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    @PostMapping("/resumes/upload/{userId}")
    public ResponseEntity<?> uploadMultipleResumes(@RequestParam("file") MultipartFile[] files,
                                                   @PathVariable Long userId) {

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }
        User user = userOpt.get();

        List<Map<String, Object>> results = new ArrayList<>();

        for (MultipartFile file : files) {
            String filename = file.getOriginalFilename();
            try (InputStream is = file.getInputStream(); PDDocument doc = PDDocument.load(is)) {

                String extractedText = extractTextFromPdfWithTika(file);
                byte[] imageBytes = renderFirstPageToImage(doc);
                String prompt = buildPrompt() + "\n\n--- Resume Text ---\n" + extractedText;

                Map<String, Object> body = Map.of(
                        "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
                );

                String response = webClient.post()
                        .uri(uriBuilder -> uriBuilder
                                .path("/v1/models/gemini-2.0-flash:generateContent")
                                .queryParam("key", GEMINI_API_KEY)
                                .build())
                        .header(HttpHeaders.CONTENT_TYPE, "application/json")
                        .bodyValue(body)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response);
                JsonNode textNode = root.path("candidates").get(0).path("content").path("parts").get(0).path("text");

                String jsonText = textNode.asText().replaceAll("(?s)```json\\s*", "").replaceAll("```", "").trim();
                log.info("Gemini Response JSON: {}", jsonText);

                if (!isValidJson(jsonText)) {
                    results.add(Map.of("file", filename, "error", "Invalid JSON format"));
                    continue;
                }

                JsonNode jsonRoot = mapper.readTree(jsonText);
                Map<String, Object> parsedMap = mapper.readValue(jsonText, Map.class);

                Resume resume = mapToResume(parsedMap, user, imageBytes);
                Resume savedResume = resumeRepository.save(resume);

                Preview preview = buildPreviewFromJson(jsonRoot, savedResume, user, imageBytes);
                previewRepository.save(preview);

                results.add(Map.of("file", filename, "message", "Uploaded & processed", "resumeId", savedResume.getId()));

            } catch (Exception e) {
                log.error("Failed to process file: {}", filename, e);
                results.add(Map.of("file", filename, "error", e.getMessage()));
            }
        }

        return ResponseEntity.ok(results);
    }

    private Resume mapToResume(Map<String, Object> json, User user, byte[] imageBytes) {
        Resume resume = new Resume();
        resume.setUser(user);
        resume.setResumeImage(imageBytes);
        resume.setName((String) json.get("name"));
        resume.setEmail((String) json.get("email"));
        resume.setPhone((String) json.get("phone number"));
        resume.setJobPosition((String) json.get("job_position"));


        Object expYears = json.get("experience_years");
        if (expYears != null) {
            resume.setTotalExperienceYears(Double.parseDouble(expYears.toString()));
        }

        List<Skill> skills = new ArrayList<>();
        List<?> skillsJson = (List<?>) json.get("skills");
        if (skillsJson != null) {
            for (Object s : skillsJson) {
                Skill skill = new Skill();
                skill.setSkillName((String) s);
                skill.setResume(resume);
                skills.add(skill);
            }
        }
        resume.setSkills(skills);

        List<DomainExperience> experiences = new ArrayList<>();
        List<?> experienceJson = (List<?>) json.get("experience");
        if (experienceJson != null) {
            for (Object obj : experienceJson) {
                Map<?, ?> e = (Map<?, ?>) obj;
                DomainExperience d = new DomainExperience();
                d.setJobTitle((String) e.get("title"));
                d.setSkill((String) e.get("specializationskills"));
                Object y = e.get("duration_years");
                if (y != null) d.setYears(Double.parseDouble(y.toString()));
                d.setResume(resume);
                experiences.add(d);
            }
        }
        resume.setExperiences(experiences);

        return resume;
    }

    private Preview buildPreviewFromJson(JsonNode root, Resume resume, User user, byte[] imageBytes) {
        Preview preview = new Preview();
        preview.setName(root.path("name").asText(null));
        preview.setEmail(root.path("email").asText(null));
        preview.setPhone(root.path("phone number").asText(null));
        preview.setLinkedin(root.path("linkedin").asText(null));
        preview.setGithub(root.path("github").asText(null));
        preview.setProfileSummary(root.path("profile_summary").asText(null));
        preview.setDescription(root.path("description").asText(null));
        preview.setJobFit(root.path("job_fit").asText(null));
        preview.setOverallScore(root.path("overall_score").asDouble(0));
        preview.setSpecializedJob(root.path("specialized_job").asText(null));
        preview.setJobPosition(root.path("job_position").asText(null));
        preview.setImageUrl("http://localhost:8080/resumes/image/" + resume.getId());
        preview.setResume(resume);
        preview.setUser(user);

        if (root.has("skills") && root.get("skills").isArray()) {
            List<String> skills = new ArrayList<>();
            for (JsonNode s : root.get("skills")) {
                skills.add(s.asText());
            }
            preview.setSkills(skills);
        }

        if (root.has("education") && root.get("education").isArray()) {
            List<EducationEntry> educationList = new ArrayList<>();
            for (JsonNode edu : root.get("education")) {
                educationList.add(new EducationEntry(
                        edu.path("degree").asText(null),
                        edu.path("institute").asText(null),
                        edu.path("start_year").asText(null),
                        edu.path("end_year").asText(null)
                ));
            }
            preview.setEducation(educationList);
        }

        if (root.has("experience") && root.get("experience").isArray()) {
            List<ExperienceEntry> expList = new ArrayList<>();
            for (JsonNode exp : root.get("experience")) {
                expList.add(new ExperienceEntry(
                        exp.path("title").asText(null),
                        exp.path("company").asText(null),
                        exp.path("specializationskills").asText(null),
                        exp.path("duration_years").asDouble(0)
                ));
            }
            preview.setExperience(expList);
        }

        return preview;
    }

    private String extractTextFromPdfWithTika(MultipartFile file) throws IOException, TikaException {
        try (InputStream is = file.getInputStream()) {
            return new Tika().parseToString(is);
        }
    }

    private byte[] renderFirstPageToImage(PDDocument document) throws IOException {
        PDFRenderer renderer = new PDFRenderer(document);
        BufferedImage image = renderer.renderImageWithDPI(0, 300);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        }
    }

    private boolean isValidJson(String json) {
        try {
            new ObjectMapper().readTree(json);
            return true;
        } catch (IOException e) {
            return false;
        }
    }



   private String buildPrompt() {
    return "From the following resume text, extract the following structured JSON data:\n\n" +

            "1. Personal Details:\n" +
            "- \"name\": Full name of the person.\n" +
            "- \"email\": Email address.\n" +
            "- \"phone number\": Phone number.\n" +
            "- \"linkedin\": LinkedIn profile URL if present.\n" +
            "- \"github\": GitHub profile URL if present.\n" +
            "- \"profile_summary\": 3–4 sentence summary in professional tone.\n" +
            "- \"job_fit\": One-line summary of suitable job roles.\n" +
            "- \"overall_score\": Resume score (1–100 based on completeness, relevance, skills).\n" +
            "- \"job_position\": Inferred job title or role (e.g., Backend Developer).\n" +
            "- \"experience_years\": Exact sum of all full-time job durations. Round total to nearest 0.5 (e.g., 1.0, 2.5).\n\n" +

            "2. Skills:\n" +
            "- \"skills\": List of technical skills (languages, frameworks, tools). Exclude soft skills or general terms.\n\n" +

            "3. Detailed Work Experience:\n" +
            "- Only include full-time roles (skip internships, OJT, or training).\n" +
            "- For each job, extract:\n" +
            "  - \"title\": Job title\n" +
            "  - \"company\": Company name\n" +
            "  - \"specializationskills\": Primary technical skill used (choose using this logic):\n" +
            "     → If job mentions 'Spring Boot', set to 'Spring Boot'\n" +
            "     → Else if job mentions 'Java' (and not Spring Boot), set to 'Java'\n" +
            "     → Else if job mentions 'Flutter', set to 'Flutter'\n" +
            "     → Else if job mentions React, Angular, or Vue, use most relevant JS framework\n" +
            "     → Else if job involves Pandas, NumPy, Machine Learning, etc., set to 'Python'\n" +
            "     → Else use best guess from tech stack mentioned\n" +
            "  - \"location\": City, Country (if available), or null\n" +
            "  - \"start_date\": Format 'Month Year'\n" +
            "  - \"end_date\": Format 'Month Year' or 'Present'\n" +
            "  - \"duration_years\": Float (rounded to 1 decimal). Use 'June 2025' if end date is Present\n" +
            "  - \"description\": 2–3 lines of work responsibilities\n\n" +

            "*Experience Rules:*\n" +
            "- Do not include internships, apprenticeships, or trainings\n" +
            "- Round each duration_years to 1 decimal place\n" +
            "- Sum all durations into \"experience_years\" and round to nearest 0.5 only\n" +
            "  → e.g., 0.4 → 0.5, 1.3 → 1.5, 2.6 → 2.5, 3.75 → 4.0\n\n" +

            "4. Education:\n" +
            "- For each degree:\n" +
            "  - \"degree\": e.g., B.Tech, MSc\n" +
            "  - \"institute\": University or college name\n" +
            "  - \"start_year\": 4-digit year\n" +
            "  - \"end_year\": 4-digit year or 'Present'\n\n" +

            "Strictly return only JSON in the following format:\n" +
            "{\n" +
            "  \"name\": \"John Doe\",\n" +
            "  \"email\": \"john.doe@email.com\",\n" +
            "  \"phone number\": \"+91-1234567890\",\n" +
            "  \"linkedin\": \"https://linkedin.com/in/johndoe\",\n" +
            "  \"github\": \"https://github.com/johndoe\",\n" +
            "  \"profile_summary\": \"Backend developer with 5 years of experience...\",\n" +
            "  \"job_fit\": \"Ideal for Spring Boot backend developer roles.\",\n" +
            "  \"overall_score\": 92,\n" +
            "  \"job_position\": \"Senior Backend Developer\",\n" +
            "  \"experience_years\": 5.0,\n" +
            "  \"skills\": [\"Java\", \"Spring Boot\", \"SQL\", \"Docker\"],\n" +
            "  \"experience\": [\n" +
            "    {\n" +
            "      \"title\": \"Software Engineer\",\n" +
            "      \"company\": \"XYZ Ltd\",\n" +
            "      \"specializationskills\": \"Spring Boot\",\n" +
            "      \"location\": \"Bangalore, India\",\n" +
            "      \"start_date\": \"Jan 2022\",\n" +
            "      \"end_date\": \"June 2025\",\n" +
            "      \"duration_years\": 3.5,\n" +
            "      \"description\": \"Developed REST APIs and deployed scalable microservices.\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"title\": \"Junior Developer\",\n" +
            "      \"company\": \"ABC Solutions\",\n" +
            "      \"specializationskills\": \"Java\",\n" +
            "      \"location\": null,\n" +
            "      \"start_date\": \"Jan 2020\",\n" +
            "      \"end_date\": \"Dec 2021\",\n" +
            "      \"duration_years\": 2.0,\n" +
            "      \"description\": \"Maintained Java backend systems and wrote SQL queries.\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"education\": [\n" +
            "    {\n" +
            "      \"degree\": \"B.Tech in Information Technology\",\n" +
            "      \"institute\": \"SRM Institute of Science and Technology\",\n" +
            "      \"start_year\": 2016,\n" +
            "      \"end_year\": 2020\n" +
            "    }\n" +
            "  ]\n" +
            "}\n\n" +
            "--- Resume Text ---\n";
}
}