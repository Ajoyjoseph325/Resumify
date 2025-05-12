
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
                "Angular", "AWS", "Azure", "Bootstrap", "c", "c++", "CSS",
                "Django", "Docker", "Express", "Flask", "GCP", "Git", "HTML",
                "Java", "JavaScript", "Jenkins", "Kafka", "Kubernetes", "MongoDB",
                "MySQL", "Node.js", "php", "PostgreSQL", "Python", "RabbitMQ",
                "React", "Redis", "SASS", "Spring", "Spring Boot", "SQL",
                "TypeScript", "Vue"
            );

            for (String skillName : skills) {
                if (!knownSkillRepository.existsByNameIgnoreCase(skillName)) {
                    knownSkillRepository.save(new Knownskill(skillName));


                }
            }
        };
    }




    
}
