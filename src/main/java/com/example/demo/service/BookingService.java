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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {
    
    private final TimeSlotRepository timeSlotRepository;
    private final CandidateBookingRepository bookingRepository;
    private final InterviewerRepository interviewerRepository;
    
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public BookingResponse bookSlot(BookSlotRequest request) {
        log.info("Booking slot {} for candidate: {}", request.getTimeSlotId(), request.getCandidateEmail());
        
        // Check if candidate already has a booking
        if (bookingRepository.findByCandidateEmail(request.getCandidateEmail()).stream()
            .anyMatch(booking -> booking.getTimeSlot().getStatus() == TimeSlot.TimeSlotStatus.BOOKED)) {
            throw new BusinessException("Candidate already has an active booking");
        }
        
        // Get time slot with optimistic lock
        TimeSlot timeSlot = timeSlotRepository.findById(request.getTimeSlotId())
            .orElseThrow(() -> new ResourceNotFoundException("Time slot not found with id: " + request.getTimeSlotId()));
        
        // Validate slot is available
        if (timeSlot.getStatus() != TimeSlot.TimeSlotStatus.AVAILABLE) {
            throw new BusinessException("Time slot is not available");
        }
        
        // Check max interviews per week constraint
        validateMaxInterviewsPerWeek(timeSlot.getInterviewer());
        
        try {
            // Create booking
            CandidateBooking booking = CandidateBooking.builder()
                .timeSlot(timeSlot)
                .candidateName(request.getCandidateName())
                .candidateEmail(request.getCandidateEmail())
                .build();
            
            // Update time slot status (this will trigger optimistic lock check)
            timeSlot.setStatus(TimeSlot.TimeSlotStatus.BOOKED);
            
            CandidateBooking savedBooking = bookingRepository.save(booking);
            timeSlotRepository.save(timeSlot);
            
            log.info("Slot booked successfully. Booking ID: {}", savedBooking.getId());
            
            return BookingResponse.builder()
                .bookingId(savedBooking.getId())
                .timeSlotId(savedBooking.getTimeSlot().getId())
                .candidateName(savedBooking.getCandidateName())
                .candidateEmail(savedBooking.getCandidateEmail())
                .bookingDateTime(savedBooking.getBookingDateTime())
                .updatedAt(savedBooking.getUpdatedAt())
                .slotDateTime(savedBooking.getTimeSlot().getSlotDateTime())
                .build();
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic lock failure while booking slot: {}", request.getTimeSlotId());
            throw new com.example.demo.exception.ConcurrentModificationException(
                "Slot was modified by another transaction. Please try again."
            );
        }
    }
    
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public BookingResponse updateBooking(UpdateBookingRequest request) {
        log.info("Updating booking {} to new slot {}", request.getBookingId(), request.getNewTimeSlotId());
        
        // Get existing booking
        CandidateBooking existingBooking = bookingRepository.findById(request.getBookingId())
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + request.getBookingId()));
        
        // Validate candidate email matches
        if (!existingBooking.getCandidateEmail().equals(request.getCandidateEmail())) {
            throw new BusinessException("Candidate email does not match booking");
        }
        
        // Get old time slot and release it
        TimeSlot oldTimeSlot = existingBooking.getTimeSlot();
        oldTimeSlot.setStatus(TimeSlot.TimeSlotStatus.AVAILABLE);
        timeSlotRepository.save(oldTimeSlot);
        
        // Get new time slot
        TimeSlot newTimeSlot = timeSlotRepository.findById(request.getNewTimeSlotId())
            .orElseThrow(() -> new ResourceNotFoundException("Time slot not found with id: " + request.getNewTimeSlotId()));
        
        // Validate new slot is available
        if (newTimeSlot.getStatus() != TimeSlot.TimeSlotStatus.AVAILABLE) {
            throw new BusinessException("New time slot is not available");
        }
        
        // Check max interviews per week constraint
        validateMaxInterviewsPerWeek(newTimeSlot.getInterviewer());
        
        try {
            // Update booking
            existingBooking.setTimeSlot(newTimeSlot);
            existingBooking.setCandidateName(request.getCandidateName());
            
            newTimeSlot.setStatus(TimeSlot.TimeSlotStatus.BOOKED);
            
            CandidateBooking updatedBooking = bookingRepository.save(existingBooking);
            timeSlotRepository.save(newTimeSlot);
            
            log.info("Booking updated successfully. Booking ID: {}", updatedBooking.getId());
            
            return BookingResponse.builder()
                .bookingId(updatedBooking.getId())
                .timeSlotId(updatedBooking.getTimeSlot().getId())
                .candidateName(updatedBooking.getCandidateName())
                .candidateEmail(updatedBooking.getCandidateEmail())
                .bookingDateTime(updatedBooking.getBookingDateTime())
                .updatedAt(updatedBooking.getUpdatedAt())
                .slotDateTime(updatedBooking.getTimeSlot().getSlotDateTime())
                .build();
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic lock failure while updating booking: {}", request.getBookingId());
            throw new com.example.demo.exception.ConcurrentModificationException(
                "Slot was modified by another transaction. Please try again."
            );
        }
    }
    
    private void validateMaxInterviewsPerWeek(Interviewer interviewer) {
        LocalDateTime now = LocalDateTime.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        
        // Get the start of the current week (Monday)
        LocalDateTime weekStart = now.with(weekFields.dayOfWeek(), 1)
            .withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime weekEnd = weekStart.plusWeeks(1);
        
        Long bookedCount = timeSlotRepository.countBookedSlotsForWeek(
            interviewer.getId(), 
            weekStart, 
            weekEnd
        );
        
        if (bookedCount >= interviewer.getMaxInterviewsPerWeek()) {
            throw new BusinessException(
                String.format("Interviewer has reached maximum interviews per week limit (%d)", 
                    interviewer.getMaxInterviewsPerWeek())
            );
        }
    }
    
    public BookingResponse getBookingById(Long bookingId) {
        CandidateBooking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        
        return BookingResponse.builder()
            .bookingId(booking.getId())
            .timeSlotId(booking.getTimeSlot().getId())
            .candidateName(booking.getCandidateName())
            .candidateEmail(booking.getCandidateEmail())
            .bookingDateTime(booking.getBookingDateTime())
            .updatedAt(booking.getUpdatedAt())
            .slotDateTime(booking.getTimeSlot().getSlotDateTime())
            .build();
    }
}

