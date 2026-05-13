package com.example.dormitory.controller;

import com.example.dormitory.dto.StudentProfileDto;
import com.example.dormitory.security.UserDetailsImpl;
import com.example.dormitory.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {
    private final StudentService studentService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentProfileDto> getProfile(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        Long userId = currentUser.getUser().getId();
        return ResponseEntity.ok(studentService.getProfile(userId));
    }
}