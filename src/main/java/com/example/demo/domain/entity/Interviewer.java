package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "interviewers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interviewer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(name = "max_interviews_per_week", nullable = false)
    private Integer maxInterviewsPerWeek;
    
    @Version
    private Long version; // Optimistic locking for race condition handling
    
    @OneToMany(mappedBy = "interviewer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AvailabilitySlot> availabilitySlots = new ArrayList<>();
    
    @OneToMany(mappedBy = "interviewer", cascade = CascadeType.ALL)
    @Builder.Default
    private List<TimeSlot> timeSlots = new ArrayList<>();
}

