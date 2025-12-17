package com.example.demo.service;

import com.example.demo.domain.entity.TimeSlot;
import com.example.demo.domain.repository.TimeSlotRepository;
import com.example.demo.dto.response.PagedTimeSlotResponse;
import com.example.demo.dto.response.TimeSlotResponse;
import com.example.demo.util.CursorEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeSlotService {
    
    private final TimeSlotRepository timeSlotRepository;
    private static final int DEFAULT_PAGE_SIZE = 20;
    
    @Transactional(readOnly = true)
    public PagedTimeSlotResponse getAvailableSlots(String cursor, Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (pageSize > 100) {
            pageSize = 100; // Limit max page size
        }
        
        Pageable pageable = PageRequest.of(0, pageSize);
        Page<TimeSlot> timeSlotPage;
        
        if (cursor == null || cursor.isEmpty()) {
            // First page
            timeSlotPage = timeSlotRepository.findAvailableSlots(
                TimeSlot.TimeSlotStatus.AVAILABLE, 
                pageable
            );
        } else {
            // Decode cursor
            CursorEncoder.Cursor decodedCursor = CursorEncoder.decode(cursor);
            timeSlotPage = timeSlotRepository.findAvailableSlotsAfterCursor(
                TimeSlot.TimeSlotStatus.AVAILABLE,
                decodedCursor.getDateTime(),
                decodedCursor.getId(),
                pageable
            );
        }
        
        List<TimeSlotResponse> timeSlotResponses = timeSlotPage.getContent().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
        
        // Generate next cursor
        String nextCursor = null;
        boolean hasNextPage = timeSlotPage.hasNext();
        
        if (hasNextPage && !timeSlotResponses.isEmpty()) {
            TimeSlot lastSlot = timeSlotPage.getContent().get(timeSlotResponses.size() - 1);
            nextCursor = CursorEncoder.encode(lastSlot.getSlotDateTime(), lastSlot.getId());
        }
        
        return PagedTimeSlotResponse.builder()
            .timeSlots(timeSlotResponses)
            .nextCursor(nextCursor)
            .hasNextPage(hasNextPage)
            .pageSize(pageSize)
            .build();
    }
    
    private TimeSlotResponse mapToResponse(TimeSlot timeSlot) {
        return TimeSlotResponse.builder()
            .id(timeSlot.getId())
            .interviewerId(timeSlot.getInterviewer().getId())
            .slotDateTime(timeSlot.getSlotDateTime())
            .status(timeSlot.getStatus())
            .build();
    }
}

