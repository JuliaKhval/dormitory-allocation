package com.example.dormitory.entity;

import com.example.dormitory.enums.RequestPreferenceStatus;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "request_preferences")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class RequestPreference {
    @EmbeddedId
    private RequestPreferenceId id;

    @ManyToOne
    @MapsId("requesterUserId")
    @JoinColumn(name = "requester_user_id", nullable = false)
    private User requester;

    @ManyToOne
    @MapsId("preferredUserId")
    @JoinColumn(name = "preferred_user_id", nullable = false)
    private User preferredUser;

    @Column(name = "year", insertable = false, updatable = false)
    private Integer year;

    @ManyToOne
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestPreferenceStatus status;
}