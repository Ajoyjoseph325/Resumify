// package com.example.ResumeParser.Controller;

// import org.springframework.web.multipart.MultipartFile;
// import com.example.ResumeParser.Service.Resumeservice;
// import com.example.ResumeParser.entity.Resume;
// import com.example.ResumeParser.entity.User;
// import com.example.ResumeParser.dto.ResumeWithSkillsDTO;
// import com.example.ResumeParser.dto.Resumefilterrequest;
// import com.example.ResumeParser.dto.SkillFilterRequest;
// import com.example.ResumeParser.repository.UserRepository;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.core.io.UrlResource;
// import org.springframework.core.io.Resource;
// import org.springframework.http.ResponseEntity;
// import org.springframework.http.MediaType;
// import org.springframework.http.HttpHeaders;
// import org.springframework.web.bind.annotation.*;

// import java.io.IOException;
// import java.nio.file.Path;
// import java.nio.file.Paths;
// import java.util.List;

// @RestController
// public class Resumecontroller {

//     @Autowired
//     private Resumeservice resumeservice;

//     @Autowired
//     private UserRepository userRepository;

//     @GetMapping("/hii")
//     public String hello() {
//         return "hello";
//     }

//     @PostMapping("/resumes/upload/{userId}")
//     public ResponseEntity<String> uploadResume(@PathVariable Long userId, @RequestParam("file") MultipartFile file) {
//         System.out.println("Upload called with userId: " + userId);
        
//         // ✅ Ensure the user exists before proceeding
//         User user = userRepository.findById(userId)
//                 .orElseThrow(() -> new RuntimeException("User with ID " + userId + " not found"));

//         // ✅ Let the service handle associating resume with this user
//         try {
//             resumeservice.uploadResumeForUser(user.getId(), file);
//         } catch (Exception e) {
//             // TODO Auto-generated catch block
//             e.printStackTrace();
//         }

//         return ResponseEntity.ok("Resume uploaded and saved successfully for user with ID " + userId);
//     }
//     // old method to save resume
//     // @PostMapping("/saveresume")
//     // public ResponseEntity<String> uploadAndSaveResume(@RequestParam("file") MultipartFile file) {
//     //     resumeservice.saveresume(file);
//     //     return ResponseEntity.ok("Resume parsed and saved successfully.");
//     // }


//         //old method to get all resumes in db
//     // @GetMapping("/resumes")
//     // public ResponseEntity<List<Resume>> getAllResumes() {
//     //     List<Resume> resumes = resumeservice.getAllResumes();
//     //     return ResponseEntity.ok(resumes);
//     // }

//     // @PostMapping("/filter")
//     // public List<ResumeWithSkillsDTO> filterResumes(@RequestBody SkillFilterRequest request) {
//     //     return resumeservice.filterResumes(request.getSkills(), (int) request.getMinExp());
//     // }

//     // New endpoint to get resumes by userId
//     @GetMapping("/resumesByUserId")
//     public ResponseEntity<List<ResumeWithSkillsDTO>> getResumesByUserId(@RequestParam("userId") Long userId) {
//         List<ResumeWithSkillsDTO> resumes = resumeservice.getResumesByUserId(userId);
//         return ResponseEntity.ok(resumes);
//     }
//  @PostMapping("/filter")
//     public List<ResumeWithSkillsDTO> filterResumes(@RequestBody Resumefilterrequest request) {
//         return resumeservice.filterResumes(request.getSkills(), request.getMinYearsOfExperience(), request.getUserId());
//     }

// @PostMapping("/filter-user-resumes")
// public ResponseEntity<List<ResumeWithSkillsDTO>> filterUserResumes(@RequestBody Resumefilterrequest request) {
//     List<ResumeWithSkillsDTO> filtered = resumeservice.filterResumes(
//         request.getSkills(),
//         request.getMinYearsOfExperience(),
//         request.getUserId()

//     );
//     return ResponseEntity.ok(filtered);
// }


