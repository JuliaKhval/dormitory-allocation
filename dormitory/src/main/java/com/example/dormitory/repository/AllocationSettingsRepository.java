package com.example.dormitory.repository;

import com.example.dormitory.entity.AllocationSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllocationSettingsRepository extends JpaRepository<AllocationSettings, Long> {
}
