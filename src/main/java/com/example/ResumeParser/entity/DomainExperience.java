package com.example.ResumeParser.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "domain_experience")
public class DomainExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "skill", columnDefinition = "TEXT")
    private String skill;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "years")
    private Double years;

    @ManyToOne
    @JoinColumn(name = "resume_id")
    private Resume resume;
}
