package com.example.dormitory.dto;

import com.example.dormitory.enums.DormitoryAssignmentStrategy;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AllocationSettingsDto {
    private Long dormitoryId;
    private List<Integer> floorOrder;
    private List<Long> facultyPriorityOrder;
    private Integer benefitFloorStart;
    private DormitoryAssignmentStrategy assignmentStrategy;
    private BigDecimal scoreThreshold;
    private Long highScoreDormitoryId;
    private Long lowScoreDormitoryId;
}
