package com.example.dormitory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RoommateInfo {
    private Long userId;
    private String fullName;
    private Long allocationId;
    private String faculty;
    private String groupName;
    private String country;
    private java.math.BigDecimal averageScore;
}
