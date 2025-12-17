package com.example.demo.domain.repository;

import com.example.demo.domain.entity.TimeSlot;
import com.example.demo.domain.entity.TimeSlot.TimeSlotStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    
    // Cursor-based pagination query
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.status = :status " +
           "AND (ts.slotDateTime > :cursorDateTime OR (ts.slotDateTime = :cursorDateTime AND ts.id > :cursorId)) " +
           "ORDER BY ts.slotDateTime ASC, ts.id ASC")
    Page<TimeSlot> findAvailableSlotsAfterCursor(
        @Param("status") TimeSlotStatus status,
        @Param("cursorDateTime") LocalDateTime cursorDateTime,
        @Param("cursorId") Long cursorId,
        Pageable pageable
    );
    
    // Initial query for cursor pagination (first page)
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.status = :status " +
           "ORDER BY ts.slotDateTime ASC, ts.id ASC")
    Page<TimeSlot> findAvailableSlots(
        @Param("status") TimeSlotStatus status,
        Pageable pageable
    );
    
    // Optimistic locking for race condition handling
    @Lock(LockModeType.OPTIMISTIC)
    Optional<TimeSlot> findById(Long id);
    
    // Find slots by interviewer and date range
    List<TimeSlot> findByInterviewerIdAndSlotDateTimeBetween(
        Long interviewerId, 
        LocalDateTime start, 
        LocalDateTime end
    );
    
    // Count booked slots for a week
    @Query("SELECT COUNT(ts) FROM TimeSlot ts WHERE ts.interviewer.id = :interviewerId " +
           "AND ts.status = 'BOOKED' " +
           "AND ts.slotDateTime >= :weekStart AND ts.slotDateTime < :weekEnd")
    Long countBookedSlotsForWeek(
        @Param("interviewerId") Long interviewerId,
        @Param("weekStart") LocalDateTime weekStart,
        @Param("weekEnd") LocalDateTime weekEnd
    );
}

