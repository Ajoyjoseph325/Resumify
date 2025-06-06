package com.example.ResumeParser.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ResumeParser.entity.DomainExperience;

@Repository
public interface DomainExperienceRepository extends JpaRepository<DomainExperience, Long> {
}

