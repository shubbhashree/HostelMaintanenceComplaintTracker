package com.example.hostelmaintenancecomplainttracker.dto;

import com.example.hostelmaintenancecomplainttracker.entity.Status;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull(message = "staffId is required") Long staffId,
        @NotNull(message = "status is required") Status status,
        String remark
) {
}