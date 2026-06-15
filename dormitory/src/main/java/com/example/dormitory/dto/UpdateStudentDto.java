package com.example.dormitory.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdateStudentDto {
    private Long groupId;
    private String gender;
    private Long countryId;
    private BigDecimal averageScore;
    private String phoneNumber;
    private List<Long> benefitTypeIds;
    private Integer course;
    private Integer benefitBonusAdjustment;
}