package com.example.dormitory.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateUserDto {
    private String fullName;
    private String email;
    private String password;
    private List<String> roles;        // например, ["STUDENT", "WARDEN"]
    // поля для студента (если роль STUDENT)
    private Long groupId;
    private Long countryId;
    private String gender;
    private BigDecimal averageScore;
    private String phoneNumber;
    private List<Long> benefitTypeIds;
}