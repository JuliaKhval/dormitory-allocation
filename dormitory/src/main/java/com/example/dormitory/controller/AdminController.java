package com.example.dormitory.controller;

import com.example.dormitory.dto.*;
import com.example.dormitory.entity.*;
import com.example.dormitory.repository.*;
import com.example.dormitory.service.*;
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
    private final FacultyRepository facultyRepository;
    private final AllocationSettingsService allocationSettingsService;

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
        if (dto != null && dto.getDataset() != null && !dto.getDataset().isBlank() && !"random".equalsIgnoreCase(dto.getDataset())) {
            testDataService.loadDatasetFromJson(dto.getDataset());
        } else {
            int count = dto != null ? dto.getStudentCount() : 50;
            testDataService.generateTestData(count, count);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/test-data/datasets")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> listTestDatasets() {
        return ResponseEntity.ok(testDataService.listAvailableDatasets());
    }

    @GetMapping("/faculties")
    @PreAuthorize("hasAnyRole('ADMIN', 'WARDEN')")
    public ResponseEntity<List<Faculty>> getAllFaculties() {
        return ResponseEntity.ok(facultyRepository.findAll());
    }

    @PutMapping("/benefits/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BenefitType> updateBenefit(@PathVariable Long id, @RequestBody BenefitType dto) {
        BenefitType benefit = benefitTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Benefit not found"));
        if (dto.getPriorityBonus() != null) benefit.setPriorityBonus(dto.getPriorityBonus());
        if (dto.getName() != null) benefit.setName(dto.getName());
        if (dto.getDescription() != null) benefit.setDescription(dto.getDescription());
        return ResponseEntity.ok(benefitTypeRepository.save(benefit));
    }

    @GetMapping("/dormitories/{dormitoryId}/allocation-settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AllocationSettingsDto> getAllocationSettings(@PathVariable Long dormitoryId) {
        return ResponseEntity.ok(allocationSettingsService.getForDormitory(dormitoryId));
    }

    @PutMapping("/dormitories/{dormitoryId}/allocation-settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AllocationSettingsDto> saveAllocationSettings(
            @PathVariable Long dormitoryId, @RequestBody AllocationSettingsDto dto) {
        return ResponseEntity.ok(allocationSettingsService.save(dormitoryId, dto));
    }

}