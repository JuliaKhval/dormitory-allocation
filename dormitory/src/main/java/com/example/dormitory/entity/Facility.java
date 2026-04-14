package com.example.dormitory.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "facilities")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Facility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
}