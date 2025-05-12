package com.example.ResumeParser.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ResumeParser.entity.Knownskill;

public interface Knownskillrepository extends JpaRepository<Knownskill, Long>{
    Optional<Knownskill> findByName(String name);
    List<Knownskill> findAll();
    boolean existsByNameIgnoreCase(String name);
    

    
}
