package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "candidate_bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateBooking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id", nullable = false, unique = true)
    private TimeSlot timeSlot;
    
    @Column(nullable = false)
    private String candidateName;
    
    @Column(nullable = false)
    private String candidateEmail;
    
    @Column(name = "booking_date_time", nullable = false, updatable = false)
    private LocalDateTime bookingDateTime;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        bookingDateTime = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

