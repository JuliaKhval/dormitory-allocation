package com.example.dormitory.controller;

import com.example.dormitory.dto.JwtResponse;
import com.example.dormitory.dto.LoginRequest;
import com.example.dormitory.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        JwtResponse response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }
}