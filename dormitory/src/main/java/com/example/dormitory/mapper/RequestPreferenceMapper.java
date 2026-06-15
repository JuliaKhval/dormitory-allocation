package com.example.dormitory.mapper;

import com.example.dormitory.dto.PreferenceDto;
import com.example.dormitory.entity.RequestPreference;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface RequestPreferenceMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "preferredUserId", source = "preferredUser.id")
    @Mapping(target = "preferredUserName", source = "preferredUser.fullName")
    @Mapping(target = "rejectionReason", source = "rejectionReason")
    @Mapping(target = "mutual", ignore = true)
    @Mapping(target = "reversePreferenceExists", ignore = true)
    PreferenceDto toDto(RequestPreference preference);
}