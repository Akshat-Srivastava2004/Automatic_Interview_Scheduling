package com.example.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookSlotRequest {
    
    @NotNull(message = "Time slot ID is required")
    private Long timeSlotId;
    
    @NotBlank(message = "Candidate name is required")
    private String candidateName;
    
    @NotBlank(message = "Candidate email is required")
    @Email(message = "Email should be valid")
    private String candidateEmail;
}

