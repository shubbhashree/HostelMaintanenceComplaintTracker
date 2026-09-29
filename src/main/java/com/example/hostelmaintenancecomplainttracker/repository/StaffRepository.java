package com.example.hostelmaintenancecomplainttracker.repository;

import com.example.hostelmaintenancecomplainttracker.entity.Role;
import com.example.hostelmaintenancecomplainttracker.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StaffRepository extends JpaRepository<Staff, Long> {

    List<Staff> findByRole(Role role);
}