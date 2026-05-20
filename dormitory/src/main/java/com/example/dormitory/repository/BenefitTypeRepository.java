package com.example.dormitory.repository;

import com.example.dormitory.entity.BenefitType;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BenefitTypeRepository extends JpaRepository<BenefitType, Long> {
}