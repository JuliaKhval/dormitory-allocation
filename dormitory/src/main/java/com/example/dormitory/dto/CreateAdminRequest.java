package com.example.dormitory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAdminRequest {
    @NotNull(message = "ID пользователя обязателен")
    private Long userId;

}