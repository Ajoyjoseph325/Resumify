
package com.example.ResumeParser.config;

import com.example.ResumeParser.entity.Knownskill;
import com.example.ResumeParser.repository.Knownskillrepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class Dataseeder {

    @Bean
    CommandLineRunner seedKnownSkills(Knownskillrepository knownSkillRepository) {
        return args -> {
            List<String> skills = List.of(

                    // Programming Languages (basic & advanced)
                    "Java", "Python", "Core Python", "Advanced Python", "C", "C++", "C#", "JavaScript", "TypeScript",
                    "HTML", "CSS", "SCSS", "SQL", "PL/SQL", "Bash", "Shell Scripting", "Ruby", "PHP", "Dart",
                    "Kotlin", "Swift", "Go", "Scala", "Perl", "Rust", "Haskell", "Lua", "Objective-C", "Visual Basic",
                    "Assembly", "JSON", "XML",

                    // Cybersecurity Skills
                    "Cybersecurity", "Network Security", "Penetration Testing", "Ethical Hacking",
                    "Vulnerability Assessment",
                    "OWASP", "SIEM", "Firewalls", "IDS", "Encryption", "Hashing", "SSL/TLS", "Authentication",
                    "Authorization", "IAM", "MFA", "Zero Trust Architecture", "Security Compliance", "Risk Assessment",
                    "Threat Modeling", "Secure Coding", "Kali Linux", "Metasploit", "Nmap", "Wireshark", "Burp Suite",

                    // Soft Skills
                    "Problem Solving", "Communication Skills", "Teamwork", "Time Management", "Critical Thinking",
                    "Leadership",
                    "Adaptability", "Self-Motivation", "Goal-Oriented", "Conflict Resolution", "Collaboration",
                    "Decision Making",
                    "Creativity", "Emotional Intelligence", "Attention to Detail", "Stress Management",

                    // Development Methodologies
                    "Agile", "Scrum", "Kanban", "DevOps", "Waterfall", "SDLC", "TDD", "BDD", "Continuous Integration",
                    "Continuous Deployment", "CI/CD", "Code Review", "Version Control", "Git", "GitHub", "GitLab",

                    // Tools & Technologies
                    "Spring", "Spring Boot", "Hibernate", "JPA", "Servlets", "JSP", "Struts", "Maven", "Gradle",
                    "JUnit",
                    "TestNG", "Mockito", "REST API", "SOAP", "Microservices", "Docker", "Kubernetes", "Jenkins",
                    "IntelliJ IDEA", "Eclipse", "NetBeans", "Visual Studio Code", "PyCharm", "Jupyter Notebook",
                    "Pandas",
                    "NumPy", "Matplotlib", "Scikit-learn", "TensorFlow", "Keras", "PyTorch", "OpenCV", "Flask",
                    "Django",
                    "FastAPI", "Angular", "React", "Vue.js", "Node.js", "Express.js", "Bootstrap", "Tailwind CSS",
                    "Firebase",
                    "AWS", "Azure", "Google Cloud Platform", "MySQL", "PostgreSQL", "MongoDB", "Oracle", "Redis",
                    "SQLite",
                    "Apache Tika", "Apache PDFBox", "Tesseract OCR", "Stanford CoreNLP", "OpenNLP",
                    // design
                    "Figma", "Adobe Photoshop", "Adobe Illustrator", "Adobe XD", "Sketch",
                    "Canva", "InVision", "CorelDRAW", "Affinity Designer", "Marvel",
                    "Balsamiq", "Axure RP", "Framer", "Proto.io", "Zeplin",
                    // tools commonly used worldwid

                    "Microsoft Word","Microsoft Excel", "Microsoft PowerPoint", "Google Docs","Google Sheets",
                    "Google Slides","LibreOffice Writer","LibreOffice Calc","LibreOffice Impress","Notepad","Notepad++",
                    "Adobe Acrobat Reader", "Evernote", "OneNote", "Trello", "Slack", "Zoom", "Skype"

            );

            for (String skillName : skills) {
                if (!knownSkillRepository.existsByNameIgnoreCase(skillName)) {
                    knownSkillRepository.save(new Knownskill(skillName));

                }
            }
        };
    }

}
