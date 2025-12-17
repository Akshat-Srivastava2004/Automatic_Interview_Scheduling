package com.example.demo.controller;

import com.example.demo.dto.request.BookSlotRequest;
import com.example.demo.dto.response.BookingResponse;
import com.example.demo.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private BookingService bookingService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void testBookSlot_Success() throws Exception {
        // Arrange
        BookSlotRequest request = new BookSlotRequest();
        request.setTimeSlotId(1L);
        request.setCandidateName("Priya Patel");
        request.setCandidateEmail("priya@example.com");
        
        BookingResponse response = BookingResponse.builder()
            .bookingId(1L)
            .timeSlotId(1L)
            .candidateName("Priya Patel")
            .candidateEmail("priya@example.com")
            .bookingDateTime(LocalDateTime.now())
            .slotDateTime(LocalDateTime.now().plusDays(1))
            .build();
        
        when(bookingService.bookSlot(any(BookSlotRequest.class))).thenReturn(response);
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bookingId").value(1L))
                .andExpect(jsonPath("$.data.candidateName").value("Priya Patel"));
    }
    
    @Test
    void testBookSlot_ValidationError() throws Exception {
        // Arrange
        BookSlotRequest request = new BookSlotRequest();
        // Missing required fields
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testGetBooking_Success() throws Exception {
        // Arrange
        BookingResponse response = BookingResponse.builder()
            .bookingId(1L)
            .timeSlotId(1L)
            .candidateName("Priya Patel")
            .candidateEmail("priya@example.com")
            .bookingDateTime(LocalDateTime.now())
            .slotDateTime(LocalDateTime.now().plusDays(1))
            .build();
        
        when(bookingService.getBookingById(1L)).thenReturn(response);
        
        // Act & Assert
        mockMvc.perform(get("/api/v1/bookings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bookingId").value(1L));
    }
}

