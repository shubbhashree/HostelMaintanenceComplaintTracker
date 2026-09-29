package com.example.hostelmaintenancecomplainttracker.service;

import com.example.hostelmaintenancecomplainttracker.dto.ComplaintRequest;
import com.example.hostelmaintenancecomplainttracker.dto.ComplaintResponse;
import com.example.hostelmaintenancecomplainttracker.dto.StatusUpdateRequest;
import com.example.hostelmaintenancecomplainttracker.entity.Category;
import com.example.hostelmaintenancecomplainttracker.entity.Complaint;
import com.example.hostelmaintenancecomplainttracker.entity.Resident;
import com.example.hostelmaintenancecomplainttracker.entity.Role;
import com.example.hostelmaintenancecomplainttracker.entity.Staff;
import com.example.hostelmaintenancecomplainttracker.entity.Status;
import com.example.hostelmaintenancecomplainttracker.exception.ApiException;
import com.example.hostelmaintenancecomplainttracker.repository.CategoryRepository;
import com.example.hostelmaintenancecomplainttracker.repository.ComplaintRepository;
import com.example.hostelmaintenancecomplainttracker.repository.ResidentRepository;
import com.example.hostelmaintenancecomplainttracker.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepo;
    private final ResidentRepository residentRepo;
    private final CategoryRepository categoryRepo;
    private final StaffRepository staffRepo;

    // Feature 1: resident raises a complaint
    public ComplaintResponse raise(ComplaintRequest req) {
        Resident resident = residentRepo.findById(req.residentId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Resident not found"));
        Category category = categoryRepo.findById(req.categoryId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category not found"));

        Complaint c = new Complaint();
        c.setResident(resident);
        c.setCategory(category);
        c.setRoomNumber(req.roomNumber());
        c.setDescription(req.description());
        c.setAssignedTo(pickLeastBusyStaff());
        return ComplaintResponse.from(complaintRepo.save(c));
    }

    // Warden assigns a complaint to a staff member
    @Transactional
    public ComplaintResponse assign(Long complaintId, Long wardenId, Long staffId) {
        Staff warden = findStaff(wardenId);
        if (warden.getRole() != Role.WARDEN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the warden can assign complaints");
        }
        Complaint c = findComplaint(complaintId);
        c.setAssignedTo(findStaff(staffId));
        return ComplaintResponse.from(complaintRepo.save(c));
    }

    // Feature 2: staff updates status (with both business rules)
    @Transactional
    public ComplaintResponse updateStatus(Long complaintId, StatusUpdateRequest req) {
        Complaint c = findComplaint(complaintId);
        Staff staff = findStaff(req.staffId());
        Status current = c.getStatus();
        Status next = req.status();

        if (next == current) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Complaint is already " + current);
        }

        // Rule 1: forward only. Going backward needs a remark.
        if (next.ordinal() < current.ordinal()) {
            if (req.remark() == null || req.remark().isBlank()) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Moving backward from " + current + " to " + next + " requires a remark");
            }
        } else if (next.ordinal() > current.ordinal() + 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Cannot skip a step. Next allowed status is "
                            + Status.values()[current.ordinal() + 1]);
        }

        // Rule 2: only assigned staff or the warden can resolve
        if (next == Status.RESOLVED) {
            boolean isWarden = staff.getRole() == Role.WARDEN;
            boolean isAssigned = c.getAssignedTo() != null
                    && c.getAssignedTo().getId().equals(staff.getId());
            if (!isWarden && !isAssigned) {
                throw new ApiException(HttpStatus.FORBIDDEN,
                        "Only the assigned staff or the warden can resolve this complaint");
            }
        }

        // All checks passed. Now change and save.
        c.setStatus(next);
        if (req.remark() != null && !req.remark().isBlank()) {
            c.setRemark(req.remark());
        }
        c.setResolvedAt(next == Status.RESOLVED ? LocalDateTime.now() : null);
        return ComplaintResponse.from(complaintRepo.save(c));
    }

    // Feature 3: resident views own complaints
    public List<ComplaintResponse> forResident(Long residentId) {
        if (!residentRepo.existsById(residentId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Resident not found");
        }
        return complaintRepo.findByResidentIdOrderByCreatedAtDesc(residentId)
                .stream().map(ComplaintResponse::from).toList();
    }

    // Feature 4: warden views all open complaints, oldest first
    public List<ComplaintResponse> allOpenByAge() {
        return complaintRepo.findByStatusNotOrderByCreatedAtAsc(Status.RESOLVED)
                .stream().map(ComplaintResponse::from).toList();
    }

    // Feature 5: overdue complaints (open more than 5 days)
    public List<ComplaintResponse> overdue() {
        return allOpenByAge().stream().filter(ComplaintResponse::overdue).toList();
    }
    // Dashboard: simple counts
    public Map<String, Long> dashboard() {
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("total", complaintRepo.count());
        result.put("open", complaintRepo.countByStatus(Status.OPEN));
        result.put("inProgress", complaintRepo.countByStatus(Status.IN_PROGRESS));
        result.put("resolved", complaintRepo.countByStatus(Status.RESOLVED));
        result.put("overdue", (long) overdue().size());
        return result;
    }
    // Filter: complaints of one category
    public List<ComplaintResponse> byCategory(Long categoryId) {
        if (!categoryRepo.existsById(categoryId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Category not found");
        }
        return complaintRepo.findByCategoryIdOrderByCreatedAtDesc(categoryId)
                .stream().map(ComplaintResponse::from).toList();
    }
    // Picks the STAFF member with the fewest unresolved complaints
    private Staff pickLeastBusyStaff() {
        return staffRepo.findByRole(Role.STAFF).stream()
                .min(Comparator.comparingLong(
                        s -> complaintRepo.countByAssignedToIdAndStatusNot(s.getId(), Status.RESOLVED)))
                .orElse(null);
    }
    private Complaint findComplaint(Long id) {
        return complaintRepo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Complaint not found"));
    }

    private Staff findStaff(Long id) {
        return staffRepo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Staff not found"));
    }
}