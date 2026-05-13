package com.example.dormitory.controller;

import com.example.dormitory.dto.SystemStatsDto;
import com.example.dormitory.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatController {
    private final AdminService adminService;

    @GetMapping
    @PreAuthorize("hasAnyRole('WARDEN', 'ADMIN')")
    public ResponseEntity<SystemStatsDto> getStats() {
        return ResponseEntity.ok(adminService.getSystemStats());
    }
}