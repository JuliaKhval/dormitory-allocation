package com.example.dormitory.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StudentProfileDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String middleName;
    private String faculty;
    private Integer course;
    private String groupName;
    private String gender;
    private String country;
    private BigDecimal averageScore; //  (обновляется администратором)
    private Boolean benefits;
}