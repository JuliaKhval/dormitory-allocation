package com.example.dormitory.service;

import com.example.dormitory.dto.AllocationDto;
import com.example.dormitory.dto.AllocationResultDto;
import com.example.dormitory.dto.UserProfileDto;
import com.example.dormitory.entity.*;
import com.example.dormitory.enums.AllocationStatus;
import com.example.dormitory.enums.RequestPreferenceStatus;
import com.example.dormitory.mapper.AllocationMapper;
import com.example.dormitory.mapper.UserMapper;
import com.example.dormitory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AllocationService {
    private final RequestRepository requestRepository;
    private final RoomRepository roomRepository;
    private final AllocationRepository allocationRepository;
    private final UserRepository userRepository;
    private final PriorityService priorityService;
    private final CompatibilityService compatibilityService;
    private final AllocationMapper allocationMapper;
    private final UserMapper userMapper;

    // ===================== АВТОМАТИЧЕСКОЕ РАСПРЕДЕЛЕНИЕ =====================
    @Transactional
    public AllocationResultDto runAllocation() {
        List<Request> requests = requestRepository.findAll().stream()
                .filter(r -> r.getAllocation() == null)
                .collect(Collectors.toList());

        // Сортируем по убыванию приоритета, при равенстве – по дате создания
        requests.sort((r1, r2) -> {
            double p1 = priorityService.calculatePriority(r1.getUser().getStudentDetail());
            double p2 = priorityService.calculatePriority(r2.getUser().getStudentDetail());
            if (p1 == p2) {
                return r1.getCreatedAt().compareTo(r2.getCreatedAt());
            }
            return Double.compare(p2, p1);
        });

        List<AllocationDto> allocations = new ArrayList<>();
        List<Long> unallocatedRequestIds = new ArrayList<>();

        for (Request request : requests) {
            StudentDetail student = request.getUser().getStudentDetail();
            if (student == null) continue;

            // Одобренные соседи
            List<User> approvedRoommates = request.getPreferences().stream()
                    .filter(p -> p.getStatus() == RequestPreferenceStatus.APPROVED)
                    .map(RequestPreference::getPreferredUser)
                    .collect(Collectors.toList());

            Room selectedRoom = null;
            for (Room room : roomRepository.findAll()) {
                List<StudentDetail> currentResidents = allocationRepository
                        .findByRoomIdAndStatus(room.getId(), AllocationStatus.ACTIVE)
                        .stream()
                        .map(a -> a.getRequest().getUser().getStudentDetail())
                        .collect(Collectors.toList());

                if (compatibilityService.isCompatible(student, room, currentResidents, approvedRoommates)) {
                    selectedRoom = room;
                    break;
                }
            }

            if (selectedRoom != null) {
                // Заселяем самого студента
                Allocation allocation = Allocation.builder()
                        .request(request)
                        .room(selectedRoom)
                        .status(AllocationStatus.ACTIVE)
                        .allocatedAt(LocalDateTime.now())
                        .build();
                allocation = allocationRepository.save(allocation);
                allocations.add(allocationMapper.toDto(allocation));

                // Заселяем одобренных соседей, если у них ещё нет allocation
                for (User roommate : approvedRoommates) {
                    Request roommateRequest = requestRepository.findByUserId(roommate.getId()).stream()
                            .filter(r -> r.getAllocation() == null)
                            .findFirst()
                            .orElse(null);
                    if (roommateRequest != null) {
                        Allocation roommateAlloc = Allocation.builder()
                                .request(roommateRequest)
                                .room(selectedRoom)
                                .status(AllocationStatus.ACTIVE)
                                .allocatedAt(LocalDateTime.now())
                                .build();
                        allocationRepository.save(roommateAlloc);
                        allocations.add(allocationMapper.toDto(roommateAlloc));
                    }
                }
            } else {
                unallocatedRequestIds.add(request.getId());
            }
        }

        AllocationResultDto result = new AllocationResultDto();
        result.setAllocations(allocations);
        result.setUnallocatedRequestIds(unallocatedRequestIds);
        return result;
    }

    // ===================== РУЧНОЕ РАСПРЕДЕЛЕНИЕ =====================
    @Transactional
    public AllocationDto manualAllocate(Long studentId, Long roomId) {
        // Проверка студента
        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        StudentDetail student = user.getStudentDetail();
        if (student == null) {
            throw new RuntimeException("User is not a student");
        }

        // Проверка комнаты
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Активная заявка студента (без allocation)
        Request request = requestRepository.findByUserId(studentId).stream()
                .filter(r -> r.getAllocation() == null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active request found for this student"));

        // Текущие жильцы комнаты
        List<StudentDetail> currentResidents = allocationRepository
                .findByRoomIdAndStatus(roomId, AllocationStatus.ACTIVE)
                .stream()
                .map(a -> a.getRequest().getUser().getStudentDetail())
                .collect(Collectors.toList());

        // Проверка совместимости (без учёта одобренных соседей)
        if (!compatibilityService.isCompatible(student, room, currentResidents, List.of())) {
            throw new RuntimeException("Incompatible: check gender, country, or capacity");
        }

        // Создаём allocation
        Allocation allocation = Allocation.builder()
                .request(request)
                .room(room)
                .status(AllocationStatus.ACTIVE)
                .allocatedAt(LocalDateTime.now())
                .build();
        allocation = allocationRepository.save(allocation);

        // Возвращаем DTO (соседей заполним в маппере позже, но можно пока пустыми)
        return allocationMapper.toDto(allocation);
    }

    // ===================== ПОЛУЧЕНИЕ РАСПРЕДЕЛЕНИЯ СТУДЕНТА =====================
    public AllocationDto getAllocationByUser(User user) {
        Request request = requestRepository.findByUserId(user.getId()).stream()
                .filter(r -> r.getAllocation() != null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No allocation found"));

        Allocation allocation = request.getAllocation();
        AllocationDto dto = allocationMapper.toDto(allocation);

        // Заполняем соседей (другие студенты в этой же комнате)
        List<UserProfileDto> roommates = allocationRepository
                .findByRoomIdAndStatus(allocation.getRoom().getId(), AllocationStatus.ACTIVE)
                .stream()
                .filter(a -> a.getRequest().getUser().getId() != user.getId())
                .map(a -> userMapper.toUserProfileDto(a.getRequest().getUser()))
                .collect(Collectors.toList());
        dto.setRoommates(roommates);
        return dto;
    }

    // ===================== (Опционально) ПОДТВЕРЖДЕНИЕ – заглушка =====================
    @Transactional
    public void confirmAllocation() {
        // В текущей реализации не требуется, т.к. распределение фиксируется сразу.
        // Можно оставить пустым или удалить.
    }
}