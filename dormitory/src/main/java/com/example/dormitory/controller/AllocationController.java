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
        // TODO: Реализовать метод в AllocationService для получения распределения по студенту
        throw new UnsupportedOperationException("Not implemented yet. Please add method to AllocationService.");
    }

    @PostMapping("/auto")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<AllocationResultDto> runAutoAllocation() {
        allocationService.runAllocation(); // метод не возвращает результат, но запускает распределение
        // Возвращаем пустой результат (или можно вернуть какую-то заглушку)
        return ResponseEntity.ok(new AllocationResultDto());
    }

    @PostMapping("/manual")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<AllocationDto> manualAllocation(@RequestParam Long studentId, @RequestParam Long roomId) {
        // В AllocationService нет ручного распределения.
        throw new UnsupportedOperationException("Manual allocation not implemented in AllocationService yet.");
    }

    @PostMapping("/confirm")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<Void> confirmAllocation() {
        // В AllocationService нет confirmAllocation, заглушка
        throw new UnsupportedOperationException("Confirm allocation not implemented yet.");
    }
}