package com.example.dormitory.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dormitories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dormitory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String address;

    @OneToOne
    @JoinColumn(name = "warden_user_id")
    private User warden;
}