package com.example.dormitory.service;

import com.example.dormitory.dto.AllocationDto;
import com.example.dormitory.dto.AllocationResultDto;
import com.example.dormitory.dto.RoomSuggestionDto;
import com.example.dormitory.dto.UserProfileDto;
import com.example.dormitory.entity.AllocationSettings;
import com.example.dormitory.entity.*;
import com.example.dormitory.enums.AllocationStatus;
import com.example.dormitory.enums.Gender;
import com.example.dormitory.enums.RequestPreferenceStatus;
import com.example.dormitory.enums.RoomType;
import com.example.dormitory.mapper.AllocationMapper;
import com.example.dormitory.mapper.UserMapper;
import com.example.dormitory.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final AllocationSettingsRepository allocationSettingsRepository;
    private final DormitoryAssignmentService dormitoryAssignmentService;
    private final DormitoryRepository   dormitoryRepository;

    // ========================= ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ =========================

    /**
     * Извлекает код блока из номера комнаты (всё до последней буквы).
     * Пример: "101А" -> "101", "202" -> "202"
     */
    private String extractBlockCode(String roomNumber) {
        if (roomNumber == null || roomNumber.isBlank()) return "";
        if (roomNumber.matches(".*[A-Za-zА-Яа-я]$")) {
            return roomNumber.substring(0, roomNumber.length() - 1);
        }
        return roomNumber;
    }

    /**
     * Синхронизирует тип всех комнат блока при первом заселении в любую из них.
     * Все комнаты блока с типом UNDEFINED получают тип, соответствующий полу заселяемого студента.
     */
    private void syncBlockIfFirstOccupant(Room room, Gender gender) {
        boolean isOccupied = !allocationRepository
                .findByRoomIdAndStatus(room.getId(), AllocationStatus.ACTIVE)
                .isEmpty();
        if (isOccupied) return; // в комнате уже кто-то есть – блок уже синхронизирован

        // Первый жилец – определяем тип блока
        RoomType targetType = (gender == Gender.MALE) ? RoomType.MALE : RoomType.FEMALE;
        room.setType(targetType);
        roomRepository.save(room);

        String blockCode = extractBlockCode(room.getRoomNumber());
        List<Room> blockRooms = roomRepository.findSameBlockRooms(
                room.getDormitory().getId(), room.getId(), blockCode
        );
        for (Room br : blockRooms) {
            if (br.getType() == RoomType.UNDEFINED) {
                br.setType(targetType);
                roomRepository.save(br);
            }
        }
    }
    private void initBlockType(Room room, Gender gender) {
        RoomType targetType = (gender == Gender.MALE) ? RoomType.MALE : RoomType.FEMALE;
        String blockCode = extractBlockCode(room.getRoomNumber());
        // Сначала меняем текущую комнату, если она UNDEFINED
        if (room.getType() == RoomType.UNDEFINED) {
            room.setType(targetType);
            roomRepository.save(room);
        }
        // Находим все комнаты того же блока (с одинаковым числовым префиксом)
        List<Room> blockRooms = roomRepository.findSameBlockRooms(room.getDormitory().getId(), room.getId(), blockCode);
        for (Room br : blockRooms) {
            if (br.getType() == RoomType.UNDEFINED) {
                br.setType(targetType);
                roomRepository.save(br);
            }
        }
    }
    /**
     * Формирует группы студентов на основе взаимных APPROVED предпочтений.
     * Возвращает список групп (каждая группа – список пользователей).
     */
    private List<List<User>> buildMutualGroups(List<Request> requests) {
        // Карта: ID пользователя -> множество ID взаимно одобренных пользователей
        Map<Long, Set<Long>> mutualGraph = new HashMap<>();

        // Заполняем граф взаимных одобрений
        for (Request req : requests) {
            User user = req.getUser();
            mutualGraph.putIfAbsent(user.getId(), new HashSet<>());
            for (RequestPreference pref : req.getPreferences()) {
                if (pref.getStatus() == RequestPreferenceStatus.APPROVED) {
                    User preferred = pref.getPreferredUser();
                    // Проверяем, есть ли встречное APPROVED предпочтение
                    Request preferredRequest = requestRepository.findByUserId(preferred.getId())
                            .stream()
                            .filter(r -> r.getAllocation() == null)
                            .findFirst()
                            .orElse(null);
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

        // Поиск компонент связности (групп)
        Set<Long> visited = new HashSet<>();
        List<List<User>> groups = new ArrayList<>();

        for (Request req : requests) {
            Long userId = req.getUser().getId();
            if (visited.contains(userId)) continue;

            // BFS для сбора компоненты
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
                // Группа из нескольких взаимно одобренных студентов
                List<User> groupUsers = new ArrayList<>();
                for (Long id : component) {
                    requestRepository.findByUserId(id).stream()
                            .filter(r -> r.getAllocation() == null)
                            .findFirst()
                            .ifPresent(r -> groupUsers.add(r.getUser()));
                }
                if (!groupUsers.isEmpty()) groups.add(groupUsers);
            }
        }

        // Добавляем одиночные заявки (не вошедшие в группы)
        for (Request req : requests) {
            Long userId = req.getUser().getId();
            boolean alreadyInGroup = groups.stream().anyMatch(g -> g.stream().anyMatch(u -> u.getId().equals(userId)));
            if (!alreadyInGroup) {
                groups.add(List.of(req.getUser()));
            }
        }

        // Сортируем группы по максимальному приоритету внутри группы (убывание)
        groups.sort((g1, g2) -> {
            double max1 = g1.stream().mapToDouble(u -> priorityService.calculatePriority(u.getStudentDetail())).max().orElse(0);
            double max2 = g2.stream().mapToDouble(u -> priorityService.calculatePriority(u.getStudentDetail())).max().orElse(0);
            return Double.compare(max2, max1);
        });

        return groups;
    }

    /**
     * Поиск комнаты, способной вместить всю группу пользователей.
     * Проверяются пол, страна, вместимость. Группа уже взаимно одобрена, поэтому проверка approvedRoommates не нужна.
     */
    private List<Room> sortRoomsForGroup(List<Room> rooms, List<User> users, AllocationSettings settings) {
        boolean hasBenefit = users.stream().anyMatch(u ->
                u.getStudentDetail().getBenefits() != null && !u.getStudentDetail().getBenefits().isEmpty());
        List<Integer> floorOrder = settings != null && settings.getFloorOrder() != null
                ? Arrays.stream(settings.getFloorOrder().split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).map(Integer::parseInt).toList()
                : List.of();
        Integer benefitFloorStart = settings != null ? settings.getBenefitFloorStart() : null;

        return rooms.stream().sorted((r1, r2) -> {
            if (hasBenefit && benefitFloorStart != null) {
                boolean r1Benefit = r1.getFloor() >= benefitFloorStart;
                boolean r2Benefit = r2.getFloor() >= benefitFloorStart;
                if (r1Benefit != r2Benefit) return r1Benefit ? -1 : 1;
            }
            if (!floorOrder.isEmpty()) {
                int i1 = floorOrder.indexOf(r1.getFloor());
                int i2 = floorOrder.indexOf(r2.getFloor());
                int o1 = i1 < 0 ? Integer.MAX_VALUE : i1;
                int o2 = i2 < 0 ? Integer.MAX_VALUE : i2;
                if (o1 != o2) return Integer.compare(o1, o2);
            }
            return Integer.compare(r1.getFloor(), r2.getFloor());
        }).collect(Collectors.toList());
    }

    private Room findRoomForGroup(List<User> users, Long dormitoryId, AllocationSettings settings) {
        int neededSlots = users.size();
        if (users.isEmpty()) return null;
        StudentDetail sample = users.get(0).getStudentDetail();
        String requiredCountry = sample.getCountry().getName();
        Gender requiredGender = sample.getGender();

        List<Room> candidateRooms = roomRepository.findByDormitoryId(dormitoryId);
        candidateRooms = sortRoomsForGroup(candidateRooms, users, settings);

        for (Room room : candidateRooms) {
            List<StudentDetail> current = allocationRepository
                    .findByRoomIdAndStatus(room.getId(), AllocationStatus.ACTIVE)
                    .stream()
                    .map(a -> a.getRequest().getUser().getStudentDetail())
                    .collect(Collectors.toList());

            // Вместимость
            if (room.getCapacity() < current.size() + neededSlots) continue;

            // Пол комнаты
            if (room.getType() != RoomType.UNDEFINED) {
                if ((room.getType() == RoomType.MALE && requiredGender != Gender.MALE) ||
                        (room.getType() == RoomType.FEMALE && requiredGender != Gender.FEMALE)) {
                    continue;
                }
            }

            // Страна: все текущие жильцы и все члены группы должны быть из одной страны
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
            for (User u : users) {
                if (!u.getStudentDetail().getCountry().getName().equals(requiredCountry)) {
                    countryOk = false;
                    break;
                }
            }
            if (!countryOk) continue;

            // Все проверки пройдены
            return room;
        }
        return null;
    }

    // ========================= ОСНОВНЫЕ МЕТОДЫ =========================

    @Transactional
    public AllocationResultDto runAllocation(Long dormitoryId) {
        if (dormitoryId == null) {
            throw new RuntimeException("Dormitory id is required for allocation");
        }
        AllocationSettings settings = allocationSettingsRepository.findById(dormitoryId).orElse(null);

        List<Request> activeRequests = requestRepository.findAll().stream()
                .filter(r -> r.getAllocation() == null)
                .filter(r -> dormitoryId.equals(dormitoryAssignmentService.resolveTargetDormitory(r.getUser().getStudentDetail())))
                .collect(Collectors.toList());
        if (activeRequests.isEmpty()) return new AllocationResultDto();

        activeRequests = sortRequestsByFacultyPriority(activeRequests, settings);

        List<List<User>> groups = buildMutualGroups(activeRequests);

        List<AllocationDto> allocations = new ArrayList<>();
        List<Long> unallocatedRequestIds = new ArrayList<>();

        for (List<User> group : groups) {
            Room selectedRoom = findRoomForGroup(group, dormitoryId, settings);
            if (selectedRoom != null) {
                // Заселяем всю группу в одну комнату
                boolean wasEmpty = allocationRepository.findByRoomIdAndStatus(selectedRoom.getId(), AllocationStatus.ACTIVE).isEmpty();

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
                // Если комната была пуста до заселения – синхронизируем блок
                if (wasEmpty && !group.isEmpty()) {
                    initBlockType(selectedRoom, group.get(0).getStudentDetail().getGender());
                }

            } else {
                // Комната не найдена – вся группа нераспределена
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
    public AllocationDto manualAllocate(Long studentId, Long roomId, Long wardenUserId) {
        // Проверка, что комендант существует и привязан к общежитию
        User warden = userRepository.findById(wardenUserId)
                .orElseThrow(() -> new RuntimeException("Warden not found"));
        Dormitory wardenDormitory = dormitoryRepository.findByWardenId(warden.getId())
                .orElseThrow(() -> new RuntimeException("Warden is not assigned to any dormitory"));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        // Комната должна принадлежать общежитию коменданта
        if (!room.getDormitory().getId().equals(wardenDormitory.getId())) {
            throw new RuntimeException("You can only allocate students to rooms in your own dormitory");
        }

        // Далее существующая логика поиска пользователя, заявки, проверка совместимости и т.д.
        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Request request = requestRepository.findByUserId(user.getId()).stream()
                .filter(r -> r.getAllocation() == null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active request"));

        List<StudentDetail> current = allocationRepository
                .findByRoomIdAndStatus(roomId, AllocationStatus.ACTIVE)
                .stream()
                .map(a -> a.getRequest().getUser().getStudentDetail())
                .collect(Collectors.toList());

        // Проверка совместимости
        if (!compatibilityService.isCompatible(user.getStudentDetail(), room, current, List.of())) {
            throw new RuntimeException("Incompatible: check gender, country, or capacity");
        }

        boolean wasEmpty = current.isEmpty();

        Allocation allocation = Allocation.builder()
                .request(request)
                .room(room)
                .status(AllocationStatus.ACTIVE)
                .allocatedAt(LocalDateTime.now())
                .build();
        allocationRepository.save(allocation);

        if (wasEmpty) {
            initBlockType(room, user.getStudentDetail().getGender());
        }

        return allocationMapper.toDto(allocation);
    }

    @Transactional
    public void evictStudent(Long allocationId) {
        Allocation allocation = allocationRepository.findById(allocationId)
                .orElseThrow(() -> new RuntimeException("Allocation not found"));
        Long roomId = allocation.getRoom().getId();
        Request request = allocation.getRequest();

        // 1. Разрываем связь со стороны Request
        request.setAllocation(null);
        requestRepository.save(request);  // сохраняем Request с обнулённой ссылкой

        // 2. Удаляем Allocation
        allocationRepository.delete(allocation);

        // 3. Проверяем, опустела ли комната
        List<Allocation> remaining = allocationRepository.findByRoomIdAndStatus(roomId, AllocationStatus.ACTIVE);
        if (remaining.isEmpty()) {
            Room room = allocation.getRoom();
            room.setType(RoomType.UNDEFINED);
            roomRepository.save(room);
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
        // Заглушка – можно оставить пустой
    }

    private List<Request> sortRequestsByFacultyPriority(List<Request> requests, AllocationSettings settings) {
        if (settings == null || settings.getFacultyPriorityOrder() == null || settings.getFacultyPriorityOrder().isBlank()) {
            return requests;
        }
        List<Long> facultyOrder = Arrays.stream(settings.getFacultyPriorityOrder().split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).map(Long::parseLong).toList();
        List<Request> sorted = new ArrayList<>(requests);
        sorted.sort((r1, r2) -> {
            Long f1 = r1.getUser().getStudentDetail().getGroup().getFaculty().getId();
            Long f2 = r2.getUser().getStudentDetail().getGroup().getFaculty().getId();
            int i1 = facultyOrder.indexOf(f1);
            int i2 = facultyOrder.indexOf(f2);
            return Integer.compare(i1 < 0 ? Integer.MAX_VALUE : i1, i2 < 0 ? Integer.MAX_VALUE : i2);
        });
        return sorted;
    }

    public List<RoomSuggestionDto> suggestRoomsForRelocation(Long allocationId) {
        Allocation allocation = allocationRepository.findById(allocationId)
                .orElseThrow(() -> new RuntimeException("Allocation not found"));
        User user = allocation.getRequest().getUser();
        StudentDetail student = user.getStudentDetail();
        Long dormitoryId = allocation.getRoom().getDormitory().getId();
        List<Room> rooms = roomRepository.findByDormitoryId(dormitoryId);

        List<RoomSuggestionDto> suggestions = new ArrayList<>();
        for (Room room : rooms) {
            if (room.getId().equals(allocation.getRoom().getId())) continue;
            List<StudentDetail> current = allocationRepository
                    .findByRoomIdAndStatus(room.getId(), AllocationStatus.ACTIVE)
                    .stream()
                    .map(a -> a.getRequest().getUser().getStudentDetail())
                    .collect(Collectors.toList());
            if (compatibilityService.isCompatible(student, room, current, List.of())) {
                RoomSuggestionDto dto = new RoomSuggestionDto();
                dto.setRoomId(room.getId());
                dto.setDormitoryName(room.getDormitory().getName());
                dto.setFloor(room.getFloor());
                dto.setRoomNumber(room.getRoomNumber());
                dto.setCapacity(room.getCapacity());
                dto.setOccupied(current.size());
                dto.setFreeSlots(room.getCapacity() - current.size());
                dto.setType(room.getType().name());
                suggestions.add(dto);
            }
        }
        suggestions.sort(Comparator.comparing(RoomSuggestionDto::getFloor).thenComparing(RoomSuggestionDto::getRoomNumber));
        return suggestions;
    }

    @Transactional
    public AllocationDto relocateStudent(Long allocationId, Long targetRoomId, Long wardenUserId) {
        Allocation allocation = allocationRepository.findById(allocationId)
                .orElseThrow(() -> new RuntimeException("Allocation not found"));
        Long userId = allocation.getRequest().getUser().getId();
        evictStudent(allocationId);
        return manualAllocate(userId, targetRoomId, wardenUserId);
    }
}