package com.example.dormitory.service;

import com.example.dormitory.dto.StudentProfileDto;
import com.example.dormitory.entity.StudentDetail;
import com.example.dormitory.mapper.StudentProfileMapper;
import com.example.dormitory.repository.StudentDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentDetailRepository studentDetailRepository;
    private final StudentProfileMapper studentProfileMapper;

    public StudentProfileDto getProfile(Long userId) {
        StudentDetail studentDetail = studentDetailRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return studentProfileMapper.toDto(studentDetail);
    }
}