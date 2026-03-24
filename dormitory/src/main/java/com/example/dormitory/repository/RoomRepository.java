package com.example.dormitory.repository;

import com.example.dormitory.entity.Room;
import com.example.dormitory.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByType(RoomType type);
    List<Room> findByBuilding(String building);
}