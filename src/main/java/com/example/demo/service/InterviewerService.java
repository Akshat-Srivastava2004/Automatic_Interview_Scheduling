package com.example.demo.service;

import com.example.demo.domain.entity.AvailabilitySlot;
import com.example.demo.domain.entity.Interviewer;
import com.example.demo.domain.repository.AvailabilitySlotRepository;
import com.example.demo.domain.repository.InterviewerRepository;
import com.example.demo.dto.request.AvailabilitySlotRequest;
import com.example.demo.dto.request.InterviewerAvailabilityRequest;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewerService {
    
    private final InterviewerRepository interviewerRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final TimeSlotGenerationService timeSlotGenerationService;
    
    @Transactional
    public Interviewer createOrUpdateInterviewerAvailability(InterviewerAvailabilityRequest request) {
        log.info("Creating/updating interviewer availability for email: {}", request.getEmail());
        
        Interviewer interviewer = interviewerRepository.findByEmail(request.getEmail())
            .orElse(Interviewer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .maxInterviewsPerWeek(request.getMaxInterviewsPerWeek())
                .build());
        
        // Update fields
        interviewer.setName(request.getName());
        interviewer.setMaxInterviewsPerWeek(request.getMaxInterviewsPerWeek());
        
        // Clear existing availability slots
        interviewer.getAvailabilitySlots().clear();
        
        // Add new availability slots
        List<AvailabilitySlot> availabilitySlots = request.getAvailabilitySlots().stream()
            .map(slotRequest -> AvailabilitySlot.builder()
                .interviewer(interviewer)
                .dayOfWeek(slotRequest.getDayOfWeek())
                .startTime(slotRequest.getStartTime())
                .endTime(slotRequest.getEndTime())
                .slotDurationMinutes(slotRequest.getSlotDurationMinutes())
                .build())
            .collect(Collectors.toList());
        
        interviewer.getAvailabilitySlots().addAll(availabilitySlots);
        
        Interviewer savedInterviewer = interviewerRepository.save(interviewer);
        
        // Generate time slots for next 2 weeks
        timeSlotGenerationService.generateTimeSlotsForNextWeeks(savedInterviewer);
        
        log.info("Interviewer availability created/updated successfully. ID: {}", savedInterviewer.getId());
        return savedInterviewer;
    }
    
    public Interviewer getInterviewerById(Long id) {
        return interviewerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Interviewer not found with id: " + id));
    }
    
    public Interviewer getInterviewerByEmail(String email) {
        return interviewerRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Interviewer not found with email: " + email));
    }
}

