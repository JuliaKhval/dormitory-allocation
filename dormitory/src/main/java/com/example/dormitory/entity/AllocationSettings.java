package com.example.dormitory.entity;

import com.example.dormitory.enums.DormitoryAssignmentStrategy;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "allocation_settings")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AllocationSettings {
    @Id
    @Column(name = "dormitory_id")
    private Long dormitoryId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "dormitory_id")
    private Dormitory dormitory;

    @Column(name = "floor_order")
    private String floorOrder;

    @Column(name = "faculty_priority_order")
    private String facultyPriorityOrder;

    @Column(name = "benefit_floor_start")
    private Integer benefitFloorStart;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_strategy", nullable = false)
    private DormitoryAssignmentStrategy assignmentStrategy;

    @Column(name = "score_threshold")
    private BigDecimal scoreThreshold;

    @Column(name = "high_score_dormitory_id")
    private Long highScoreDormitoryId;

    @Column(name = "low_score_dormitory_id")
    private Long lowScoreDormitoryId;
}
