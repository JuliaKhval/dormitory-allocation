package com.example.dormitory.dto;

import lombok.Data;

@Data
public class RoomFilterDto {
    private String building;
    private Integer floor;
    private String type;       // MALE, FEMALE, MIXED
    private String facilities;
}