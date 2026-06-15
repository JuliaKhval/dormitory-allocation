package com.example.dormitory.dto;

import lombok.Data;

@Data
public class GenerateTestDataDto {
    private int studentCount = 50;

    private String dataset;
}