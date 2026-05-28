package com.example.dormitory.controller;

import com.example.dormitory.dto.AllocationDto;
import com.example.dormitory.dto.AllocationResultDto;
import com.example.dormitory.security.UserDetailsImpl;
import com.example.dormitory.service.AllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<AllocationResultDto> runAutoAllocation() {
        AllocationResultDto result = allocationService.runAllocation();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/manual")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<AllocationDto> manualAllocation(@RequestParam Long studentId, @RequestParam Long roomId) {
        AllocationDto allocation = allocationService.manualAllocate(studentId, roomId);
        return ResponseEntity.ok(allocation);
    }

    @PostMapping("/confirm")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<Void> confirmAllocation() {
        allocationService.confirmAllocation();
        return ResponseEntity.ok().build();
    }
}