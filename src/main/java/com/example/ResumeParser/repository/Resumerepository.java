package com.example.ResumeParser.repository;

import com.example.ResumeParser.entity.Resume;
import com.example.ResumeParser.entity.User;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Resumerepository extends JpaRepository<Resume, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM Resume r WHERE r.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    List<Resume> findByUser(User user);

    @Query(value = """
        SELECT r.id,r.name, r.email, r.phone, r.total_experience_years,
               GROUP_CONCAT(s.skill_name ORDER BY s.skill_name SEPARATOR ', ') AS skills
        FROM resume r
        LEFT JOIN skill s ON r.id = s.resume_id
        WHERE r.user_id = :userId
        GROUP BY r.id, r.name, r.email, r.phone, r.total_experience_years
        ORDER BY r.name
        """, nativeQuery = true)
    List<Object[]> findResumesByUserIdNative(@Param("userId") Long userId);

   @Query(value = """
    SELECT r.id, r.name, r.email, r.phone, r.total_experience_years,
           GROUP_CONCAT(DISTINCT s.skill_name ORDER BY s.skill_name SEPARATOR ', ') AS skills
    FROM resume r
    JOIN domain_experience e ON r.id = e.resume_id
    LEFT JOIN skill s ON s.resume_id = r.id
    WHERE e.skill IN (:skills)
      AND e.years BETWEEN :start AND :end
      AND r.user_id = :userId
    GROUP BY r.id, r.name, r.email, r.phone, r.total_experience_years
    ORDER BY r.total_experience_years
    """, nativeQuery = true)
List<Object[]> findResumesBySkillsAndExperienceRangeAndUserId(
    @Param("skills") List<String> skills,
    @Param("start") double start,
    @Param("end") double end,
    @Param("userId") Long userId
);


@Query(value = """
    SELECT r.id, r.name, r.email, r.phone, r.total_experience_years,
           GROUP_CONCAT(DISTINCT s_all.skill_name ORDER BY s_all.skill_name SEPARATOR ', ') AS skills
    FROM resume r
    JOIN (
        SELECT DISTINCT resume_id
        FROM skill
        WHERE skill_name IN (:skills)
    ) s_filtered ON s_filtered.resume_id = r.id
    LEFT JOIN skill s_all ON s_all.resume_id = r.id
    WHERE r.user_id = :userId
    GROUP BY r.id, r.name, r.email, r.phone, r.total_experience_years
    ORDER BY r.total_experience_years
""", nativeQuery = true)
List<Object[]> findResumesBySkillsUserId(
    @Param("skills") List<String> skills,
    @Param("userId") Long userId
);

void deleteByUser_Id(Long userId); 

List<Resume> findByUser_Id(Long userId);

 Optional<Resume> findByIdAndUserId(Long resumeId, Long userId);
    
    // Delete resume by id and user id
void deleteByIdAndUserId(Long resumeId, Long userId);
}