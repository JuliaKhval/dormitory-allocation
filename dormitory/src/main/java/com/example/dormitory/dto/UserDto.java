package com.example.dormitory.dto;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String email;
    private String role;
    private StudentProfileDto student;   // если роль STUDENT
    private WardenDto warden;             // если роль WARDEN
    private AdminProfileDto admin;         // если роль ADMIN
}