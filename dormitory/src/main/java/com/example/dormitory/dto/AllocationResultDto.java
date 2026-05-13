package com.example.dormitory.dto;

import lombok.Data;
import java.util.List;

@Data
public class AllocationResultDto {
    private List<AllocationDto> allocations;
    private List<Long> unallocatedRequestIds;
}