package com.example.dormitory.dto;

import lombok.Data;

@Data
public class RoomDto {
    private Long id;
    private String building;
    private Integer floor;
    private String roomNumber;
    private Integer capacity;
    private Integer occupied;
    private String type;      // MALE, FEMALE, MIXED
    private String facilities;
}