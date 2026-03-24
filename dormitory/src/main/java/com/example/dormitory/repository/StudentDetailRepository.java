package com.example.dormitory.repository;

import com.example.dormitory.entity.StudentDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentDetailRepository extends JpaRepository<StudentDetail, Long> {
    Optional<StudentDetail> findByUserId(Long userId);
}
