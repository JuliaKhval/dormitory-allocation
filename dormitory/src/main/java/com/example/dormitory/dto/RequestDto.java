package com.example.dormitory.dto;

import com.example.dormitory.enums.RequestPreferenceStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RequestDto {
    private Long id;
    private Long userId;
    private LocalDateTime createdAt;
    private Integer year;
    private List<PreferenceDto> preferences;
    private AllocationDto allocation;
}
