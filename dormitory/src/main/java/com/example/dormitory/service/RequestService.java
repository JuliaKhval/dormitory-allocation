package com.example.dormitory.service;

import com.example.dormitory.dto.CreateRequestDto;
import com.example.dormitory.dto.RequestDto;
import com.example.dormitory.dto.UserProfileDto;
import com.example.dormitory.entity.*;
import com.example.dormitory.enums.AllocationStatus;
import com.example.dormitory.enums.RequestPreferenceStatus;
import com.example.dormitory.mapper.RequestMapper;
import com.example.dormitory.mapper.UserMapper;
import com.example.dormitory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestService {
    private final RequestRepository requestRepository;
    private final RequestPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;
    private final RequestPreferenceService preferenceService;
    private final RequestMapper requestMapper;
    private final AllocationRepository allocationRepository;
    private final UserMapper userMapper;

    @Transactional
    public RequestDto createRequest(Long userId, CreateRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        StudentDetail student = user.getStudentDetail();
        if (student == null) throw new RuntimeException("User is not a student");

        boolean hasActive = requestRepository.findByUserId(userId).stream()
                .anyMatch(r -> r.getAllocation() == null);
        if (hasActive) throw new RuntimeException("You already have an active request");

        int year = LocalDateTime.now().getYear();
        Request request = Request.builder()
                .user(user)
                .year(year)
                .build();
        request = requestRepository.save(request);

        List<Long> preferredIds = dto.getPreferredUserIds();
        if (preferredIds != null && preferredIds.size() > 3)
            throw new RuntimeException("At most 3 preferred roommates");

        // Обрабатываем предпочтения, только если они есть
        if (preferredIds != null && !preferredIds.isEmpty()) {
            for (Long prefId : preferredIds) {
                User prefUser = userRepository.findById(prefId)
                        .orElseThrow(() -> new RuntimeException("Preferred user not found"));
                StudentDetail prefStudent = prefUser.getStudentDetail();
                if (prefStudent == null)
                    throw new RuntimeException("Preferred user is not a student");
                if (prefId.equals(userId))
                    throw new RuntimeException("You cannot select yourself as a roommate");
                if (!prefStudent.getCountry().getId().equals(student.getCountry().getId()))
                    throw new RuntimeException("Preferred roommate must be from the same country");
                if (prefStudent.getGender() != student.getGender()) {
                    throw new RuntimeException("Preferred roommate must be of the same gender");
                }

                RequestPreference pref = RequestPreference.builder()
                        .requester(user)
                        .preferredUser(prefUser)
                        .year(year)
                        .request(request)
                        .status(RequestPreferenceStatus.PENDING)
                        .build();
                preferenceRepository.save(pref);
                // Вызываем проверку для КАЖДОГО созданного предпочтения
                preferenceService.checkAndConfirmMutual(pref);
            }
        }

        return requestMapper.toDto(request);
    }
    public List<RequestDto> getUserRequests(Long userId) {
        return requestRepository.findByUserId(userId).stream()
                .map(req -> {
                    RequestDto dto = requestMapper.toDto(req);
                    dto.setRequestStatus(computeRequestStatus(req));
                    if (req.getAllocation() != null) {
                        fillRoommates(dto, req.getAllocation());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<RequestDto> getAllRequests(String sortBy) {
        List<Request> requests = requestRepository.findAllWithUser(); // нужен JOIN FETCH
        if ("date".equalsIgnoreCase(sortBy)) {
            requests.sort((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()));
        }
        return requests.stream()
                .map(req -> {
                    RequestDto dto = requestMapper.toDto(req);
                    dto.setRequestStatus(computeRequestStatus(req));
                    if (req.getAllocation() != null) {
                        fillRoommates(dto, req.getAllocation());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelRequest(Long requestId, Long userId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        if (!request.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only cancel your own requests");
        }
        if (request.getAllocation() != null) {
            throw new RuntimeException("Cannot cancel an already allocated request");
        }
        preferenceRepository.deleteAll(preferenceRepository.findByRequestId(requestId));
        requestRepository.delete(request);
    }

    private String computeRequestStatus(Request request) {
        if (request.getAllocation() != null) return "ALLOCATED";
        boolean hasApproved = request.getPreferences().stream()
                .anyMatch(p -> p.getStatus() == RequestPreferenceStatus.APPROVED);
        boolean hasRejected = request.getPreferences().stream()
                .anyMatch(p -> p.getStatus() == RequestPreferenceStatus.REJECTED);
        if (hasApproved) return "APPROVED";
        if (hasRejected) return "REJECTED";
        return "PENDING";
    }

    private void fillRoommates(RequestDto dto, Allocation allocation) {
        List<UserProfileDto> roommates = allocationRepository
                .findByRoomIdAndStatus(allocation.getRoom().getId(), AllocationStatus.ACTIVE)
                .stream()
                .filter(a -> a.getRequest().getUser().getId() != dto.getUserId())
                .map(a -> userMapper.toUserProfileDto(a.getRequest().getUser()))
                .collect(Collectors.toList());
        if (dto.getAllocation() != null) {
            dto.getAllocation().setRoommates(roommates);
        }
    }
}