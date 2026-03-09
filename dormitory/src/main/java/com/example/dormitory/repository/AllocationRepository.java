package com.example.dormitory.repository;

import com.example.dormitory.entity.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AllocationRepository extends JpaRepository<Allocation, Long> {
    Optional<Allocation> findByRequestId(Long requestId);
    List<Allocation> findByRoomId(Long roomId);
}