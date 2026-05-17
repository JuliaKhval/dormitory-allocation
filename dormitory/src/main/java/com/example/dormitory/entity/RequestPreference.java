package com.example.dormitory.entity;

import com.example.dormitory.enums.RequestPreferenceStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "request_preferences",
        uniqueConstraints = @UniqueConstraint(columnNames = {"requester_user_id", "preferred_user_id", "year"}))
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class RequestPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "requester_user_id", nullable = false)
    private User requester;

    @ManyToOne
    @JoinColumn(name = "preferred_user_id", nullable = false)
    private User preferredUser;

    @Column(name = "year", nullable = false)
    private Integer year;

    @ManyToOne
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestPreferenceStatus status;
}