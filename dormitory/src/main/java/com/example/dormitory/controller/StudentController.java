package com.example.dormitory.controller;

import com.example.dormitory.dto.*;
import com.example.dormitory.service.RequestService;
import com.example.dormitory.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@Tag(name = "Student", description = "Функции студента")
public class StudentController {
    private final StudentService studentService;
    private final RequestService requestService;

    @GetMapping("/me")
    @Operation(summary = "Получить профиль студента")
    public ResponseEntity<StudentProfileDto> getProfile(@RequestParam Long userId) {
        // В будущем userId будет из токена
        return ResponseEntity.ok(studentService.getProfile(userId));
    }

    @PostMapping("/requests")
    @Operation(summary = "Создать заявку")
    public ResponseEntity<RequestDto> createRequest(@RequestParam Long userId,
                                                    @RequestBody CreateRequestDto dto) {
        return ResponseEntity.ok(requestService.createRequest(userId, dto));
    }

    @GetMapping("/requests")
    @Operation(summary = "Получить все заявки студента")
    public ResponseEntity<List<RequestDto>> getMyRequests(@RequestParam Long userId) {
        return ResponseEntity.ok(requestService.getUserRequests(userId));
    }

    @DeleteMapping("/requests/{requestId}")
    @Operation(summary = "Отменить заявку")
    public ResponseEntity<Void> cancelRequest(@PathVariable Long requestId,
                                              @RequestParam Long userId) {
        requestService.cancelRequest(requestId, userId);
        return ResponseEntity.noContent().build();
    }
}