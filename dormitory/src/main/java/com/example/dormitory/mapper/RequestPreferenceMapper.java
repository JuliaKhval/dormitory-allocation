package com.example.dormitory.mapper;

import com.example.dormitory.dto.PreferenceDto;
import com.example.dormitory.entity.RequestPreference;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface RequestPreferenceMapper {
    @Mapping(target = "id", source = "id")
    PreferenceDto toDto(RequestPreference preference);
}