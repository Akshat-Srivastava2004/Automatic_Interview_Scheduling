package com.example.demo.controller;

import com.example.demo.domain.entity.Interviewer;
import com.example.demo.dto.request.InterviewerAvailabilityRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.service.InterviewerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/interviewers")
@RequiredArgsConstructor
public class InterviewerController {
    
    private final InterviewerService interviewerService;
    
    @PostMapping("/availability")
    public ResponseEntity<ApiResponse<Interviewer>> createOrUpdateAvailability(
            @Valid @RequestBody InterviewerAvailabilityRequest request) {
        Interviewer interviewer = interviewerService.createOrUpdateInterviewerAvailability(request);
        
        ApiResponse<Interviewer> response = ApiResponse.<Interviewer>builder()
            .success(true)
            .message("Interviewer availability created/updated successfully")
            .data(interviewer)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Interviewer>> getInterviewer(@PathVariable Long id) {
        Interviewer interviewer = interviewerService.getInterviewerById(id);
        
        ApiResponse<Interviewer> response = ApiResponse.<Interviewer>builder()
            .success(true)
            .message("Interviewer retrieved successfully")
            .data(interviewer)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<Interviewer>> getInterviewerByEmail(@PathVariable String email) {
        Interviewer interviewer = interviewerService.getInterviewerByEmail(email);
        
        ApiResponse<Interviewer> response = ApiResponse.<Interviewer>builder()
            .success(true)
            .message("Interviewer retrieved successfully")
            .data(interviewer)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
}

