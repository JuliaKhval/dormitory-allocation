package com.example.dormitory.mapper;

import com.example.dormitory.dto.RequestDto;
import com.example.dormitory.entity.Request;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {RequestPreferenceMapper.class, AllocationMapper.class})
public interface RequestMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.fullName")
    @Mapping(target = "preferences", source = "preferences")
    @Mapping(target = "allocation", source = "allocation")
    @Mapping(target = "requestStatus", ignore = true)
    RequestDto toDto(Request request);
}