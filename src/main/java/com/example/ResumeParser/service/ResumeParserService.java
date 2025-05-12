package com.example.ResumeParser.service;

import com.example.ResumeParser.model.ResumeData;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.regex.*;

@Service // Marks this class as a Spring Service (logic component)
public class ResumeParserService {

    public ResumeData parsePdf(String pdfPath) throws IOException {
        // Load and read the PDF file
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(pdfPath));
        StringBuilder content = new StringBuilder();

        // Extract text from each page
        for (int i = 1; i <= pdfDoc.getNumberOfPages(); ++i) {
            String text = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i));
            content.append(text).append("\n");
        }

        pdfDoc.close(); // Close the document to free resources

        String text = content.toString(); // All PDF text as one big string

        // Create a ResumeData object and fill in fields
        ResumeData resume = new ResumeData();
        resume.setName(extractName(text));
        resume.setEmail(extractEmail(text));
        resume.setPhone(extractPhoneNumber(text));
        resume.setSkills(extractSkills(text));
        resume.setExperience(extractExperience(text)); // ✅ Added

        return resume;
    }

    // Extract email using regex pattern
    private String extractEmail(String text) {
        Matcher matcher = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-z]{2,}").matcher(text);
        return matcher.find() ? matcher.group() : "Not found";
    }

    // Extract phone numbers (handles various formats)
    private String extractPhoneNumber(String text) {
        Matcher matcher = Pattern.compile("(\\+\\d{1,2}\\s)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}").matcher(text);
        return matcher.find() ? matcher.group() : "Not found";
    }

    // Extract the first non-empty line as the name (basic assumption)
    private String extractName(String text) {
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.trim().length() > 1 && !line.contains("@")) {
                return line.trim();
            }
        }
        return "Not found";
    }

    // Search for known skill keywords in the resume text
    private String extractSkills(String text) {
        String[] knownSkills = { "Java", "Python", "Spring", "Angular", "SQL", "JavaScript", "HTML", "CSS", "React", "Django" };
        StringBuilder foundSkills = new StringBuilder();

        for (String skill : knownSkills) {
            if (text.toLowerCase().contains(skill.toLowerCase())) {
                foundSkills.append(skill).append(", ");
            }
        }

        return foundSkills.length() > 0 ? foundSkills.substring(0, foundSkills.length() - 2) : "Not found";
    }

    // ✅ Extract years of experience using regex
    private String extractExperience(String text) {
        // Example patterns: "3 years", "5+ years", "2.5 years"
        Matcher matcher = Pattern.compile("(\\d+\\.?\\d*)\\s*(\\+)?\\s*years?").matcher(text.toLowerCase());
        if (matcher.find()) {
            return matcher.group(0); // return the matched phrase like "3 years"
        }
        return "Not found";
    }
}
