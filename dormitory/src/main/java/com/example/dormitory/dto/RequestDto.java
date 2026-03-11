package com.example.dormitory.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RequestDto {
    private Long id;
    private Long studentId;
    private String studentName;
    private String status;       // PENDING, APPROVED, REJECTED, ALLOCATED
    private LocalDateTime createdAt;
    private List<StudentProfileDto> preferredRoommates;
    private AllocationDto allocation;
}