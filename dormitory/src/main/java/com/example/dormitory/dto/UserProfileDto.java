package com.example.dormitory.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserProfileDto {
    private Long id;
    private String fullName;
    private String email;
    private List<String> roles;
}