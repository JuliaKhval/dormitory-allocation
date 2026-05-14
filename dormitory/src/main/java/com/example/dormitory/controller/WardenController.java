package com.example.dormitory.controller;

import com.example.dormitory.enums.RequestPreferenceStatus;
import com.example.dormitory.service.RequestPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/warden")
@RequiredArgsConstructor
public class WardenController {

    private final RequestPreferenceService requestPreferenceService;

    @PutMapping("/preferences/{id}")
    @PreAuthorize("hasRole('WARDEN')")    // или hasAnyRole('WARDEN', 'ADMIN')
    public ResponseEntity<Void> updatePreferenceStatus(@PathVariable Long id,
                                                       @RequestParam RequestPreferenceStatus status) {
        requestPreferenceService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }
}