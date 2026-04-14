package com.example.dormitory.service;

import com.example.dormitory.dto.SystemStatsDto;
import com.example.dormitory.enums.AllocationStatus;
import com.example.dormitory.enums.RoleName;
import com.example.dormitory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final AllocationRepository allocationRepository;
    private final RequestRepository requestRepository;

    public SystemStatsDto getSystemStats() {
        SystemStatsDto stats = new SystemStatsDto();
        stats.setTotalUsers(userRepository.count());
        stats.setTotalStudents(userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName() == RoleName.STUDENT))
                .count());
        stats.setTotalWardens(userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName() == RoleName.WARDEN))
                .count());
        stats.setTotalAdmins(userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ADMIN))
                .count());
        stats.setTotalRooms(roomRepository.count());
        stats.setOccupiedRooms(roomRepository.findAll().stream()
                .filter(r -> !allocationRepository.findByRoomIdAndStatus(r.getId(), AllocationStatus.ACTIVE).isEmpty())
                .count());
        stats.setFreeRooms(stats.getTotalRooms() - stats.getOccupiedRooms());
        stats.setActiveRequests(requestRepository.findAll().stream()
                .filter(r -> r.getAllocation() == null)
                .count());
        stats.setAllocatedRequests(requestRepository.findAll().stream()
                .filter(r -> r.getAllocation() != null)
                .count());
        return stats;
    }
}