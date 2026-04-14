package com.example.dormitory.service;

import com.example.dormitory.dto.CreateRoomDto;
import com.example.dormitory.dto.RoomDto;
import com.example.dormitory.entity.Facility;
import com.example.dormitory.entity.Room;
import com.example.dormitory.enums.AllocationStatus;
import com.example.dormitory.mapper.RoomMapper;
import com.example.dormitory.repository.AllocationRepository;
import com.example.dormitory.repository.FacilityRepository;
import com.example.dormitory.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final AllocationRepository allocationRepository;
    private final FacilityRepository facilityRepository;
    private final RoomMapper roomMapper;

    public List<RoomDto> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(room -> {
                    int occupied = allocationRepository.findByRoomIdAndStatus(room.getId(), AllocationStatus.ACTIVE).size();
                    return roomMapper.toDto(room);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomDto createRoom(CreateRoomDto dto) {
        Room room = roomMapper.toEntity(dto);
        if (dto.getFacilities() != null && !dto.getFacilities().isEmpty()) {
            List<Facility> facilities = dto.getFacilities().stream()
                    .map(name -> facilityRepository.findByName(name)
                            .orElseThrow(() -> new RuntimeException("Facility not found: " + name)))
                    .collect(Collectors.toList());
            room.setFacilities(facilities);
        }
        room = roomRepository.save(room);
        return roomMapper.toDto(room);
    }

    @Transactional
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        if (!allocationRepository.findByRoomIdAndStatus(id, AllocationStatus.ACTIVE).isEmpty()) {
            throw new RuntimeException("Cannot delete room with active allocations");
        }
        roomRepository.delete(room);
    }
}