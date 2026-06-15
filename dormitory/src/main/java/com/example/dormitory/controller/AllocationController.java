package com.example.dormitory.controller;

import com.example.dormitory.dto.AllocationDto;
import com.example.dormitory.dto.AllocationResultDto;
import com.example.dormitory.dto.RoomSuggestionDto;
import com.example.dormitory.security.UserDetailsImpl;
import com.example.dormitory.service.AllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/allocations")
@RequiredArgsConstructor
public class AllocationController {
    private final AllocationService allocationService;

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AllocationDto> getMyAllocation(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        AllocationDto allocation = allocationService.getAllocationByUser(currentUser.getUser());
        return ResponseEntity.ok(allocation);
    }

    @PostMapping("/auto")
    @PreAuthorize("hasAnyRole('WARDEN', 'ADMIN')")
    public ResponseEntity<AllocationResultDto> runAutoAllocation(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestParam(required = false) Long dormitoryId) {
        Long targetDormitoryId = dormitoryId;
        if (currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_WARDEN"))) {
            targetDormitoryId = currentUser.getDormitoryId();
            if (targetDormitoryId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Вам не назначено общежитие. Попросите администратора назначить общежитие.");
            }
        }
        if (targetDormitoryId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите общежитие для распределения");
        }
        AllocationResultDto result = allocationService.runAllocation(targetDormitoryId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/manual")
    @PreAuthorize("hasAnyRole('WARDEN', 'ADMIN')")
    public ResponseEntity<AllocationDto> manualAllocation(@RequestParam Long studentId,
                                                          @RequestParam Long roomId,
                                                          @AuthenticationPrincipal UserDetailsImpl currentUser) {
        AllocationDto allocation = allocationService.manualAllocate(studentId, roomId, currentUser.getUser().getId());
        return ResponseEntity.ok(allocation);
    }

    @PostMapping("/confirm")
    @PreAuthorize("hasAnyRole('WARDEN', 'ADMIN')")
    public ResponseEntity<Void> confirmAllocation() {
        allocationService.confirmAllocation();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{allocationId}/relocate-suggestions")
    @PreAuthorize("hasAnyRole('WARDEN', 'ADMIN')")
    public ResponseEntity<List<RoomSuggestionDto>> relocateSuggestions(@PathVariable Long allocationId) {
        return ResponseEntity.ok(allocationService.suggestRoomsForRelocation(allocationId));
    }

    @PostMapping("/{allocationId}/relocate")
    @PreAuthorize("hasAnyRole('WARDEN', 'ADMIN')")
    public ResponseEntity<AllocationDto> relocateStudent(@PathVariable Long allocationId,
                                                         @RequestParam Long targetRoomId,
                                                         @AuthenticationPrincipal UserDetailsImpl currentUser) {
        AllocationDto allocation = allocationService.relocateStudent(allocationId, targetRoomId, currentUser.getUser().getId());
        return ResponseEntity.ok(allocation);
    }

}