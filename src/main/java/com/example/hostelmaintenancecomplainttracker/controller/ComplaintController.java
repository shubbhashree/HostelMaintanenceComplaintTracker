package com.example.hostelmaintenancecomplainttracker.controller;

import com.example.hostelmaintenancecomplainttracker.dto.ComplaintRequest;
import com.example.hostelmaintenancecomplainttracker.dto.ComplaintResponse;
import com.example.hostelmaintenancecomplainttracker.dto.StatusUpdateRequest;
import com.example.hostelmaintenancecomplainttracker.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComplaintResponse raise(@Valid @RequestBody ComplaintRequest req) {
        return service.raise(req);
    }

    @PutMapping("/{id}/assign")
    public ComplaintResponse assign(@PathVariable Long id,
                                    @RequestParam Long wardenId,
                                    @RequestParam Long staffId) {
        return service.assign(id, wardenId, staffId);
    }

    @PutMapping("/{id}/status")
    public ComplaintResponse updateStatus(@PathVariable Long id,
                                          @Valid @RequestBody StatusUpdateRequest req) {
        return service.updateStatus(id, req);
    }

    @GetMapping("/resident/{residentId}")
    public List<ComplaintResponse> myComplaints(@PathVariable Long residentId) {
        return service.forResident(residentId);
    }

    @GetMapping("/open")
    public List<ComplaintResponse> openComplaints() {
        return service.allOpenByAge();
    }

    @GetMapping("/overdue")
    public List<ComplaintResponse> overdueComplaints() {
        return service.overdue();
    }

    @GetMapping("/dashboard")
    public Map<String, Long> dashboard() {
        return service.dashboard();
    }

    @GetMapping("/category/{categoryId}")
    public List<ComplaintResponse> byCategory(@PathVariable Long categoryId) {
        return service.byCategory(categoryId);
    }
}