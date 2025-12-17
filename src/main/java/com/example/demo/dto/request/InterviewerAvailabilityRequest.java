package com.example.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class InterviewerAvailabilityRequest {
    
    @NotBlank(message = "Interviewer name is required")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotNull(message = "Max interviews per week is required")
    @Min(value = 1, message = "Max interviews per week must be at least 1")
    private Integer maxInterviewsPerWeek;
    
    @NotNull(message = "Availability slots are required")
    private List<AvailabilitySlotRequest> availabilitySlots;
}

