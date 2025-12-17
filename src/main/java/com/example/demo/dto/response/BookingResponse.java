package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingResponse {
    private Long bookingId;
    private Long timeSlotId;
    private String candidateName;
    private String candidateEmail;
    private LocalDateTime bookingDateTime;
    private LocalDateTime updatedAt;
    private LocalDateTime slotDateTime;
}

