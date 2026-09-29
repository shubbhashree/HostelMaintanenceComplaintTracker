package com.example.hostelmaintenancecomplainttracker.repository;

import com.example.hostelmaintenancecomplainttracker.entity.Resident;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResidentRepository extends JpaRepository<Resident, Long> {
}