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
    public void updateStatus(Long preferenceId, RequestPreferenceStatus newStatus, String rejectionReason) {
        RequestPreference pref = preferenceRepository.findById(preferenceId)
                .orElseThrow(() -> new RuntimeException("Preference not found"));
        pref.setStatus(newStatus);
        if (newStatus == RequestPreferenceStatus.REJECTED) {
            pref.setRejectionReason(rejectionReason);
        } else {
            pref.setRejectionReason(null);
        }
        preferenceRepository.save(pref);
        if (newStatus == RequestPreferenceStatus.APPROVED) {
            checkAndConfirmMutual(pref);
        }
    }

    @Transactional
    public void checkAndConfirmMutual(RequestPreference newPref) {
        if (newPref == null) {
            return;   // или логировать, но лучше не вызывать с null
        }
        Integer year = newPref.getRequest().getYear();
        Long requesterId = newPref.getRequester().getId();
        Long preferredId = newPref.getPreferredUser().getId();

        preferenceRepository.findByRequesterAndPreferred(preferredId, requesterId, year)
                .ifPresent(mutual -> {
                    if (mutual.getStatus() == RequestPreferenceStatus.PENDING) {
                        newPref.setStatus(RequestPreferenceStatus.APPROVED);
                        mutual.setStatus(RequestPreferenceStatus.APPROVED);
                        preferenceRepository.save(newPref);
                        preferenceRepository.save(mutual);
                    }
                });
    }
}