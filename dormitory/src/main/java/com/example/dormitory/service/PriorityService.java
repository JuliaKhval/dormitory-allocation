package com.example.dormitory.service;

import com.example.dormitory.entity.StudentDetail;
import org.springframework.stereotype.Service;

@Service
public class PriorityService {
    public double calculatePriority(StudentDetail student) {
        double priority = student.getAverageScore().doubleValue();
        if (student.getBenefits() != null && !student.getBenefits().isEmpty()) {
            priority += student.getBenefits().stream()
                    .mapToInt(b -> b.getPriorityBonus())
                    .sum();
        }
        if (student.getBenefitBonusAdjustment() != null) {
            priority -= student.getBenefitBonusAdjustment();
        }
        return priority;
    }
}