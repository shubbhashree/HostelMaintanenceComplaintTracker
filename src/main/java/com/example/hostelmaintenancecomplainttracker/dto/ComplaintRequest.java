package com.example.hostelmaintenancecomplainttracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ComplaintRequest(
        @NotNull(message = "residentId is required") Long residentId,
        @NotNull(message = "categoryId is required") Long categoryId,
        @NotBlank(message = "roomNumber is required") String roomNumber,
        @NotBlank(message = "description is required") String description
) {
}