package com.example.dormitory.controller;

import com.example.dormitory.dto.StudentProfileDto;
import com.example.dormitory.dto.UserProfileDto;
import com.example.dormitory.entity.StudentDetail;
import com.example.dormitory.entity.User;
import com.example.dormitory.enums.RoleName;
import com.example.dormitory.mapper.StudentProfileMapper;
import com.example.dormitory.mapper.UserMapper;
import com.example.dormitory.repository.StudentDetailRepository;
import com.example.dormitory.repository.UserRepository;
import com.example.dormitory.security.UserDetailsImpl;
import com.example.dormitory.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {
    private final StudentService studentService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final StudentDetailRepository    studentDetailRepository;
    private final StudentProfileMapper studentProfileMapper;

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentProfileDto> getProfile(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        Long userId = currentUser.getUser().getId();
        return ResponseEntity.ok(studentService.getProfile(userId));
    }
    @GetMapping("/search")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<UserProfileDto>> searchStudents(@RequestParam String query) {
        List<User> users = userRepository.findByFullNameContainingIgnoreCase(query);
        List<UserProfileDto> result = users.stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName() == RoleName.STUDENT))
                .map(userMapper::toUserProfileDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('STUDENT', 'WARDEN', 'ADMIN')")
    public ResponseEntity<List<StudentProfileDto>> getStudentsList() {
        List<StudentDetail> students = studentDetailRepository.findAll(); // или с фильтрацией, если нужно
        List<StudentProfileDto> dtos = students.stream()
                .map(studentProfileMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}