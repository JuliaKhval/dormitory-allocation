package com.example.dormitory.controller;

import com.example.dormitory.dto.*;
import com.example.dormitory.entity.Dormitory;
import com.example.dormitory.enums.RequestPreferenceStatus;
import com.example.dormitory.security.UserDetailsImpl;
import com.example.dormitory.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
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
    private final AllocationSettingsService allocationSettingsService;

    @GetMapping("/context")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<WardenContextDto> getWardenContext(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        WardenContextDto dto = new WardenContextDto();
        Dormitory dorm = wardenService.getWardenDormitory(currentUser.getUser().getId());
        if (dorm != null) {
            dto.setDormitoryAssigned(true);
            dto.setDormitoryId(dorm.getId());
            dto.setDormitoryName(dorm.getName());
        } else {
            dto.setDormitoryAssigned(false);
        }
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/preferences/{id}")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<Void> updatePreferenceStatus(@PathVariable Long id,
                                                       @RequestBody UpdatePreferenceDto dto) {
        requestPreferenceService.updateStatus(id, dto.getStatus(), dto.getRejectionReason());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/students-to-allocate")
    @PreAuthorize("hasAnyRole('WARDEN', 'ADMIN')")
    public ResponseEntity<List<StudentProfileDto>> getStudentsToAllocate(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Long facultyId,
            @RequestParam(required = false) Long countryId) {
        return ResponseEntity.ok(studentService.getStudentsToAllocate(name, gender, facultyId, countryId));
    }

    @GetMapping("/allocation-settings")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<AllocationSettingsDto> getAllocationSettings(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        Long dormitoryId = requireWardenDormitoryId(currentUser);
        return ResponseEntity.ok(allocationSettingsService.getForDormitory(dormitoryId));
    }

    @PutMapping("/allocation-settings")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<AllocationSettingsDto> saveAllocationSettings(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestBody AllocationSettingsDto dto) {
        Long dormitoryId = requireWardenDormitoryId(currentUser);
        return ResponseEntity.ok(allocationSettingsService.save(dormitoryId, dto));
    }

    private Long requireWardenDormitoryId(UserDetailsImpl currentUser) {
        Long dormitoryId = currentUser.getDormitoryId();
        if (dormitoryId == null) {
            Dormitory dorm = wardenService.getWardenDormitory(currentUser.getUser().getId());
            if (dorm != null) {
                dormitoryId = dorm.getId();
            }
        }
        if (dormitoryId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Вам не назначено общежитие. Попросите администратора назначить общежитие в разделе «Пользователи».");
        }
        return dormitoryId;
    }
    @DeleteMapping("/allocations/{allocationId}")
    @PreAuthorize("hasAnyRole('WARDEN', 'ADMIN')")
    public ResponseEntity<Void> evictStudent(@PathVariable Long allocationId) {
        System.out.println("=== DELETE /allocations/" + allocationId + " called");
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