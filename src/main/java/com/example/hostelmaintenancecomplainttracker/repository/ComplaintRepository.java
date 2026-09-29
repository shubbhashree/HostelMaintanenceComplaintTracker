package com.example.hostelmaintenancecomplainttracker.repository;

import com.example.hostelmaintenancecomplainttracker.entity.Complaint;
import com.example.hostelmaintenancecomplainttracker.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findByResidentIdOrderByCreatedAtDesc(Long residentId);

    List<Complaint> findByStatusNotOrderByCreatedAtAsc(Status status);
    long countByStatus(Status status);
    List<Complaint> findByCategoryIdOrderByCreatedAtDesc(Long categoryId);
    long countByAssignedToIdAndStatusNot(Long staffId, Status status);
}