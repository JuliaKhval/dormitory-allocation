package com.example.dormitory.dto;

import lombok.Data;

@Data
public class WardenContextDto {
    private boolean dormitoryAssigned;
    private Long dormitoryId;
    private String dormitoryName;
}
