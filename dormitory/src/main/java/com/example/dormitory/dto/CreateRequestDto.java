package com.example.dormitory.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateRequestDto {
    private List<Long> preferredRoommates;
}