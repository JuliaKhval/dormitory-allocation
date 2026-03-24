package com.example.dormitory.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
public class RequestPreferenceId implements Serializable {
    private Long requesterUserId;
    private Long preferredUserId;
    private Integer year;
}