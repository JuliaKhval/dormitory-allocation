package com.example.dormitory.repository;

import com.example.dormitory.entity.Room;
import com.example.dormitory.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByDormitoryIdAndType(Long dormitoryId, RoomType type);

    @Query("SELECT r FROM Room r WHERE r.dormitory.id = :dormitoryId AND r.id != :roomId AND r.roomNumber LIKE CONCAT(:blockCode, '%')")
    List<Room> findSameBlockRooms(@Param("dormitoryId") Long dormitoryId,
                                  @Param("roomId") Long roomId,
                                  @Param("blockCode") String blockCode);
}