package com.example.dormitory.mapper;

import com.example.dormitory.dto.CreateRoomDto;
import com.example.dormitory.dto.RoomDto;
import com.example.dormitory.entity.Facility;
import com.example.dormitory.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    @Mapping(target = "dormitoryName", source = "room.dormitory.name")
    @Mapping(target = "facilities", source = "room.facilities", qualifiedByName = "facilityNames")
    @Mapping(target = "occupied", source = "occupied")
    @Mapping(target = "roommates", ignore = true)
    RoomDto toDto(Room room, Integer occupied);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dormitory", ignore = true)
    @Mapping(target = "facilities", ignore = true)
    Room toEntity(CreateRoomDto dto);

    @Named("facilityNames")
    default List<String> facilityNames(List<Facility> facilities) {
        if (facilities == null) return List.of();
        return facilities.stream()
                .map(Facility::getName)
                .collect(Collectors.toList());
    }
}