// package com.example.ResumeParser.repository;

// import java.util.List;
// import com.example.ResumeParser.entity.Resume;
// import com.example.ResumeParser.entity.User; // Import User entity

// import jakarta.transaction.Transactional;

// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Modifying;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
// import org.springframework.stereotype.Repository;

// @Repository
// public interface Resumerepository extends JpaRepository<Resume, Long> {

//     @Modifying
//     @Transactional
//     @Query("DELETE FROM Resume r WHERE r.user.id = :userId")
//     void deleteAllByUserId(@Param("userId") Long userId);


//     // Optional: Find resumes that have a specific skill
//     @Query("SELECT r FROM Resume r JOIN r.skills s WHERE LOWER(s.name) = LOWER(:skillName)")
//     List<Resume> findBySkillName(@Param("skillName") String skillName);

//     // Optional: Sort resumes by years of experience (descending)
//     List<Resume> findAllByOrderByYearsOfExperienceDesc();

//     @Query(value =
//         "SELECT r.id, r.name, r.phone_number, r.email, r.years_of_experience, " +
//         "GROUP_CONCAT(DISTINCT s.name) AS skills " +
//         "FROM resumes r " +
//         "JOIN resume_skills rs ON r.id = rs.resume_id " +
//         "JOIN skills s ON rs.skill_id = s.id " +
//         "WHERE s.name IN (:skills) " +
//         "AND r.years_of_experience >= :minExp " +
//         "AND r.user_id = :userId " +
//         "GROUP BY r.id " +
//         "HAVING COUNT(DISTINCT s.name) = :skillCount",
//         nativeQuery = true)
//     List<Object[]> filterBySkillsAndExperience(
//         @Param("skills") List<String> skills,
//         @Param("minExp") double minExp,
//         @Param("skillCount") int skillCount,
//         @Param("userId") Long userId
//     );

//     // Add a method to find resumes by user (without changing existing methods)
//     List<Resume> findByUser(User user);
// }









package com.example.ResumeParser.repository;

import java.util.List;
import com.example.ResumeParser.entity.Resume;
import com.example.ResumeParser.entity.User;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface Resumerepository extends JpaRepository<Resume, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM Resume r WHERE r.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    @Query("SELECT r FROM Resume r JOIN r.skills s WHERE LOWER(s.name) = LOWER(:skillName)")
    List<Resume> findBySkillName(@Param("skillName") String skillName);

    List<Resume> findAllByOrderByYearsOfExperienceDesc();

    @Query(value =
        "SELECT r.id, r.name, r.phone_number, r.email, r.years_of_experience, " +
        "GROUP_CONCAT(DISTINCT s.name) AS skills " +
        "FROM resumes r " +
        "JOIN resume_skills rs ON r.id = rs.resume_id " +
        "JOIN skills s ON rs.skill_id = s.id " +
        "WHERE s.name IN (:skills) " +
        "AND r.years_of_experience >= :minExp " +
        "AND r.user_id = :userId " +
        "GROUP BY r.id " +
        "HAVING COUNT(DISTINCT s.name) = :skillCount",
        nativeQuery = true)
    List<Object[]> filterBySkillsAndExperience(
        @Param("skills") List<String> skills,
        @Param("minExp") double minExp,
        @Param("skillCount") int skillCount,
        @Param("userId") Long userId
    );

    // Find resumes by user entity
    List<Resume> findByUser(User user);

    // Added: Find resumes by user id
    List<Resume> findByUser_Id(Long userId);
}
