package com.example.demo.domain.repository;

import com.example.demo.domain.entity.Interviewer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InterviewerRepository extends JpaRepository<Interviewer, Long> {
    Optional<Interviewer> findByEmail(String email);
}

