package com.example.demo.dto.response;

import com.example.demo.domain.entity.TimeSlot;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TimeSlotResponse {
    private Long id;
    private Long interviewerId;
    private LocalDateTime slotDateTime;
    private TimeSlot.TimeSlotStatus status;
}

