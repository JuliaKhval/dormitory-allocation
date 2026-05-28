package com.example.dormitory.service;

import com.example.dormitory.dto.AllocationDto;
import com.example.dormitory.dto.AllocationResultDto;
import com.example.dormitory.dto.UserProfileDto;
import com.example.dormitory.entity.*;
import com.example.dormitory.enums.AllocationStatus;
import com.example.dormitory.enums.Gender;
import com.example.dormitory.enums.RequestPreferenceStatus;
import com.example.dormitory.enums.RoomType;
import com.example.dormitory.mapper.AllocationMapper;
import com.example.dormitory.mapper.UserMapper;
import com.example.dormitory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AllocationService {
    private final RequestRepository requestRepository;
    private final RoomRepository roomRepository;
    private final AllocationRepository allocationRepository;
    private final PriorityService priorityService;
    private final CompatibilityService compatibilityService;
    private final AllocationMapper allocationMapper;
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    // ========================= ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ =========================

    /**
     * Извлекает код блока из номера комнаты.
     * Пример: "101А" -> "101", "202" -> "202", "15Б" -> "15"
     */
    private String extractBlockCode(String roomNumber) {
        if (roomNumber == null || roomNumber.isBlank()) return "";
        if (roomNumber.matches(".*[A-Za-zА-Яа-я]$")) {
            return roomNumber.substring(0, roomNumber.length() - 1);
        }
        return roomNumber;
    }

    /**
     * Синхронизирует тип всех комнат блока, если в текущую комнату заселяется первый жилец.
     * Все комнаты блока, имеющие тип UNDEFINED, получают тип, соответствующий полу заселяемого студента.
     */
    private void syncBlockIfFirstOccupant(Room room, Gender gender) {
        boolean isOccupied = !allocationRepository.findByRoomIdAndStatus(room.getId(), AllocationStatus.ACTIVE).isEmpty();
        if (isOccupied) {
            // В комнате уже кто-то есть – блок уже синхронизирован ранее
            return;
        }
        // Первый жилец в этой комнате – определяем тип блока
        RoomType targetType = (gender == Gender.MALE) ? RoomType.MALE : RoomType.FEMALE;
        room.setType(targetType);
        roomRepository.save(room);

        String blockCode = extractBlockCode(room.getRoomNumber());
        List<Room> sameBlockRooms = roomRepository.findSameBlockRooms(
                room.getDormitory().getId(), room.getId(), blockCode
        );
        for (Room br : sameBlockRooms) {
            if (br.getType() == RoomType.UNDEFINED) {
                br.setType(targetType);
                roomRepository.save(br);
            }
        }
    }

    /**
     * Формирует группы заявок на основе взаимно одобренных предпочтений.
     * Возвращает список групп, где каждая группа – список пользователей, которые должны заселяться вместе.
     */
    private List<List<User>> buildMutualGroups(List<Request> requests) {
        // Маппинг: пользователь -> множество пользователей, взаимно одобривших друг друга
        Map<Long, Set<Long>> mutualGraph = new HashMap<>();

        for (Request req : requests) {
            User user = req.getUser();
            mutualGraph.putIfAbsent(user.getId(), new HashSet<>());

            // Проходим по всем предпочтениям текущей заявки
            for (RequestPreference pref : req.getPreferences()) {
                if (pref.getStatus() == RequestPreferenceStatus.APPROVED) {
                    User preferred = pref.getPreferredUser();
                    // Проверяем, есть ли встречное APPROVED предпочтение от preferred к user
                    Request preferredRequest = requestRepository.findByUserId(preferred.getId())
                            .stream().filter(r -> r.getAllocation() == null).findFirst().orElse(null);
                    if (preferredRequest != null) {
                        boolean mutual = preferredRequest.getPreferences().stream()
                                .anyMatch(p -> p.getPreferredUser().getId().equals(user.getId())
                                        && p.getStatus() == RequestPreferenceStatus.APPROVED);
                        if (mutual) {
                            mutualGraph.get(user.getId()).add(preferred.getId());
                            mutualGraph.computeIfAbsent(preferred.getId(), k -> new HashSet<>()).add(user.getId());
                        }
                    }
                }
            }
        }

        // Поиск компонент связности в графе взаимных предпочтений
        Set<Long> visited = new HashSet<>();
        List<List<User>> groups = new ArrayList<>();

        for (Request req : requests) {
            Long userId = req.getUser().getId();
            if (visited.contains(userId)) continue;

            // BFS/DFS для сбора компоненты
            Set<Long> component = new HashSet<>();
            Deque<Long> stack = new ArrayDeque<>();
            stack.push(userId);
            visited.add(userId);
            while (!stack.isEmpty()) {
                Long current = stack.pop();
                component.add(current);
                for (Long neighbor : mutualGraph.getOrDefault(current, Collections.emptySet())) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        stack.push(neighbor);
                    }
                }
            }

            if (component.size() > 1) {
                // Группа из двух и более взаимно одобренных студентов
                List<User> groupUsers = new ArrayList<>();
                for (Long id : component) {
                    requestRepository.findByUserId(id).stream()
                            .filter(r -> r.getAllocation() == null)
                            .findFirst()
                            .ifPresent(r -> groupUsers.add(r.getUser()));
                }
                if (!groupUsers.isEmpty()) {
                    groups.add(groupUsers);
                }
            }
        }

        // Добавляем одиночные заявки (у которых нет взаимных одобрений)
        for (Request req : requests) {
            Long userId = req.getUser().getId();
            boolean alreadyInGroup = groups.stream().anyMatch(g -> g.stream().anyMatch(u -> u.getId().equals(userId)));
            if (!alreadyInGroup) {
                groups.add(List.of(req.getUser()));
            }
        }

        return groups;
    }

    /**
     * Поиск комнаты, способной вместить всю группу пользователей.
     * Учитываются текущие жильцы комнаты, пол, страна, вместимость.
     * Не проверяются одобренные соседи (так как группа уже взаимно одобрена).
     */
    private Room findRoomForGroup(List<User> users, List<Room> exclude) {
        int neededSlots = users.size();
        StudentDetail sample = users.get(0).getStudentDetail();
        String requiredCountry = sample.getCountry().getName();
        Gender requiredGender = sample.getGender();

        for (Room room : roomRepository.findAll()) {
            if (exclude != null && exclude.contains(room)) continue;

            // Текущие жильцы комнаты
            List<StudentDetail> current = allocationRepository
                    .findByRoomIdAndStatus(room.getId(), AllocationStatus.ACTIVE)
                    .stream()
                    .map(a -> a.getRequest().getUser().getStudentDetail())
                    .collect(Collectors.toList());

            // Проверка вместимости
            if (room.getCapacity() < current.size() + neededSlots) continue;

            // Проверка пола: комната должна быть UNDEFINED или совпадать с полом группы
            if (room.getType() != RoomType.UNDEFINED) {
                if ((room.getType() == RoomType.MALE && requiredGender != Gender.MALE) ||
                        (room.getType() == RoomType.FEMALE && requiredGender != Gender.FEMALE)) {
                    continue;
                }
            }

            // Проверка страны: все текущие жильцы и все члены группы должны быть из одной страны
            boolean countryOk = true;
            if (!current.isEmpty()) {
                String currentCountry = current.get(0).getCountry().getName();
                if (!currentCountry.equals(requiredCountry)) continue;
                for (StudentDetail sd : current) {
                    if (!sd.getCountry().getName().equals(currentCountry)) {
                        countryOk = false;
                        break;
                    }
                }
                if (!countryOk) continue;
            }
            // Все члены группы уже из одной страны (по построению группы)
            // Дополнительная проверка на случай, если в группе оказались разные страны (быть не должно)
            for (User u : users) {
                if (!u.getStudentDetail().getCountry().getName().equals(requiredCountry)) {
                    countryOk = false;
                    break;
                }
            }
            if (!countryOk) continue;

            // Если все проверки пройдены – комната подходит
            return room;
        }
        return null;
    }

    // ========================= ОСНОВНЫЕ МЕТОДЫ =========================

    @Transactional
    public AllocationResultDto runAllocation() {
        List<Request> activeRequests = requestRepository.findAll().stream()
                .filter(r -> r.getAllocation() == null)
                .collect(Collectors.toList());
        if (activeRequests.isEmpty()) {
            return new AllocationResultDto();
        }

        // Сортировка заявок по приоритету (убывание) и дате создания
        activeRequests.sort((r1, r2) -> {
            double p1 = priorityService.calculatePriority(r1.getUser().getStudentDetail());
            double p2 = priorityService.calculatePriority(r2.getUser().getStudentDetail());
            if (Double.compare(p2, p1) != 0) return Double.compare(p2, p1);
            return r1.getCreatedAt().compareTo(r2.getCreatedAt());
        });

        // Формирование групп на основе взаимных одобрений
        List<List<User>> groups = buildMutualGroups(activeRequests);
        // Сортировка групп по максимальному приоритету внутри группы (для жадности)
        groups.sort((g1, g2) -> {
            double maxP1 = g1.stream().mapToDouble(u -> priorityService.calculatePriority(u.getStudentDetail())).max().orElse(0);
            double maxP2 = g2.stream().mapToDouble(u -> priorityService.calculatePriority(u.getStudentDetail())).max().orElse(0);
            return Double.compare(maxP2, maxP1);
        });

        List<AllocationDto> allocations = new ArrayList<>();
        List<Long> unallocatedRequestIds = new ArrayList<>();

        for (List<User> group : groups) {
            Room selectedRoom = findRoomForGroup(group, null);
            if (selectedRoom != null) {
                // Заселяем всю группу в одну комнату
                boolean firstInRoom = allocationRepository.findByRoomIdAndStatus(selectedRoom.getId(), AllocationStatus.ACTIVE).isEmpty();
                for (User u : group) {
                    Request req = requestRepository.findByUserId(u.getId()).stream()
                            .filter(r -> r.getAllocation() == null)
                            .findFirst()
                            .orElse(null);
                    if (req != null) {
                        Allocation alloc = Allocation.builder()
                                .request(req)
                                .room(selectedRoom)
                                .status(AllocationStatus.ACTIVE)
                                .allocatedAt(LocalDateTime.now())
                                .build();
                        allocationRepository.save(alloc);
                        allocations.add(allocationMapper.toDto(alloc));
                    }
                }
                if (firstInRoom && !group.isEmpty()) {
                    // Синхронизация типа блока при первом заселении
                    syncBlockIfFirstOccupant(selectedRoom, group.get(0).getStudentDetail().getGender());
                }
            } else {
                // Группа не нашла комнату – вся группа нераспределена
                for (User u : group) {
                    requestRepository.findByUserId(u.getId()).stream()
                            .filter(r -> r.getAllocation() == null)
                            .findFirst()
                            .ifPresent(r -> unallocatedRequestIds.add(r.getId()));
                }
            }
        }

        AllocationResultDto result = new AllocationResultDto();
        result.setAllocations(allocations);
        result.setUnallocatedRequestIds(unallocatedRequestIds);
        return result;
    }

    @Transactional
    public AllocationDto manualAllocate(Long studentId, Long roomId) {
        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        Request request = requestRepository.findByUserId(user.getId()).stream()
                .filter(r -> r.getAllocation() == null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active request"));

        List<StudentDetail> current = allocationRepository
                .findByRoomIdAndStatus(roomId, AllocationStatus.ACTIVE)
                .stream()
                .map(a -> a.getRequest().getUser().getStudentDetail())
                .collect(Collectors.toList());

        // Проверка совместимости (без учёта одобренных соседей, т.к. ручное распределение)
        if (!compatibilityService.isCompatible(user.getStudentDetail(), room, current, List.of())) {
            throw new RuntimeException("Incompatible: check gender, country, or capacity");
        }

        Allocation allocation = Allocation.builder()
                .request(request)
                .room(room)
                .status(AllocationStatus.ACTIVE)
                .allocatedAt(LocalDateTime.now())
                .build();
        allocationRepository.save(allocation);

        // Синхронизация блока, если это первый жилец в комнате
        syncBlockIfFirstOccupant(room, user.getStudentDetail().getGender());

        return allocationMapper.toDto(allocation);
    }

    @Transactional
    public void evictStudent(Long allocationId) {
        Allocation allocation = allocationRepository.findById(allocationId)
                .orElseThrow(() -> new RuntimeException("Allocation not found"));
        Long roomId = allocation.getRoom().getId();
        allocationRepository.delete(allocation);

        // Если комната опустела, сбрасываем её тип на UNDEFINED и тип всех комнат блока? (по желанию)
        List<Allocation> remaining = allocationRepository.findByRoomIdAndStatus(roomId, AllocationStatus.ACTIVE);
        if (remaining.isEmpty()) {
            Room room = allocation.getRoom();
            room.setType(RoomType.UNDEFINED);
            roomRepository.save(room);
            // Можно также сбросить тип других комнат блока, но это не требуется по ТЗ
        }
    }

    public AllocationDto getAllocationByUser(User user) {
        Request request = requestRepository.findByUserId(user.getId()).stream()
                .filter(r -> r.getAllocation() != null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No allocation found"));
        Allocation allocation = request.getAllocation();
        AllocationDto dto = allocationMapper.toDto(allocation);
        List<UserProfileDto> roommates = allocationRepository
                .findByRoomIdAndStatus(allocation.getRoom().getId(), AllocationStatus.ACTIVE)
                .stream()
                .filter(a -> a.getRequest().getUser().getId() != user.getId())
                .map(a -> userMapper.toUserProfileDto(a.getRequest().getUser()))
                .collect(Collectors.toList());
        dto.setRoommates(roommates);
        return dto;
    }

    @Transactional
    public void confirmAllocation() {
        // Метод заглушка – можно оставить пустым
    }
}