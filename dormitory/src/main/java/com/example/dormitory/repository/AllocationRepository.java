package com.example.dormitory.repository;

import com.example.dormitory.entity.Allocation;
import com.example.dormitory.enums.AllocationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllocationRepository extends JpaRepository<Allocation, Long> {
    List<Allocation> findByRoomId(Long roomId);
    List<Allocation> findByRoomIdAndStatus(Long roomId, AllocationStatus status);
}