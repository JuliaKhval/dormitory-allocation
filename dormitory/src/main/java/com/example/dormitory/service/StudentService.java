package com.example.dormitory.service;

import com.example.dormitory.dto.StudentProfileDto;
import com.example.dormitory.dto.UpdateStudentDto;
import com.example.dormitory.entity.*;
import com.example.dormitory.enums.Gender;
import com.example.dormitory.mapper.StudentProfileMapper;
import com.example.dormitory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentDetailRepository studentDetailRepository;
    private final StudentProfileMapper studentProfileMapper;
    private final RequestRepository requestRepository;
    private final PriorityService priorityService;
    private final StudyGroupRepository studyGroupRepository;
    private final CountryRepository countryRepository;
    private final BenefitTypeRepository benefitTypeRepository;

    public StudentProfileDto getProfile(Long userId) {
        StudentDetail studentDetail = studentDetailRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return studentProfileMapper.toDto(studentDetail);
    }

    // Список студентов для заселения (сортировка по приоритету)
    public List<StudentProfileDto> getStudentsToAllocate() {
        List<Request> activeRequests = requestRepository.findAll().stream()
                .filter(r -> r.getAllocation() == null)
                .collect(Collectors.toList());
        activeRequests.sort((r1, r2) -> Double.compare(
                priorityService.calculatePriority(r2.getUser().getStudentDetail()),
                priorityService.calculatePriority(r1.getUser().getStudentDetail())
        ));
        return activeRequests.stream()
                .map(r -> studentProfileMapper.toDto(r.getUser().getStudentDetail()))
                .collect(Collectors.toList());
    }

    // Обновление данных студента (администратор)
    @Transactional
    public StudentProfileDto updateStudent(Long userId, UpdateStudentDto dto) {
        StudentDetail sd = studentDetailRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        if (dto.getGroupId() != null) {
            StudyGroup group = studyGroupRepository.findById(dto.getGroupId())
                    .orElseThrow(() -> new RuntimeException("Group not found"));
            sd.setGroup(group);
        }
        if (dto.getGender() != null) sd.setGender(Gender.valueOf(dto.getGender()));
        if (dto.getCountryId() != null) {
            Country country = countryRepository.findById(dto.getCountryId())
                    .orElseThrow(() -> new RuntimeException("Country not found"));
            sd.setCountry(country);
        }
        if (dto.getAverageScore() != null) sd.setAverageScore(dto.getAverageScore());
        if (dto.getPhoneNumber() != null) sd.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getBenefitTypeIds() != null) {
            List<BenefitType> benefits = benefitTypeRepository.findAllById(dto.getBenefitTypeIds());
            sd.setBenefits(benefits);
        }
        studentDetailRepository.save(sd);
        return studentProfileMapper.toDto(sd);
    }
}