package com.example.dormitory.dto;

import com.example.dormitory.enums.Gender;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class StudentProfileDto {
    private Long id;
    private String fullName;
    private String email;
    private String faculty;
    private String groupName;
    private Integer course;
    private Gender gender;
    private String country;
    private BigDecimal averageScore;
    private String phoneNumber;
    private List<String> benefits;
}