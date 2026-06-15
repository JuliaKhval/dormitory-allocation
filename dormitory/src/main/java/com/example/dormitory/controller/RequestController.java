package com.example.dormitory.controller;

import com.example.dormitory.dto.CreateRequestDto;
import com.example.dormitory.dto.RequestDto;
import com.example.dormitory.security.UserDetailsImpl;
import com.example.dormitory.service.RequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/requests")
@RequiredArgsConstructor
public class RequestController {
    private final RequestService requestService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<RequestDto> createRequest(@AuthenticationPrincipal UserDetailsImpl currentUser,
                                                    @Valid @RequestBody CreateRequestDto dto) {
        Long userId = currentUser.getUser().getId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(requestService.createRequest(userId, dto));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<RequestDto>> getMyRequests(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        Long userId = currentUser.getUser().getId();
        return ResponseEntity.ok(requestService.getUserRequests(userId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> cancelRequest(@AuthenticationPrincipal UserDetailsImpl currentUser,
                                              @PathVariable Long id) {
        Long userId = currentUser.getUser().getId();
        requestService.cancelRequest(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<List<RequestDto>> getAllRequests(
            @RequestParam(required = false) String sortBy,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        Long dormitoryId = currentUser.getDormitoryId();
        return ResponseEntity.ok(requestService.getAllRequests(sortBy));
    }
}