package com.example.dormitory.repository;

import com.example.dormitory.entity.RequestPreference;
import com.example.dormitory.entity.RequestPreferenceId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.ScopedValue;
import java.util.List;

public interface RequestPreferenceRepository extends JpaRepository<RequestPreference, RequestPreferenceId> {
    List<RequestPreference> findByRequestId(Long requestId);
    List<RequestPreference> findByPreferredUserId(Long userId);
    boolean existsByRequestIdAndPreferredUserId(Long requestId, Long preferredUserId);

    List<RequestPreference> findById(Long preferenceId);
}