package com.example.dormitory.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wardens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warden {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "full_name")
    private String fullName;
}