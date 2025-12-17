package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.PagedTimeSlotResponse;
import com.example.demo.service.TimeSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/time-slots")
@RequiredArgsConstructor
public class TimeSlotController {
    
    private final TimeSlotService timeSlotService;
    
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<PagedTimeSlotResponse>> getAvailableSlots(
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer pageSize) {
        
        PagedTimeSlotResponse response = timeSlotService.getAvailableSlots(cursor, pageSize);
        
        ApiResponse<PagedTimeSlotResponse> apiResponse = ApiResponse.<PagedTimeSlotResponse>builder()
            .success(true)
            .message("Available time slots retrieved successfully")
            .data(response)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(apiResponse);
    }
}

