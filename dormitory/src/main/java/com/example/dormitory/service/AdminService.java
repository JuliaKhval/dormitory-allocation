package com.example.dormitory.service;

import com.example.dormitory.dto.SystemStatsDto;
import com.example.dormitory.entity.Room;
import com.example.dormitory.enums.AllocationStatus;
import com.example.dormitory.enums.RoleName;
import com.example.dormitory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final AllocationRepository allocationRepository;
    private final RequestRepository requestRepository;


    public SystemStatsDto getSystemStats(Long dormitoryId) {
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

        // Фильтруем комнаты по dormitoryId (если передан)
        List<Room> rooms;
        if (dormitoryId != null) {
            rooms = roomRepository.findByDormitoryId(dormitoryId);
        } else {
            rooms = roomRepository.findAll();
        }
        stats.setTotalRooms((long) rooms.size());
        long occupied = rooms.stream()
                .filter(r -> !allocationRepository.findByRoomIdAndStatus(r.getId(), AllocationStatus.ACTIVE).isEmpty())
                .count();
        stats.setOccupiedRooms(occupied);
        stats.setFreeRooms(stats.getTotalRooms() - occupied);


        stats.setActiveRequests(requestRepository.findAll().stream()
                .filter(r -> r.getAllocation() == null)
                .count());
        stats.setAllocatedRequests(requestRepository.findAll().stream()
                .filter(r -> r.getAllocation() != null)
                .count());
        return stats;
    }
}