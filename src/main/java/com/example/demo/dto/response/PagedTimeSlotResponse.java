package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PagedTimeSlotResponse {
    private List<TimeSlotResponse> timeSlots;
    private String nextCursor; // Encoded cursor for next page
    private Boolean hasNextPage;
    private Integer pageSize;
}

