package com.example.dormitory.dto;

import com.example.dormitory.enums.RequestPreferenceStatus;
import lombok.Data;

@Data
public class UpdatePreferenceDto {
    private RequestPreferenceStatus status;
    private String rejectionReason;
}
