package com.example.dormitory.service;

import com.example.dormitory.entity.*;
import com.example.dormitory.enums.Gender;
import com.example.dormitory.enums.RoomType;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompatibilityService {

    /**
     * Проверяет, может ли студент быть заселён в комнату с учётом текущих жильцов и одобренных соседей.
     * @param student студент для заселения
     * @param room целевая комната
     * @param currentOccupants уже заселённые студенты (список StudentDetail)
     * @param approvedRoommates список пользователей, которые должны уже жить в комнате (только для поштучного заселения)
     * @return true, если заселение возможно
     */
    public boolean isCompatible(StudentDetail student, Room room,
                                List<StudentDetail> currentOccupants,
                                List<User> approvedRoommates) {
        // 1. Пол комнаты (если комнате уже задан тип)
        if (room.getType() != RoomType.UNDEFINED) {
            if ((room.getType() == RoomType.MALE && student.getGender() != Gender.MALE) ||
                    (room.getType() == RoomType.FEMALE && student.getGender() != Gender.FEMALE)) {
                return false;
            }
        }
        // 2. Вместимость
        if (room.getCapacity() <= currentOccupants.size()) return false;
        // 3. Страна и пол – все жильцы (старые и новый) должны совпадать
        if (!currentOccupants.isEmpty()) {
            String requiredCountry = currentOccupants.get(0).getCountry().getName();
            Gender requiredGender = currentOccupants.get(0).getGender();
            if (!student.getCountry().getName().equals(requiredCountry)) return false;
            if (student.getGender() != requiredGender) return false;
            for (StudentDetail occ : currentOccupants) {
                if (!occ.getCountry().getName().equals(requiredCountry)) return false;
                if (occ.getGender() != requiredGender) return false;
            }
        }
        // 4. Одобренные соседи (если указаны) – они должны уже быть в комнате
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