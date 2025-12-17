package com.example.demo.service;

import com.example.demo.domain.entity.Interviewer;
import com.example.demo.domain.entity.TimeSlot;
import com.example.demo.domain.repository.TimeSlotRepository;
import com.example.demo.dto.response.PagedTimeSlotResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeSlotServiceTest {
    
    @Mock
    private TimeSlotRepository timeSlotRepository;
    
    @InjectMocks
    private TimeSlotService timeSlotService;
    
    private Interviewer interviewer;
    private List<TimeSlot> timeSlots;
    
    @BeforeEach
    void setUp() {
        interviewer = Interviewer.builder()
            .id(1L)
            .name("Rajesh Kumar")
            .email("rajesh@example.com")
            .maxInterviewsPerWeek(5)
            .build();
        
        timeSlots = Arrays.asList(
            TimeSlot.builder()
                .id(1L)
                .interviewer(interviewer)
                .slotDateTime(LocalDateTime.now().plusDays(1))
                .status(TimeSlot.TimeSlotStatus.AVAILABLE)
                .build(),
            TimeSlot.builder()
                .id(2L)
                .interviewer(interviewer)
                .slotDateTime(LocalDateTime.now().plusDays(2))
                .status(TimeSlot.TimeSlotStatus.AVAILABLE)
                .build()
        );
    }
    
    @Test
    void testGetAvailableSlots_FirstPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<TimeSlot> page = new PageImpl<>(timeSlots, pageable, 2);
        
        when(timeSlotRepository.findAvailableSlots(
            eq(TimeSlot.TimeSlotStatus.AVAILABLE), 
            any(Pageable.class)
        )).thenReturn(page);
        
        // Act
        PagedTimeSlotResponse response = timeSlotService.getAvailableSlots(null, 20);
        
        // Assert
        assertNotNull(response);
        assertEquals(2, response.getTimeSlots().size());
        assertFalse(response.getHasNextPage());
        verify(timeSlotRepository, times(1)).findAvailableSlots(
            eq(TimeSlot.TimeSlotStatus.AVAILABLE), 
            any(Pageable.class)
        );
    }
    
    @Test
    void testGetAvailableSlots_WithCursor() {
        // Arrange
        String cursor = "cursor123";
        Pageable pageable = PageRequest.of(0, 20);
        Page<TimeSlot> page = new PageImpl<>(timeSlots, pageable, 2);
        
        when(timeSlotRepository.findAvailableSlotsAfterCursor(
            eq(TimeSlot.TimeSlotStatus.AVAILABLE),
            any(LocalDateTime.class),
            any(Long.class),
            any(Pageable.class)
        )).thenReturn(page);
        
        // Act
        PagedTimeSlotResponse response = timeSlotService.getAvailableSlots(cursor, 20);
        
        // Assert
        assertNotNull(response);
        assertEquals(2, response.getTimeSlots().size());
    }
    
    @Test
    void testGetAvailableSlots_DefaultPageSize() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<TimeSlot> page = new PageImpl<>(timeSlots, pageable, 2);
        
        when(timeSlotRepository.findAvailableSlots(
            eq(TimeSlot.TimeSlotStatus.AVAILABLE), 
            any(Pageable.class)
        )).thenReturn(page);
        
        // Act
        PagedTimeSlotResponse response = timeSlotService.getAvailableSlots(null, null);
        
        // Assert
        assertNotNull(response);
        assertEquals(20, response.getPageSize());
    }
    
    @Test
    void testGetAvailableSlots_MaxPageSizeLimit() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 100);
        Page<TimeSlot> page = new PageImpl<>(timeSlots, pageable, 2);
        
        when(timeSlotRepository.findAvailableSlots(
            eq(TimeSlot.TimeSlotStatus.AVAILABLE), 
            any(Pageable.class)
        )).thenReturn(page);
        
        // Act
        PagedTimeSlotResponse response = timeSlotService.getAvailableSlots(null, 200);
        
        // Assert
        assertNotNull(response);
        assertEquals(100, response.getPageSize());
    }
}

