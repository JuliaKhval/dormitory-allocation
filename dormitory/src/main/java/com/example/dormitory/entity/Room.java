package com.example.dormitory.entity;

import com.example.dormitory.enums.RoomType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String building;

    @Column(nullable = false)
    private Integer floor;

    @Column(name = "room_number", nullable = false, length = 10)
    private String roomNumber;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomType type;

    @Column(length = 255)
    private String facilities;

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private List<Allocation> allocations = new ArrayList<>();
}