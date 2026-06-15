package com.example.dormitory.service;

import com.example.dormitory.entity.AllocationSettings;
import com.example.dormitory.entity.Dormitory;
import com.example.dormitory.entity.StudentDetail;
import com.example.dormitory.enums.DormitoryAssignmentStrategy;
import com.example.dormitory.repository.AllocationSettingsRepository;
import com.example.dormitory.repository.DormitoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class DormitoryAssignmentService {

    private final AllocationSettingsRepository settingsRepository;
    private final DormitoryRepository dormitoryRepository;
    private final AtomicInteger roundRobinCounter = new AtomicInteger(0);

    /**
     * Определяет, в какое общежитие направляется студент (по баллам или round-robin).
     */
    public Long resolveTargetDormitory(StudentDetail student) {
        return resolveByGlobalStrategy(student);
    }

    private Long resolveByGlobalStrategy(StudentDetail student) {
        List<AllocationSettings> allSettings = settingsRepository.findAll();
        if (allSettings.isEmpty()) {
            return dormitoryRepository.findAll().stream()
                    .findFirst()
                    .map(Dormitory::getId)
                    .orElse(null);
        }
        AllocationSettings settings = allSettings.get(0);
        if (settings.getAssignmentStrategy() == DormitoryAssignmentStrategy.SCORE_THRESHOLD
                && settings.getScoreThreshold() != null) {
            BigDecimal score = student.getAverageScore();
            if (score.compareTo(settings.getScoreThreshold()) >= 0) {
                return settings.getHighScoreDormitoryId();
            }
            return settings.getLowScoreDormitoryId();
        }
        if (settings.getAssignmentStrategy() == DormitoryAssignmentStrategy.ROUND_ROBIN) {
            List<Dormitory> dorms = dormitoryRepository.findAll();
            if (dorms.isEmpty()) return null;
            int idx = Math.floorMod(roundRobinCounter.getAndIncrement(), dorms.size());
            return dorms.get(idx).getId();
        }
        return settings.getDormitoryId();
    }
}
