package com.example.dormitory.dto;

import lombok.Data;

@Data
public class UserProfileDto {
    private Long id;
    private String fullName;
    private String email;
}