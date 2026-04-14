package com.example.dormitory.service;

import com.example.dormitory.dto.UserProfileDto;
import com.example.dormitory.mapper.UserMapper;
import com.example.dormitory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<UserProfileDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserProfileDto)
                .collect(Collectors.toList());
    }
}