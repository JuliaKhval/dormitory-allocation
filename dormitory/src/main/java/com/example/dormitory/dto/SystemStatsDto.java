package com.example.dormitory.dto;

import lombok.Data;

@Data
public class SystemStatsDto {
    private Long totalUsers;
    private Long totalStudents;
    private Long totalWardens;
    private Long totalAdmins;
    private Long totalRooms;
    private Long occupiedRooms;
    private Long freeRooms;
    private Long activeRequests;
    private Long allocatedRequests;
}