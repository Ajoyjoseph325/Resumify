package com.example.ResumeParser.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ResumeParser.entity.ResumeDetailsExtracted;

@Repository
public interface ResumeDetailsExtractedRepository extends JpaRepository<ResumeDetailsExtracted, Long> {

}
