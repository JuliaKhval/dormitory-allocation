package com.example.dormitory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateRoomDto {
    @NotBlank(message = "Корпус обязателен")
    private String building;

    @NotNull(message = "Этаж обязателен")
    @Min(1)
    private Integer floor;

    @NotBlank(message = "Номер комнаты обязателен")
    private String roomNumber;

    @NotNull(message = "Вместимость обязательна")
    @Min(1)
    private Integer capacity;

    @NotBlank(message = "Тип комнаты обязателен")
    @Pattern(regexp = "MALE|FEMALE|MIXED", message = "Тип должен быть MALE, FEMALE или MIXED")
    private String type;

    private String facilities;
}