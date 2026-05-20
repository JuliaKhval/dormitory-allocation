package com.example.dormitory.mapper;

import com.example.dormitory.dto.AllocationDto;
import com.example.dormitory.entity.Allocation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AllocationMapper {
    @Mapping(target = "roomId", source = "room.id")
    @Mapping(target = "dormitoryName", source = "room.dormitory.name")
    @Mapping(target = "floor", source = "room.floor")
    @Mapping(target = "roomNumber", source = "room.roomNumber")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "roommates", ignore = true)
    AllocationDto toDto(Allocation allocation);
}