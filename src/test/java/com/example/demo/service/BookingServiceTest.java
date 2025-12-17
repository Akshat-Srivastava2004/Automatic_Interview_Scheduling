package com.example.demo.service;

import com.example.demo.domain.entity.CandidateBooking;
import com.example.demo.domain.entity.Interviewer;
import com.example.demo.domain.entity.TimeSlot;
import com.example.demo.domain.repository.CandidateBookingRepository;
import com.example.demo.domain.repository.InterviewerRepository;
import com.example.demo.domain.repository.TimeSlotRepository;
import com.example.demo.dto.request.BookSlotRequest;
import com.example.demo.dto.request.UpdateBookingRequest;
import com.example.demo.dto.response.BookingResponse;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    
    @Mock
    private TimeSlotRepository timeSlotRepository;
    
    @Mock
    private CandidateBookingRepository bookingRepository;
    
    @Mock
    private InterviewerRepository interviewerRepository;
    
    @InjectMocks
    private BookingService bookingService;
    
    private Interviewer interviewer;
    private TimeSlot availableTimeSlot;
    private TimeSlot bookedTimeSlot;
    
    @BeforeEach
    void setUp() {
        interviewer = Interviewer.builder()
            .id(1L)
            .name("Rajesh Kumar")
            .email("rajesh@example.com")
            .maxInterviewsPerWeek(5)
            .version(1L)
            .build();
        
        availableTimeSlot = TimeSlot.builder()
            .id(1L)
            .interviewer(interviewer)
            .slotDateTime(LocalDateTime.now().plusDays(1))
            .status(TimeSlot.TimeSlotStatus.AVAILABLE)
            .version(1L)
            .build();
        
        bookedTimeSlot = TimeSlot.builder()
            .id(2L)
            .interviewer(interviewer)
            .slotDateTime(LocalDateTime.now().plusDays(2))
            .status(TimeSlot.TimeSlotStatus.BOOKED)
            .version(1L)
            .build();
    }
    
    @Test
    void testBookSlot_Success() {
        // Arrange
        BookSlotRequest request = new BookSlotRequest();
        request.setTimeSlotId(1L);
        request.setCandidateName("Priya Patel");
        request.setCandidateEmail("priya@example.com");
        
        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(availableTimeSlot));
        when(bookingRepository.findByCandidateEmail("priya@example.com"))
            .thenReturn(Collections.emptyList());
        when(timeSlotRepository.countBookedSlotsForWeek(any(), any(), any())).thenReturn(2L);
        when(bookingRepository.save(any(CandidateBooking.class))).thenAnswer(invocation -> {
            CandidateBooking booking = invocation.getArgument(0);
            booking.setId(1L);
            return booking;
        });
        when(timeSlotRepository.save(any(TimeSlot.class))).thenReturn(availableTimeSlot);
        
        // Act
        BookingResponse response = bookingService.bookSlot(request);
        
        // Assert
        assertNotNull(response);
        assertEquals("Priya Patel", response.getCandidateName());
        assertEquals("priya@example.com", response.getCandidateEmail());
        assertEquals(1L, response.getTimeSlotId());
        verify(bookingRepository, times(1)).save(any(CandidateBooking.class));
        verify(timeSlotRepository, times(1)).save(any(TimeSlot.class));
    }
    
    @Test
    void testBookSlot_TimeSlotNotFound() {
        // Arrange
        BookSlotRequest request = new BookSlotRequest();
        request.setTimeSlotId(999L);
        request.setCandidateName("Priya Patel");
        request.setCandidateEmail("priya@example.com");
        
        when(timeSlotRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            bookingService.bookSlot(request);
        });
    }
    
    @Test
    void testBookSlot_SlotNotAvailable() {
        // Arrange
        BookSlotRequest request = new BookSlotRequest();
        request.setTimeSlotId(2L);
        request.setCandidateName("Priya Patel");
        request.setCandidateEmail("priya@example.com");
        
        when(timeSlotRepository.findById(2L)).thenReturn(Optional.of(bookedTimeSlot));
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            bookingService.bookSlot(request);
        });
    }
    
    @Test
    void testBookSlot_CandidateAlreadyHasBooking() {
        // Arrange
        BookSlotRequest request = new BookSlotRequest();
        request.setTimeSlotId(1L);
        request.setCandidateName("Priya Patel");
        request.setCandidateEmail("priya@example.com");
        
        CandidateBooking existingBooking = CandidateBooking.builder()
            .id(1L)
            .timeSlot(bookedTimeSlot)
            .candidateEmail("priya@example.com")
            .build();
        
        when(bookingRepository.findByCandidateEmail("priya@example.com"))
            .thenReturn(Collections.singletonList(existingBooking));
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            bookingService.bookSlot(request);
        });
    }
    
    @Test
    void testBookSlot_MaxInterviewsPerWeekExceeded() {
        // Arrange
        BookSlotRequest request = new BookSlotRequest();
        request.setTimeSlotId(1L);
        request.setCandidateName("Priya Patel");
        request.setCandidateEmail("priya@example.com");
        
        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(availableTimeSlot));
        when(bookingRepository.findByCandidateEmail("priya@example.com"))
            .thenReturn(Collections.emptyList());
        when(timeSlotRepository.countBookedSlotsForWeek(any(), any(), any())).thenReturn(5L);
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            bookingService.bookSlot(request);
        });
    }
    
    @Test
    void testUpdateBooking_Success() {
        // Arrange
        UpdateBookingRequest request = new UpdateBookingRequest();
        request.setBookingId(1L);
        request.setNewTimeSlotId(3L);
        request.setCandidateName("Priya Patel Updated");
        request.setCandidateEmail("priya@example.com");
        
        TimeSlot newTimeSlot = TimeSlot.builder()
            .id(3L)
            .interviewer(interviewer)
            .slotDateTime(LocalDateTime.now().plusDays(3))
            .status(TimeSlot.TimeSlotStatus.AVAILABLE)
            .version(1L)
            .build();
        
        CandidateBooking existingBooking = CandidateBooking.builder()
            .id(1L)
            .timeSlot(bookedTimeSlot)
            .candidateName("Priya Patel")
            .candidateEmail("priya@example.com")
            .bookingDateTime(LocalDateTime.now())
            .build();
        
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(existingBooking));
        when(timeSlotRepository.findById(3L)).thenReturn(Optional.of(newTimeSlot));
        when(timeSlotRepository.countBookedSlotsForWeek(any(), any(), any())).thenReturn(2L);
        when(bookingRepository.save(any(CandidateBooking.class))).thenReturn(existingBooking);
        when(timeSlotRepository.save(any(TimeSlot.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        BookingResponse response = bookingService.updateBooking(request);
        
        // Assert
        assertNotNull(response);
        assertEquals("Priya Patel Updated", response.getCandidateName());
        verify(bookingRepository, times(1)).save(any(CandidateBooking.class));
        verify(timeSlotRepository, times(2)).save(any(TimeSlot.class));
    }
    
    @Test
    void testUpdateBooking_BookingNotFound() {
        // Arrange
        UpdateBookingRequest request = new UpdateBookingRequest();
        request.setBookingId(999L);
        request.setNewTimeSlotId(3L);
        request.setCandidateName("Priya Patel");
        request.setCandidateEmail("priya@example.com");
        
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            bookingService.updateBooking(request);
        });
    }
}

