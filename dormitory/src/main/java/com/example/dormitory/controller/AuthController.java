package com.example.dormitory.controller;

import com.example.dormitory.dto.JwtResponse;
import com.example.dormitory.dto.LoginRequest;
import com.example.dormitory.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Аутентификация")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Вход в систему")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}