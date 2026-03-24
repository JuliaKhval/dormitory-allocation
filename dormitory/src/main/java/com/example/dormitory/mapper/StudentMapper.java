package com.example.dormitory.mapper;

import com.example.dormitory.dto.StudentProfileDto;
import com.example.dormitory.entity.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StudentMapper {
    @Mapping(source = "user.email", target = "email")
    StudentProfileDto toDto(Student student);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "averageScore", ignore = true)
    Student toEntity(StudentProfileDto dto);
}