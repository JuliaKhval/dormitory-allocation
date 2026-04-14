package com.example.dormitory.service;

import com.example.dormitory.entity.*;
import com.example.dormitory.enums.AllocationStatus;
import com.example.dormitory.enums.RequestPreferenceStatus;
import com.example.dormitory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AllocationService {
    private final RequestRepository requestRepository;
    private final RoomRepository roomRepository;
    private final AllocationRepository allocationRepository;
    private final PriorityService priorityService;
    private final CompatibilityService compatibilityService;

    @Transactional
    public void runAllocation() {
        // Получаем все заявки без allocation
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

        for (Request request : requests) {
            StudentDetail student = request.getUser().getStudentDetail();
            if (student == null) continue;

            // Получаем одобренных соседей
            List<User> approvedRoommates = request.getPreferences().stream()
                    .filter(p -> p.getStatus() == RequestPreferenceStatus.APPROVED)
                    .map(RequestPreference::getPreferredUser)
                    .collect(Collectors.toList());

            // Поиск подходящей комнаты
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
                // Создаём allocation для заявителя
                Allocation allocation = Allocation.builder()
                        .request(request)
                        .room(selectedRoom)
                        .status(AllocationStatus.ACTIVE)
                        .allocatedAt(LocalDateTime.now())
                        .build();
                allocationRepository.save(allocation);

                // Для каждого одобренного соседа, если у него ещё нет allocation, создаём
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
                    }
                }
            }
        }
    }
}