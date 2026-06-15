package com.example.dormitory.dto;

import lombok.Data;

@Data
public class RoomSuggestionDto {
    private Long roomId;
    private String dormitoryName;
    private Integer floor;
    private String roomNumber;
    private int capacity;
    private int occupied;
    private int freeSlots;
    private String type;
}
