package com.example.dormitory.controller;

import com.example.dormitory.dto.*;
import com.example.dormitory.entity.*;
import com.example.dormitory.repository.*;
import com.example.dormitory.service.StudentService;
import com.example.dormitory.service.TestDataService;
import com.example.dormitory.service.UserService;
import com.example.dormitory.service.WardenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;
    private final CountryRepository countryRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final StudentService studentService;
    private final BenefitTypeRepository benefitTypeRepository;
    private final DormitoryRepository dormitoryRepository;
    private final FacilityRepository facilityRepository;
    private final TestDataService testDataService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserProfileDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileDto> createUser(@Valid @RequestBody CreateUserDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(dto));
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/facilities")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Facility>> getAllFacilities() {
        return ResponseEntity.ok(facilityRepository.findAll());
    }

    @GetMapping("/groups")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StudyGroup>> getAllGroups() {
        return ResponseEntity.ok(studyGroupRepository.findAll());
    }

    @GetMapping("/countries")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Country>> getAllCountries() {
        return ResponseEntity.ok(countryRepository.findAll());
    }

    @GetMapping("/benefits")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BenefitType>> getAllBenefits() {
        return ResponseEntity.ok(benefitTypeRepository.findAll());
    }

    @GetMapping("/students/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentProfileDto> getStudentById(@PathVariable Long userId) {
        return ResponseEntity.ok(studentService.getProfile(userId));
    }

    @PutMapping("/students/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentProfileDto> updateStudent(@PathVariable Long userId,
                                                           @RequestBody UpdateStudentDto dto) {
        return ResponseEntity.ok(studentService.updateStudent(userId, dto));
    }

    @PutMapping("/users/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileDto> updateUserRoles(@PathVariable Long id,
                                                          @RequestBody List<String> roleNames) {
        return ResponseEntity.ok(userService.updateRoles(id, roleNames));
    }
    @GetMapping("/dormitories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Dormitory>> getAllDormitories() {
        return ResponseEntity.ok(dormitoryRepository.findAll());
    }

    @PutMapping("/dormitories/{dormitoryId}/warden")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignWardenToDormitory(@PathVariable Long dormitoryId,
                                                        @RequestParam Long wardenUserId) {
        userService.assignWardenToDormitory(dormitoryId, wardenUserId);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/test-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> generateTestData(@RequestBody(required = false) GenerateTestDataDto dto) {
        int count = dto != null ? dto.getStudentCount() : 50;
        testDataService.generateTestData(count, count);
        return ResponseEntity.ok().build();
    }
}