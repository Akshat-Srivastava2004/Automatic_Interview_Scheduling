package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "time_slots", indexes = {
    @Index(name = "idx_interviewer_date", columnList = "interviewer_id,slot_date_time"),
    @Index(name = "idx_status_date", columnList = "status,slot_date_time"),
    @Index(name = "idx_date_cursor", columnList = "slot_date_time,id") // For cursor pagination
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interviewer_id", nullable = false)
    private Interviewer interviewer;
    
    @Column(name = "slot_date_time", nullable = false)
    private LocalDateTime slotDateTime;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TimeSlotStatus status = TimeSlotStatus.AVAILABLE;
    
    @OneToOne(mappedBy = "timeSlot", cascade = CascadeType.ALL, orphanRemoval = true)
    private CandidateBooking booking;
    
    @Version
    private Long version; // Optimistic locking for race condition handling
    
    public enum TimeSlotStatus {
        AVAILABLE,
        BOOKED,
        CANCELLED
    }
}

