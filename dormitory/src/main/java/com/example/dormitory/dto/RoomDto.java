package com.example.dormitory.dto;

import lombok.Data;
import java.util.List;

@Data
public class RoomDto {
    private Long id;
    private String dormitoryName;
    private Integer floor;
    private String roomNumber;
    private Integer capacity;
    private String type;
    private List<String> facilities;
    private Integer occupied;
    private List<RoommateInfo> roommates;// текущие жильцы
}