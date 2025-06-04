package com.example.ResumeParser.Controller;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class GeminiModelController {

    private static final Logger log = LoggerFactory.getLogger(GeminiModelController.class);
    @Value("${spring.ai.openai.api-key}")
    private String GEMINI_API_KEY;
    private final RestClient restClient;

    public GeminiModelController(RestClient.Builder builder) {
        log.info("GeminiModelController...");
        this.restClient = builder
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    /*
        curl https://generativelanguage.googleapis.com/v1beta/openai/models \                                                                                                                                                                                                  ✔  10s   base 
        -H "Authorization: Bearer GEMINI_API_KEY"
     */
    // @GetMapping("/models")
    // public List<GeminiModel> models() {
    //     ResponseEntity<ModelListResponse> response = restClient.get()
    //             .uri("/v1beta/openai/models")
    //             .header("Authorization","Bearer " + GEMINI_API_KEY)
    //             .retrieve()
    //             .toEntity(ModelListResponse.class);
    //     return response.getBody().data();
    // }

@PostMapping("/flash/pdf")
public ResponseEntity<?> promptWithPdf(@RequestParam("file") MultipartFile file) {
    String extractedText = extractTextFromPdf(file);

    String prompt = "From the following resume text, extract the following:\n" + //
                "1. Basic Information:\n" + //
                "- \"name\": Full name of the person.\n" + //
                "- \"email\": email of the person.\n" + //
                "- \"phone number \": phone number of the person.\n" + //
                "- \"experience_years\": Total professional work experience in years, calculated only from full-time job roles. - Do NOT include internships, academic projects, education, or training programs. Round to the nearest half year (e.g., 3.5, 5, 6.5). If a role says \"Present\", use June 2025 as the current date.\n" + //
                "- \"skills\": List of technical skills (e.g., programming languages, frameworks, tools). Include only specific technical skills, not soft skills.\n" + //
                "2. Detailed Work Experience:\n" + //
                "For each full-time professional experience (excluding internships), extract:\n" + //
                "- \"title\": Job title\n" + //
                "- \"company\": Company name\n" + //
                "- \"location\": City and country if mentioned\n" + //
                "- \"start_date\": In \"Month Year\" format\n" + //
                "- \"end_date\": In \"Month Year\" format or \"Present\"\n" + //
                "- \"duration_years\": Duration in years as a float, calculated using June 2025 as the current date\n" + //
                "Only include real, full-time professional roles. Do not include internships, education, certifications, or training.\n" + //
                "Return a single clean JSON in the format below, and nothing else:\n" + //
                "```json\n" + //
                "{\n" + //
                "  \"name\": \"Jane Smith\",\n" + //
                "  \"experience_years\": 4.5,\n" + //
                "  \"skills\": [\"Java\", \"Spring Boot\", \"Docker\"],\n" + //
                "  \"experience\": [\n" + //
                "    {\n" + //
                "      \"title\": \"Software Engineer\",\n" + //
                "      \"company\": \"TechNova\",\n" + //
                "      \"location\": \"Bangalore, IN\",\n" + //
                "      \"start_date\": \"Jan 2020\",\n" + //
                "      \"end_date\": \"May 2022\",\n" + //
                "      \"duration_years\": 2.5\n" + //
                "    },\n" + //
                "    {\n" + //
                "      \"title\": \"Senior Developer\",\n" + //
                "      \"company\": \"InnoSoft\",\n" + //
                "      \"location\": \"Remote\",\n" + //
                "      \"start_date\": \"Jun 2022\",\n" + //
                "      \"end_date\": \"Present\",\n" + //
                "      \"duration_years\": 3.0\n" + //
                "    }\n" + //
                "  ]\n" + //
                "}\n" + //
                "";
    
    String combinedPrompt = prompt + "\n\n--- PDF Content ---\n" + extractedText;

    Map<String, Object> body = Map.of(
            "contents", List.of(
                    Map.of("parts", List.of(
                            Map.of("text", combinedPrompt)
                    ))
            )
    );

    ResponseEntity<String> response = restClient.post()
            .uri("/v1/models/gemini-2.0-flash:generateContent?key=" + GEMINI_API_KEY)
            .header(HttpHeaders.CONTENT_TYPE, "application/json")
            .body(body)
            .retrieve()
            .toEntity(String.class);

    try {
        // Step 1: Extract the raw text from Gemini response
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        String jsonText = root.path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();

        // Step 2: Clean up markdown ```json block if present
        jsonText = jsonText.replaceAll("(?s)```json\\s*", "").replaceAll("```", "").trim();

        // Step 3: Check if it's valid JSON
        if (!isValidJson(jsonText)) {
            // Not valid JSON, return the raw response with 502 Bad Gateway
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Gemini returned non-JSON output", "raw", jsonText));
        }

        // Step 4: Return parsed JSON as response
        Map<String, Object> cleanJson = mapper.readValue(jsonText, Map.class);
        // returning json here
        return ResponseEntity.ok(cleanJson);

    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to process Gemini response", "details", e.getMessage()));
    }
}

// Helper method to check JSON validity
private boolean isValidJson(String json) {
    try {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.readTree(json);
        return true;
    } catch (IOException e) {
        return false;
    }
}








    private String extractTextFromPdf(MultipartFile file) {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract PDF content", e);
        }
    }
    










}
