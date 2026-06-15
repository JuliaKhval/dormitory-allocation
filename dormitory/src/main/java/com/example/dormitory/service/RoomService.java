package com.example.dormitory.service;

import com.example.dormitory.dto.CreateRoomDto;
import com.example.dormitory.dto.RoomDto;
import com.example.dormitory.dto.RoommateInfo;
import com.example.dormitory.dto.UserProfileDto;
import com.example.dormitory.entity.Allocation;
import com.example.dormitory.entity.Dormitory;
import com.example.dormitory.entity.Facility;
import com.example.dormitory.entity.Room;
import com.example.dormitory.enums.AllocationStatus;
import com.example.dormitory.enums.RoomType;
import com.example.dormitory.mapper.RoomMapper;
import com.example.dormitory.mapper.UserMapper;
import com.example.dormitory.repository.AllocationRepository;
import com.example.dormitory.repository.DormitoryRepository;
import com.example.dormitory.repository.FacilityRepository;
import com.example.dormitory.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final AllocationRepository allocationRepository;
    private final FacilityRepository facilityRepository;
    private final DormitoryRepository dormitoryRepository;
    private final RoomMapper roomMapper;
    private  final UserMapper userMapper;

    private String extractBlockCode(String roomNumber) {

        if (roomNumber != null && roomNumber.matches(".*[A-Za-zА-Яа-я]$")) {
            return roomNumber.substring(0, roomNumber.length() - 1);
        }
        return roomNumber;
    }

    public List<RoomDto> getAllRooms(Integer floor, String type, Long dormitoryId) {
        return roomRepository.findAll().stream()
                .filter(room -> dormitoryId == null || room.getDormitory().getId().equals(dormitoryId))
                .filter(room -> floor == null || room.getFloor().equals(floor))
                .filter(room -> type == null || room.getType().name().equalsIgnoreCase(type))
                .sorted(Comparator.comparing(Room::getFloor).thenComparing(Room::getRoomNumber))
                .map(room -> {
                    int occupied = allocationRepository.findByRoomIdAndStatus(room.getId(), AllocationStatus.ACTIVE).size();
                    return roomMapper.toDto(room, occupied);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomDto createRoom(CreateRoomDto dto) {
        Dormitory dormitory = dormitoryRepository.findById(dto.getDormitoryId())
                .orElseThrow(() -> new RuntimeException("Dormitory not found"));
        Room room = roomMapper.toEntity(dto);
        room.setDormitory(dormitory);
        room.setType(RoomType.valueOf(dto.getType()));
        if (dto.getFacilities() != null && !dto.getFacilities().isEmpty()) {
            List<Facility> facilities = dto.getFacilities().stream()
                    .map(name -> facilityRepository.findByName(name)
                            .orElseThrow(() -> new RuntimeException("Facility not found: " + name)))
                    .collect(Collectors.toList());
            room.setFacilities(facilities);
        }
        room = roomRepository.save(room);
        return roomMapper.toDto(room, 0);
    }

    @Transactional
    public RoomDto updateRoom(Long id, CreateRoomDto dto) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        // Исправлено: находим Dormitory по id
        if (dto.getDormitoryId() != null) {
            Dormitory dormitory = dormitoryRepository.findById(dto.getDormitoryId())
                    .orElseThrow(() -> new RuntimeException("Dormitory not found"));
            room.setDormitory(dormitory);
        }
        room.setFloor(dto.getFloor());
        room.setRoomNumber(dto.getRoomNumber());
        room.setCapacity(dto.getCapacity());
        room.setType(RoomType.valueOf(dto.getType()));
        if (dto.getFacilities() != null) {
            List<Facility> facilities = dto.getFacilities().stream()
                    .map(name -> facilityRepository.findByName(name)
                            .orElseThrow(() -> new RuntimeException("Facility not found: " + name)))
                    .collect(Collectors.toList());
            room.setFacilities(facilities);
        }
        int occupied = allocationRepository.findByRoomIdAndStatus(room.getId(), AllocationStatus.ACTIVE).size();
        return roomMapper.toDto(roomRepository.save(room), occupied);
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

    public RoomDto getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        int occupied = allocationRepository.findByRoomIdAndStatus(id, AllocationStatus.ACTIVE).size();

        List<Allocation> allocations = allocationRepository.findByRoomIdAndStatus(id, AllocationStatus.ACTIVE);
        List<RoommateInfo> roommates = allocations.stream()
                .map(a -> {
                    var sd = a.getRequest().getUser().getStudentDetail();
                    RoommateInfo info = new RoommateInfo();
                    info.setUserId(a.getRequest().getUser().getId());
                    info.setFullName(a.getRequest().getUser().getFullName());
                    info.setAllocationId(a.getId());
                    if (sd != null) {
                        info.setFaculty(sd.getGroup().getFaculty().getName());
                        info.setGroupName(sd.getGroup().getGroupName());
                        info.setCountry(sd.getCountry().getName());
                        info.setAverageScore(sd.getAverageScore());
                    }
                    return info;
                })
                .collect(Collectors.toList());

        RoomDto dto = roomMapper.toDto(room, occupied);
        dto.setRoommates(roommates);
        return dto;
    }
}
