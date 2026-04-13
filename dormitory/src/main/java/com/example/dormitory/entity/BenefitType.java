package com.example.dormitory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "benefit_types")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class BenefitType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(name = "priority_bonus", nullable = false)
    private Integer priorityBonus;
}