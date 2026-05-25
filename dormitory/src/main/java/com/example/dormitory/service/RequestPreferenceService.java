package com.example.dormitory.service;

import com.example.dormitory.entity.Request;
import com.example.dormitory.entity.RequestPreference;
import com.example.dormitory.entity.User;
import com.example.dormitory.enums.RequestPreferenceStatus;
import com.example.dormitory.repository.RequestPreferenceRepository;
import com.example.dormitory.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RequestPreferenceService {
    private final RequestPreferenceRepository preferenceRepository;

    @Transactional
    public void checkAndConfirmMutual(RequestPreference newPreference) {
        Request request = newPreference.getRequest();
        Integer year = request.getYear();
        User requester = newPreference.getRequester();
        User preferred = newPreference.getPreferredUser();

        // Ищем встречное предпочтение: кто подал заявку на requester'а
        Optional<RequestPreference> mutualOpt = preferenceRepository.findMutual(preferred, requester, year);
        if (mutualOpt.isPresent()) {
            RequestPreference mutual = mutualOpt.get();
            // Если встречное предпочтение ещё не обработано
            if (mutual.getStatus() == RequestPreferenceStatus.PENDING) {
                // Устанавливаем статус APPROVED для обоих
                newPreference.setStatus(RequestPreferenceStatus.APPROVED);
                mutual.setStatus(RequestPreferenceStatus.APPROVED);
                preferenceRepository.save(newPreference);
                preferenceRepository.save(mutual);
            }
        }
    }

    @Transactional
    public void updateStatus(Long preferenceId, RequestPreferenceStatus newStatus) {
        RequestPreference pref = preferenceRepository.findById(preferenceId)
                .orElseThrow(() -> new RuntimeException("Preference not found"));
        pref.setStatus(newStatus);
        // Если комендант одобрил вручную, тоже проверяем взаимность
        if (newStatus == RequestPreferenceStatus.APPROVED) {
            checkAndConfirmMutual(pref);
        }
    }
}