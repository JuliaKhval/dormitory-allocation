package com.example.dormitory.service;

import com.example.dormitory.entity.Dormitory;
import com.example.dormitory.repository.DormitoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WardenService {
    private final DormitoryRepository dormitoryRepository;

    public Dormitory getWardenDormitory(Long userId) {
        return dormitoryRepository.findByWardenId(userId).orElse(null);
    }
}