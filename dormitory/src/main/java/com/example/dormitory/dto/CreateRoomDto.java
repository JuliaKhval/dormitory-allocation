package com.example.dormitory.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateRoomDto {
    private Long dormitoryId;
    private Integer floor;
    private String roomNumber;
    private Integer capacity;
    private String type;
    private List<String> facilities;
}