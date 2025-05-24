// package com.example.ResumeParser.entity;

// import java.util.HashSet;
// import java.util.Set;

// import com.fasterxml.jackson.annotation.JsonManagedReference;

// import jakarta.persistence.CascadeType;
// import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.JoinColumn;
// import jakarta.persistence.JoinTable;
// import jakarta.persistence.ManyToMany;
// import jakarta.persistence.ManyToOne;
// import jakarta.persistence.Table;

// @Entity
// @Table(name = "resumes")
// public class Resume {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;
//     private String filePath;
//     private String fileName;
//     private String name;
//     private String email;
//     private String phoneNumber;
//     private double yearsOfExperience;

//     @ManyToOne
//     @JoinColumn(name = "user_id", nullable = false)
//     private User user;

//     @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
//     @JoinTable(
//         name = "resume_skills",
//         joinColumns = @JoinColumn(name = "resume_id"),
//         inverseJoinColumns = @JoinColumn(name = "skill_id")
//     )
//     @JsonManagedReference
//     private Set<Skill> skills = new HashSet<>();

//     public Resume() {}

//     // Getters and Setters

//     public Long getId() {
//         return id;
//     }

//     public void setId(Long id) {
//         this.id = id;
//     }

//     public String getName() {
//         return name;
//     }

//     public void setName(String name) {
//         this.name = name;
//     }

//     public String getEmail() {
//         return email;
//     }

//     public void setEmail(String email) {
//         this.email = email;
//     }

//     public String getPhoneNumber() {
//         return phoneNumber;
//     }

//     public void setPhoneNumber(String phoneNumber) {
//         this.phoneNumber = phoneNumber;
//     }

//     public double getYearsOfExperience() {
//         return yearsOfExperience;
//     }

//     public void setYearsOfExperience(double yearsOfExperience) {
//         this.yearsOfExperience = yearsOfExperience;
//     }

//     public User getUser() {
//         return user;
//     }

//     public void setUser(User user) {
//         this.user = user;
//     }

//     public Set<Skill> getSkills() {
//         return skills;
//     }

//     public void setSkills(Set<Skill> skills) {
//         this.skills = skills;
//     }

//     // Methods to manage bidirectional relationship
//     public void addSkill(Skill skill) {
//         this.skills.add(skill);
//         skill.getResumes().add(this);
//     }

//     public void removeSkill(Skill skill) {
//         this.skills.remove(skill);
//         skill.getResumes().remove(this);
//     }
//     public String getFilePath() {
//         return filePath;
//     }
//     public void setFilePath(String filePath) {
//         this.filePath = filePath;
//     }
//     public String getFileName() {
//         if (this.filePath != null) {
//             return new java.io.File(this.filePath).getName();
//         }
//         return fileName;
//     }
// }






package com.example.ResumeParser.entity;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "resumes")
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filePath;

    private String fileName;

    private String name;

    private String email;

    private String phoneNumber;

    private double yearsOfExperience;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
        name = "resume_skills",
        joinColumns = @JoinColumn(name = "resume_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    @JsonManagedReference
    private Set<Skill> skills = new HashSet<>();

    // ✅ Field to store resume first page image as binary
    @Lob
    @Column(name = "resume_image", columnDefinition = "LONGBLOB")
    private byte[] resumeImage;

    public Resume() {}

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        if (this.filePath != null) {
            return new java.io.File(this.filePath).getName();
        }
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public double getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(double yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<Skill> getSkills() {
        return skills;
    }

    public void setSkills(Set<Skill> skills) {
        this.skills = skills;
    }

    public byte[] getResumeImage() {
        return resumeImage;
    }

    public void setResumeImage(byte[] resumeImage) {
        this.resumeImage = resumeImage;
    }

    // Utility methods for bidirectional relationship
    public void addSkill(Skill skill) {
        this.skills.add(skill);
        skill.getResumes().add(this);
    }

    public void removeSkill(Skill skill) {
        this.skills.remove(skill);
        skill.getResumes().remove(this);
    }
}
