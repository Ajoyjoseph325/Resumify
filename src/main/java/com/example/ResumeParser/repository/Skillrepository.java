package com.example.ResumeParser.repository;

import com.example.ResumeParser.entity.Skill;

import com.example.ResumeParser.entity.Resume;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Skillrepository extends JpaRepository<Skill, Long> {

    // Find a skill by name (case-insensitive logic done in service)
 
    Optional<Skill> findByName(String name);
}
