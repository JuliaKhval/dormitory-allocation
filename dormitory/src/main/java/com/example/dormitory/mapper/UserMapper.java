package com.example.dormitory.mapper;

import com.example.dormitory.dto.UserProfileDto;
import com.example.dormitory.entity.Role;
import com.example.dormitory.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "email", source = "credential.email")
    @Mapping(target = "roles", source = "roles", qualifiedByName = "roleNames")
    UserProfileDto toUserProfileDto(User user);

    @Named("roleNames")
    default List<String> roleNames(List<Role> roles) {
        if (roles == null) return List.of();
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());
    }
}