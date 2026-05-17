package com.example.dormitory.service;

import com.example.dormitory.entity.RequestPreference;
import com.example.dormitory.enums.RequestPreferenceStatus;
import com.example.dormitory.repository.RequestPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RequestPreferenceService {
    private final RequestPreferenceRepository preferenceRepository;

    @Transactional
    public void updateStatus(Long preferenceId, RequestPreferenceStatus newStatus) {
        RequestPreference pref = preferenceRepository.findById(preferenceId)
                .orElseThrow(() -> new RuntimeException("Preference not found"));
        pref.setStatus(newStatus);
    }
}