package com.example.demo.service;

import com.example.demo.domain.entity.AvailabilitySlot;
import com.example.demo.domain.entity.Interviewer;
import com.example.demo.domain.entity.TimeSlot;
import com.example.demo.domain.repository.AvailabilitySlotRepository;
import com.example.demo.domain.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeSlotGenerationService {
    
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final TimeSlotRepository timeSlotRepository;
    private static final int WEEKS_TO_GENERATE = 2;
    
    @Transactional
    public void generateTimeSlotsForNextWeeks(Interviewer interviewer) {
        log.info("Generating time slots for interviewer: {}", interviewer.getId());
        
        List<AvailabilitySlot> availabilitySlots = availabilitySlotRepository.findByInterviewerId(interviewer.getId());
        if (availabilitySlots.isEmpty()) {
            log.warn("No availability slots found for interviewer: {}", interviewer.getId());
            return;
        }
        
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusWeeks(WEEKS_TO_GENERATE);
        
        List<TimeSlot> timeSlotsToCreate = new ArrayList<>();
        
        LocalDate currentDate = startDate;
        while (currentDate.isBefore(endDate)) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            
            List<AvailabilitySlot> slotsForDay = availabilitySlots.stream()
                .filter(slot -> slot.getDayOfWeek() == dayOfWeek)
                .collect(Collectors.toList());
            
            for (AvailabilitySlot availabilitySlot : slotsForDay) {
                List<TimeSlot> slots = generateSlotsForAvailability(
                    interviewer, 
                    currentDate, 
                    availabilitySlot
                );
                timeSlotsToCreate.addAll(slots);
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        // Remove duplicates and save
        List<TimeSlot> existingSlots = timeSlotRepository.findByInterviewerIdAndSlotDateTimeBetween(
            interviewer.getId(),
            startDate.atStartOfDay(),
            endDate.atStartOfDay()
        );
        
        // Filter out slots that already exist
        List<TimeSlot> newSlots = timeSlotsToCreate.stream()
            .filter(newSlot -> existingSlots.stream()
                .noneMatch(existing -> existing.getSlotDateTime().equals(newSlot.getSlotDateTime())))
            .collect(Collectors.toList());
        
        if (!newSlots.isEmpty()) {
            timeSlotRepository.saveAll(newSlots);
            log.info("Generated {} new time slots for interviewer: {}", newSlots.size(), interviewer.getId());
        }
    }
    
    private List<TimeSlot> generateSlotsForAvailability(
        Interviewer interviewer,
        LocalDate date,
        AvailabilitySlot availabilitySlot
    ) {
        List<TimeSlot> slots = new ArrayList<>();
        LocalTime startTime = availabilitySlot.getStartTime();
        LocalTime endTime = availabilitySlot.getEndTime();
        int duration = availabilitySlot.getSlotDurationMinutes();
        
        LocalTime currentTime = startTime;
        while (currentTime.plusMinutes(duration).isBefore(endTime) || 
               currentTime.plusMinutes(duration).equals(endTime)) {
            LocalDateTime slotDateTime = LocalDateTime.of(date, currentTime);
            
            // Only create slots in the future
            if (slotDateTime.isAfter(LocalDateTime.now())) {
                TimeSlot timeSlot = TimeSlot.builder()
                    .interviewer(interviewer)
                    .slotDateTime(slotDateTime)
                    .status(TimeSlot.TimeSlotStatus.AVAILABLE)
                    .build();
                slots.add(timeSlot);
            }
            
            currentTime = currentTime.plusMinutes(duration);
        }
        
        return slots;
    }
}

