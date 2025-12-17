package com.example.demo.controller;

import com.example.demo.dto.request.BookSlotRequest;
import com.example.demo.dto.request.UpdateBookingRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.BookingResponse;
import com.example.demo.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {
    
    private final BookingService bookingService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> bookSlot(
            @Valid @RequestBody BookSlotRequest request) {
        BookingResponse booking = bookingService.bookSlot(request);
        
        ApiResponse<BookingResponse> response = ApiResponse.<BookingResponse>builder()
            .success(true)
            .message("Slot booked successfully")
            .data(booking)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping
    public ResponseEntity<ApiResponse<BookingResponse>> updateBooking(
            @Valid @RequestBody UpdateBookingRequest request) {
        BookingResponse booking = bookingService.updateBooking(request);
        
        ApiResponse<BookingResponse> response = ApiResponse.<BookingResponse>builder()
            .success(true)
            .message("Booking updated successfully")
            .data(booking)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(@PathVariable Long bookingId) {
        BookingResponse booking = bookingService.getBookingById(bookingId);
        
        ApiResponse<BookingResponse> response = ApiResponse.<BookingResponse>builder()
            .success(true)
            .message("Booking retrieved successfully")
            .data(booking)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
}

