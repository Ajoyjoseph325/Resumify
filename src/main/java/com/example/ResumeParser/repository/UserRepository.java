package com.example.ResumeParser.repository;

import com.example.ResumeParser.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // findById is inherited, no need to redeclare
    
    Optional<User> findByEmail(String email); // keep this for login stuff
   
}
