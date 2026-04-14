package com.example.dormitory.dto;

import com.example.dormitory.enums.RequestPreferenceStatus;
import lombok.Data;

@Data
public class PreferenceDto {
    private Long preferredUserId;
    private String preferredUserName;
    private RequestPreferenceStatus status;
}