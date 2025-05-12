package com.example.ResumeParser.entity;

import jakarta.persistence.*;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonBackReference;


@Entity
@Table(name = "skills")
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @ManyToMany(mappedBy = "skills")
    @JsonBackReference
    private Set<Resume> resumes = new HashSet<>();

    public Skill() {}

    public Skill(String name) {
        this.name = name.toLowerCase();
    }

    public Set<Resume> getResumes() {
        return resumes;
    }

    public void setResumes(Set<Resume> resumes) {
        this.resumes = resumes;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name.toLowerCase();
    }
    

  
}


