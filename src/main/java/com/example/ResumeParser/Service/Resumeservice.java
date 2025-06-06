package com.example.ResumeParser.Service;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ResumeParser.entity.Resume;
import com.example.ResumeParser.repository.Resumerepository;

@Service
public class ResumeService {

    private final Resumerepository resumeRepository;

    public ResumeService(Resumerepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

    @Transactional  // 🔥 must-have to avoid the EntityManager error!
    public void deleteResumesByUserId(Long userId) {
        resumeRepository.deleteByUser_Id(userId);
    }

    public List<Resume> getResumesByUserId(Long userId) {
        return resumeRepository.findByUser_Id(userId);
    }

     @Transactional
    public void deleteResumeByIdAndUserId(Long resumeId, Long userId) {
        // Check if resume belongs to user
        boolean exists = resumeRepository.findByIdAndUserId(resumeId, userId).isPresent();

        if (!exists) {
            throw new IllegalArgumentException("Resume with ID " + resumeId + " does not belong to user with ID " + userId);
        }
        
        resumeRepository.deleteByIdAndUserId(resumeId, userId);
    }
}
