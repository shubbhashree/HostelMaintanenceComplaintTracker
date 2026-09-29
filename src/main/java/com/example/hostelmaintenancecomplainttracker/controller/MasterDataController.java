package com.example.hostelmaintenancecomplainttracker.controller;

import com.example.hostelmaintenancecomplainttracker.entity.Category;
import com.example.hostelmaintenancecomplainttracker.entity.Resident;
import com.example.hostelmaintenancecomplainttracker.entity.Staff;
import com.example.hostelmaintenancecomplainttracker.repository.CategoryRepository;
import com.example.hostelmaintenancecomplainttracker.repository.ResidentRepository;
import com.example.hostelmaintenancecomplainttracker.repository.StaffRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MasterDataController {

    private final CategoryRepository categoryRepo;
    private final ResidentRepository residentRepo;
    private final StaffRepository staffRepo;

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public Category addCategory(@Valid @RequestBody Category category) {
        return categoryRepo.save(category);
    }

    @PostMapping("/residents")
    @ResponseStatus(HttpStatus.CREATED)
    public Resident addResident(@Valid @RequestBody Resident resident) {
        return residentRepo.save(resident);
    }

    @PostMapping("/staff")
    @ResponseStatus(HttpStatus.CREATED)
    public Staff addStaff(@Valid @RequestBody Staff staff) {
        return staffRepo.save(staff);
    }

    @GetMapping("/categories")
    public List<Category> allCategories() {
        return categoryRepo.findAll();
    }

    @GetMapping("/residents")
    public List<Resident> allResidents() {
        return residentRepo.findAll();
    }

    @GetMapping("/staff")
    public List<Staff> allStaff() {
        return staffRepo.findAll();
    }
}