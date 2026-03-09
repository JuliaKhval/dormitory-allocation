package com.example.dormitory.repository;

import com.example.dormitory.entity.Room;
import com.example.dormitory.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {
    List<Room> findByType(RoomType type);
    List<Room> findByBuilding(String building);
}