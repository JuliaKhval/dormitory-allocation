package com.example.dormitory.repository;

import com.example.dormitory.entity.Dormitory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface DormitoryRepository extends JpaRepository<Dormitory, Long> {
    Optional<Dormitory> findByName(String name);
    Optional<Dormitory> findByWardenId(Long wardenUserId);
}