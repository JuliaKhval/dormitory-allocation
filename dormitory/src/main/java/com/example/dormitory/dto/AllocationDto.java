package com.example.dormitory.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AllocationDto {
    private Long id;
    private Long roomId;
    private String building;
    private Integer floor;
    private String roomNumber;
    private LocalDateTime allocatedAt;
    private List<StudentProfileDto> roommates;
}