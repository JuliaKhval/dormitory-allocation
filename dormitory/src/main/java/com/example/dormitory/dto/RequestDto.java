package com.example.dormitory.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RequestDto {
    private Long id;
    private Long userId;
    private String userName;
    private LocalDateTime createdAt;
    private Integer year;
    private List<PreferenceDto> preferences;
    private AllocationDto allocation;
}