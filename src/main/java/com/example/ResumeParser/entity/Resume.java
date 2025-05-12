package com.example.ResumeParser.entity;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "resumes")
public class Resume{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String phoneNumber;
    private double yearsOfExperience;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "resume_skills",
        joinColumns = @JoinColumn(name = "resume_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    @JsonManagedReference
    private Set<Skill> skills = new HashSet<>();

    public Resume() {}

    // Getters and setters

    public void addSkill(Skill skill) {
        this.skills.add(skill);
        skill.getResumes().add(this);
    }

    public void removeSkill(Skill skill) {
        this.skills.remove(skill);
        skill.getResumes().remove(this);
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


    public Set<Skill> getSkills() {
        return skills;
    }
    
    public void setSkills(Set<Skill> skills) {
        this.skills = skills;
    }
    
    

    
}
