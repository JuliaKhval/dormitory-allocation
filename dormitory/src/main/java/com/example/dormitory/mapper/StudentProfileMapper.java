package com.example.dormitory.mapper;

import com.example.dormitory.dto.StudentProfileDto;
import com.example.dormitory.entity.BenefitType;
import com.example.dormitory.entity.StudentDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface StudentProfileMapper {

    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "email", source = "user.credential.email")
    @Mapping(target = "faculty", source = "group.faculty.name")
    @Mapping(target = "groupName", source = "group.groupName")
    @Mapping(target = "course", source = "group.course")
    @Mapping(target = "gender", source = "gender")
    @Mapping(target = "country", source = "country.name")
    @Mapping(target = "benefits", source = "benefits", qualifiedByName = "benefitNames")
    StudentProfileDto toDto(StudentDetail studentDetail);

    @Named("benefitNames")
    default List<String> benefitNames(List<BenefitType> benefits) {
        if (benefits == null) return List.of();
        return benefits.stream()
                .map(BenefitType::getName)
                .collect(Collectors.toList());
    }
}