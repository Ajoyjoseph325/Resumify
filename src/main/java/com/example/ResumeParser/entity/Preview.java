package com.example.ResumeParser.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Preview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String phone;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String profileSummary;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;
    private String linkedin;
    private String github;
    private Double overallScore;
    @Column(columnDefinition = "TEXT")
    private String jobFit;
    private String specializedJob;
    private String jobPosition;

    @OneToOne(fetch = FetchType.LAZY)
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ElementCollection
    @CollectionTable(name = "preview_skills", joinColumns = @JoinColumn(name = "preview_id"))
    private List<String> skills;

    @ElementCollection
    @CollectionTable(name = "preview_education", joinColumns = @JoinColumn(name = "preview_id"))
    private List<EducationEntry> education;

    @ElementCollection
    @CollectionTable(name = "preview_experience", joinColumns = @JoinColumn(name = "preview_id"))
    private List<ExperienceEntry> experience;

    @Transient
    private String imageUrl; // 👈 Not persisted, only used in API response
}
