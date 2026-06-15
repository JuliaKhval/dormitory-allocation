package com.example.dormitory.service;

import com.example.dormitory.dto.AllocationSettingsDto;
import com.example.dormitory.entity.AllocationSettings;
import com.example.dormitory.entity.Dormitory;
import com.example.dormitory.enums.DormitoryAssignmentStrategy;
import com.example.dormitory.repository.AllocationSettingsRepository;
import com.example.dormitory.repository.DormitoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AllocationSettingsService {

    private final AllocationSettingsRepository settingsRepository;
    private final DormitoryRepository dormitoryRepository;

    public AllocationSettingsDto getForDormitory(Long dormitoryId) {
        AllocationSettings settings = settingsRepository.findById(dormitoryId)
                .orElseGet(() -> createDefault(dormitoryId));
        return toDto(settings);
    }

    @Transactional
    public AllocationSettingsDto save(Long dormitoryId, AllocationSettingsDto dto) {
        Dormitory dormitory = dormitoryRepository.findById(dormitoryId)
                .orElseThrow(() -> new RuntimeException("Dormitory not found"));
        AllocationSettings settings = settingsRepository.findById(dormitoryId)
                .orElse(AllocationSettings.builder().dormitory(dormitory).build());
        settings.setDormitory(dormitory);
        settings.setFloorOrder(joinInts(dto.getFloorOrder()));
        settings.setFacultyPriorityOrder(joinLongs(dto.getFacultyPriorityOrder()));
        settings.setBenefitFloorStart(dto.getBenefitFloorStart());
        settings.setAssignmentStrategy(dto.getAssignmentStrategy() != null
                ? dto.getAssignmentStrategy() : DormitoryAssignmentStrategy.SCORE_THRESHOLD);
        settings.setScoreThreshold(dto.getScoreThreshold());
        settings.setHighScoreDormitoryId(dto.getHighScoreDormitoryId());
        settings.setLowScoreDormitoryId(dto.getLowScoreDormitoryId());
        settingsRepository.save(settings);
        return toDto(settings);
    }

    private AllocationSettings createDefault(Long dormitoryId) {
        Dormitory dormitory = dormitoryRepository.findById(dormitoryId)
                .orElseThrow(() -> new RuntimeException("Dormitory not found"));
        AllocationSettings s = AllocationSettings.builder()
                .dormitory(dormitory)
                .assignmentStrategy(DormitoryAssignmentStrategy.SCORE_THRESHOLD)
                .build();
        return settingsRepository.save(s);
    }

    private AllocationSettingsDto toDto(AllocationSettings s) {
        AllocationSettingsDto dto = new AllocationSettingsDto();
        dto.setDormitoryId(s.getDormitoryId());
        dto.setFloorOrder(parseInts(s.getFloorOrder()));
        dto.setFacultyPriorityOrder(parseLongs(s.getFacultyPriorityOrder()));
        dto.setBenefitFloorStart(s.getBenefitFloorStart());
        dto.setAssignmentStrategy(s.getAssignmentStrategy());
        dto.setScoreThreshold(s.getScoreThreshold());
        dto.setHighScoreDormitoryId(s.getHighScoreDormitoryId());
        dto.setLowScoreDormitoryId(s.getLowScoreDormitoryId());
        return dto;
    }

    private static String joinInts(List<Integer> list) {
        if (list == null || list.isEmpty()) return null;
        return list.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private static String joinLongs(List<Long> list) {
        if (list == null || list.isEmpty()) return null;
        return list.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private static List<Integer> parseInts(String s) {
        if (s == null || s.isBlank()) return List.of();
        return Arrays.stream(s.split(",")).map(String::trim).map(Integer::parseInt).collect(Collectors.toList());
    }

    private static List<Long> parseLongs(String s) {
        if (s == null || s.isBlank()) return List.of();
        return Arrays.stream(s.split(",")).map(String::trim).map(Long::parseLong).collect(Collectors.toList());
    }
}
