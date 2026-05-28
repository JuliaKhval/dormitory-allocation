package com.example.dormitory.controller;

import com.example.dormitory.dto.RoomDto;
import com.example.dormitory.dto.StudentProfileDto;
import com.example.dormitory.entity.Dormitory;
import com.example.dormitory.enums.RequestPreferenceStatus;
import com.example.dormitory.security.UserDetailsImpl;
import com.example.dormitory.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/warden")
@RequiredArgsConstructor
public class WardenController {

    private final RequestPreferenceService requestPreferenceService;
    private final StudentService studentService;
    private final WardenService wardenService;
    private final RoomService roomService;
private final AllocationService allocationService;

    @PutMapping("/preferences/{id}")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<Void> updatePreferenceStatus(@PathVariable Long id,
                                                       @RequestParam RequestPreferenceStatus status) {
        requestPreferenceService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/students-to-allocate")
    @PreAuthorize("hasAnyRole('WARDEN', 'ADMIN')")
    public ResponseEntity<List<StudentProfileDto>> getStudentsToAllocate() {
        return ResponseEntity.ok(studentService.getStudentsToAllocate());
    }
    @DeleteMapping("/allocations/{allocationId}")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<Void> evictStudent(@PathVariable Long allocationId) {
        allocationService.evictStudent(allocationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rooms")
    @PreAuthorize("hasAnyRole('STUDENT', 'WARDEN', 'ADMIN')")
    public ResponseEntity<List<RoomDto>> getAllRooms(
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) String type,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        Long dormitoryId = null;
        if (currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_WARDEN"))) {

            Dormitory dorm = wardenService.getWardenDormitory(currentUser.getUser().getId());
            if (dorm != null) dormitoryId = dorm.getId();
        }
        return ResponseEntity.ok(roomService.getAllRooms(floor, type, dormitoryId));
    }
}