// @DeleteMapping("/delete/{userId}")
// public ResponseEntity<?> deleteUserResumes(@PathVariable Long userId) {
//     resumeservice.deleteResumesByUserId(userId);
//     return ResponseEntity.ok("User's resumes deleted successfully");
// }
// @GetMapping("/resumes/view/{resumeId}")
// public ResponseEntity<Resource> viewResume(@PathVariable Long resumeId) throws IOException {
//     Resume resume = resumeservice.getResumeById(resumeId);
//     if (resume == null) {
//         return ResponseEntity.notFound().build();
//     }

//     Path file = Paths.get(resume.getFilePath());
//     Resource resource = new UrlResource(file.toUri());

//     if (!resource.exists()) {
//         return ResponseEntity.notFound().build();
//     }

//     return ResponseEntity.ok()
//             .contentType(MediaType.APPLICATION_PDF)
//             .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resume.getFileName() + "\"")
//             .body(resource);
// }






// }





package com.example.ResumeParser.Controller;

import com.example.ResumeParser.Service.Resumeservice;
import com.example.ResumeParser.dto.ResumeWithSkillsDTO;
import com.example.ResumeParser.dto.Resumefilterrequest;
import com.example.ResumeParser.entity.Resume;
import com.example.ResumeParser.entity.User;
import com.example.ResumeParser.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api")
public class Resumecontroller {

    @Autowired
    private Resumeservice resumeservice;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/hii")
    public String hello() {
        return "hello";
    }

    @PostMapping("/resumes/upload/{userId}")
    public ResponseEntity<String> uploadResume(@PathVariable Long userId, @RequestParam("file") MultipartFile file) {
        System.out.println("Upload called with userId: " + userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User with ID " + userId + " not found"));

        try {
            resumeservice.uploadResumeForUser(user.getId(), file);
            return ResponseEntity.ok("Resume uploaded and saved successfully for user with ID " + userId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to upload resume: " + e.getMessage());
        }
    }

    @GetMapping("/resumesByUserId")
    public ResponseEntity<List<ResumeWithSkillsDTO>> getResumesByUserId(@RequestParam("userId") Long userId) {
        List<ResumeWithSkillsDTO> resumes = resumeservice.getResumesByUserId(userId);
        return ResponseEntity.ok(resumes);
    }

    @PostMapping("/filter")
    public List<ResumeWithSkillsDTO> filterResumes(@RequestBody Resumefilterrequest request) {
        return resumeservice.filterResumes(request.getSkills(), request.getMinYearsOfExperience(), request.getUserId());
    }

    @PostMapping("/filter-user-resumes")
    public ResponseEntity<List<ResumeWithSkillsDTO>> filterUserResumes(@RequestBody Resumefilterrequest request) {
        List<ResumeWithSkillsDTO> filtered = resumeservice.filterResumes(
                request.getSkills(),
                request.getMinYearsOfExperience(),
                request.getUserId()
        );
        return ResponseEntity.ok(filtered);
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUserResumes(@PathVariable Long userId) {
        resumeservice.deleteResumesByUserId(userId);
        return ResponseEntity.ok("User's resumes deleted successfully");
    }

    @GetMapping("/resumes/view/{resumeId}")
    public ResponseEntity<Resource> viewResume(@PathVariable Long resumeId) throws MalformedURLException {
        Resume resume = resumeservice.getResumeById(resumeId);
        if (resume == null || resume.getFilePath() == null) {
            return ResponseEntity.notFound().build();
        }

        Path filePath = Paths.get(resume.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resume.getFileName() + "\"")
                .body(resource);
    }

    @GetMapping("/resumes/image/{resumeId}")
    public ResponseEntity<Resource> getResumeImage(@PathVariable Long resumeId) {
        Resume resume = resumeservice.getResumeById(resumeId);
        if (resume == null || resume.getResumeImage() == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] imageData = resume.getResumeImage();
        ByteArrayResource resource = new ByteArrayResource(imageData);

        return ResponseEntity.ok()
                .contentLength(imageData.length)
                .contentType(MediaType.IMAGE_PNG) // Use actual image format if needed
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"resume_" + resumeId + ".png\"")
                .body(resource);
    }
}
