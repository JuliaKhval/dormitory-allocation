package com.example.dormitory.repository;

import com.example.dormitory.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByUserId(Long userId);
    List<Request> findByYear(Integer year);
    Optional<Request> findByIdAndUserId(Long id, Long userId);
}