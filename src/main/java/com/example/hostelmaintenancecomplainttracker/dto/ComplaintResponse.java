package com.example.hostelmaintenancecomplainttracker.dto;

import com.example.hostelmaintenancecomplainttracker.entity.Complaint;
import com.example.hostelmaintenancecomplainttracker.entity.Status;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record ComplaintResponse(
        Long id,
        String category,
        String roomNumber,
        String description,
        Status status,
        LocalDateTime createdAt,
        long ageInDays,
        boolean overdue,
        String assignedTo,
        String remark
) {

    public static ComplaintResponse from(Complaint c) {
        long age = ChronoUnit.DAYS.between(c.getCreatedAt(), LocalDateTime.now());
        boolean overdue = c.getStatus() != Status.RESOLVED && age > 5;

        return new ComplaintResponse(
                c.getId(),
                c.getCategory().getName(),
                c.getRoomNumber(),
                c.getDescription(),
                c.getStatus(),
                c.getCreatedAt(),
                age,
                overdue,
                c.getAssignedTo() == null ? null : c.getAssignedTo().getName(),
                c.getRemark()
        );
    }
}