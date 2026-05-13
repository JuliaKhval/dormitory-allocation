package com.example.dormitory.repository;

import com.example.dormitory.entity.RequestPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestPreferenceRepository extends JpaRepository<RequestPreference, Long> {
    List<RequestPreference> findByRequestId(Long requestId);
    List<RequestPreference> findByPreferredUserId(Long userId);
    boolean existsByRequestIdAndPreferredUserId(Long requestId, Long preferredUserId);
}