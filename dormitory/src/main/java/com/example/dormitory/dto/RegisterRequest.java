package com.example.dormitory.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
    private String password;

    @NotBlank(message = "Имя обязательно")
    private String firstName;

    @NotBlank(message = "Фамилия обязательна")
    private String lastName;

    private String middleName;

    @NotBlank(message = "Факультет обязателен")
    private String faculty;

    @NotNull(message = "Курс обязателен")
    @Min(1)
    @Max(5)
    private Integer course;

    @NotBlank(message = "Группа обязательна")
    private String groupName;

    @NotBlank(message = "Пол обязателен")
    @Pattern(regexp = "MALE|FEMALE", message = "Пол должен быть MALE или FEMALE")
    private String gender;

    @NotBlank(message = "Страна обязательна")
    private String country;

    @NotNull(message = "Средний балл обязателен")
    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "100.0", inclusive = true)
    private Double averageScore;

    private Boolean benefits = false;
}