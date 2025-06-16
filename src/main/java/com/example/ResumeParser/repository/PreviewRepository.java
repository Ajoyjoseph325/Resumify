package com.example.ResumeParser.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ResumeParser.entity.Preview;

public interface PreviewRepository extends JpaRepository<Preview, Long> {
    List<Preview> findAllByUserId(Long userId);
}