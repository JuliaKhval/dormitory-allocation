package com.example.dormitory.service;

import com.example.dormitory.entity.*;
import com.example.dormitory.enums.Gender;
import com.example.dormitory.enums.RoomType;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompatibilityService {

    public boolean isCompatible(StudentDetail student, Room room,
                                List<StudentDetail> currentOccupants,
                                List<User> approvedRoommates) {
        // 1. Пол
        if (room.getType() == RoomType.MALE && student.getGender() != Gender.MALE) return false;
        if (room.getType() == RoomType.FEMALE && student.getGender() != Gender.FEMALE) return false;

        // 2. Вместимость
        if (room.getCapacity() <= currentOccupants.size()) return false;

        // 3. Страна
        if (!currentOccupants.isEmpty()) {
            String firstCountry = currentOccupants.get(0).getCountry().getName();
            if (!student.getCountry().getName().equals(firstCountry)) return false;
            for (StudentDetail occ : currentOccupants) {
                if (!occ.getCountry().getName().equals(firstCountry)) return false;
            }
        }

        // 4. Одобренные соседи
        if (approvedRoommates != null && !approvedRoommates.isEmpty()) {
            List<Long> currentIds = currentOccupants.stream()
                    .map(occ -> occ.getUser().getId())
                    .collect(Collectors.toList());
            for (User pref : approvedRoommates) {
                if (!currentIds.contains(pref.getId())) return false;
            }
        }
        return true;
    }
}