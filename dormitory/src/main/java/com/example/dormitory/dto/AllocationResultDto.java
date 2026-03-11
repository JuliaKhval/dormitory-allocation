package com.example.dormitory.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AllocationResultDto {
    private List<AllocationDto> allocations;        // успешно распределённые
    private List<Long> unallocatedStudentIds;       // нераспределённые студенты
    private Map<String, String> errors;             // причины отказов
}